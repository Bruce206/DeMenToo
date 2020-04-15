package de.bruss.demontoo.ssh;

import com.jcraft.jsch.*;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class SshService {

    @Value("${openssh.key}")
    private String prvKeyLocation;

    @Value("${openssh.pass}")
    private String privateKeyPass;

    private final Logger logger = LoggerFactory.getLogger(SshService.class);

	public Session getSession(String host) {
		try {
			JSch jsch = new JSch();

			if (jsch.getIdentityNames().size() == 0) {
				jsch.addIdentity(prvKeyLocation, privateKeyPass);
			}

			Session session = jsch.getSession("root", host, 22);

			java.util.Properties props = new java.util.Properties();
			props.put("StrictHostKeyChecking", "no");
			session.setConfig(props);

			return session;

		} catch (Exception e) {
			logger.error("Connection failed", e);
		}
		return null;
	}

	public ChannelSftp getSftpChannel(Session session) throws JSchException {
		Channel channel = session.openChannel("sftp");
		channel.setInputStream(System.in);
		channel.setOutputStream(System.out);
		channel.connect();
		return (ChannelSftp) channel;
	}

	public ChannelExec getExecChannel(Session session) throws JSchException {
		Channel channel;
		channel = session.openChannel("exec");
		return (ChannelExec) channel;
	}

	public ChannelShell getShellChannel(Session session) throws JSchException {
		Channel channel;
		channel = session.openChannel("shell");
		return (ChannelShell) channel;
	}

	public void copyFile(String from, String to, Session session, ProgressBar progressBar) throws JSchException, SftpException {
		ChannelSftp sftpChannel = getSftpChannel(session);
		sftpChannel.put(from, to, new SftpProgressMonitor() {

			private double bytes;
			private double max;

			@Override
			public void init(int op, String src, String dest, long max) {
				this.max = max;
				logger.info("-- Starting upload... FileSize: " + FileUtils.byteCountToDisplaySize(max));
				progressBar.setVisible(true);
			}

			@Override
			public void end() {
				logger.info("-- Finished uploading!");
				progressBar.setVisible(false);
			}

			@Override
			public boolean count(long bytes) {
				this.bytes += bytes;
				progressBar.setProgress(this.bytes / max);
				return true;
			}
		});

		sftpChannel.exit();
		sftpChannel.disconnect();
	}

	public void downloadFile(String from, String to, Session session) throws JSchException, SftpException {
		ChannelSftp sftpChannel = getSftpChannel(session);
		sftpChannel.get(from, to, new SftpProgressMonitor() {

			private double bytes;
			private double max;

			@Override
			public void init(int op, String src, String dest, long max) {
				this.max = max;
				logger.info("Starting Download (FileSize: " + FileUtils.byteCountToDisplaySize(max) + ") ...");
			}

			@Override
			public void end() {
				logger.info(" -> [done]!");
			}

			@Override
			public boolean count(long bytes) {
				this.bytes += bytes;
				return true;
			}
		});

		sftpChannel.exit();
		sftpChannel.disconnect();
	}

	public void createRemotePath(Session session, String path) throws JSchException {
		ChannelSftp sftpChannel = getSftpChannel(session);
		try {
			for (String folder : path.split("/")) {

				if (StringUtils.isNotBlank(folder)) {
					try {
						sftpChannel.mkdir(folder);
						logger.info("Created folder: " + folder);
					} catch (SftpException sftpe) {
						continue;
					} finally {
						sftpChannel.cd(folder);
					}
				} else {
					sftpChannel.cd("/");
				}

			}
		} catch (SftpException e) {
			logger.error("Folder creation failed!", e);
		}

	}

	public boolean fileExistsOnServer(Session session, String path) throws JSchException {
		ChannelSftp sftpChannel = getSftpChannel(session);

		try {
			sftpChannel.lstat(path);
		} catch (SftpException se) {
			logger.info("File " + path + " not found on server!");
			return false;
		}

		sftpChannel.exit();
		sftpChannel.disconnect();
		return true;
	}

    public void stopService(Session session, ServiceType serviceType, String serviceName) {
        if (ServiceType.UPSTART.equals(serviceType)) {
            sendCommand(session, "stop " + serviceName);
        } else {
            sendCommand(session, "service " + serviceName + " stop");
        }
    }

    public void startService(Session session, ServiceType serviceType, String serviceName) {
        if (ServiceType.UPSTART.equals(serviceType)) {
            sendCommand(session, "start " + serviceName);
        } else {
            sendCommand(session, "service " + serviceName + " start");
        }
    }

	public String sendCommand(Session session, String command) {

		try {
			StringBuilder outputBuffer = new StringBuilder();

			ChannelExec execChannel = getExecChannel(session);
			execChannel.setCommand(command);
			execChannel.connect();

			InputStream commandOutput = execChannel.getInputStream();
			int readByte = commandOutput.read();

			while (readByte != 0xffffffff) {
				outputBuffer.append((char) readByte);
				readByte = commandOutput.read();
			}

			execChannel.disconnect();

			return outputBuffer.toString();
		} catch (JSchException | IOException e) {
			logger.error("Failed to connect to Server while sending Command: " + command, e);

		}
		return null;
	}

	public void removeFile(Session session, String path) throws JSchException {
		ChannelSftp sftpChannel = getSftpChannel(session);

		try {
			sftpChannel.rm(path);
		} catch (SftpException se) {
			logger.info("File " + path + " not found on server!");
		}

		sftpChannel.exit();
		sftpChannel.disconnect();
	}

    public ServiceType getServiceType(Session session) {
        String response = sendCommand(session, "lsb_release -r");

        String version = StringUtils.chomp(response.substring(response.indexOf(":") + 1, response.length()).trim());

        double versionDbl = Double.parseDouble(version);

        if (versionDbl < 16) {
            logger.info("Detected Ubuntu-Version: " + version + ". Using " + ServiceType.UPSTART.toString());
            return ServiceType.UPSTART;
        } else {
            logger.info("Detected Ubuntu-Version: " + version + ". Using " + ServiceType.SYSTEMD.toString());
            return ServiceType.SYSTEMD;
        }

    }

    public enum ServiceType {
        UPSTART, SYSTEMD
    }
}
