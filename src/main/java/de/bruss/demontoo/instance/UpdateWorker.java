package de.bruss.demontoo.instance;

import com.jcraft.jsch.*;
import de.bruss.demontoo.server.Server;
import de.bruss.demontoo.ssh.SshService;
import de.bruss.demontoo.websockets.Instance;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Scope(value = "prototype")
public class UpdateWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(UpdateWorker.class);
    private String filesHome;

    private InstanceType instanceType;

    private final SshService sshService;

    public UpdateWorker(SshService sshService, String filesHome) {
        this.sshService = sshService;
        this.filesHome = filesHome;
    }

    @Override
    @Transactional
    public void run() {
        logger.info("Updating " + instanceType.getName());
        String remotePath = instanceType.getUpdatePath();
        if (StringUtils.isEmpty(remotePath)) {
            throw new RuntimeException("remotePath empty");
        }

        if (!remotePath.startsWith("/")) {
            remotePath = "/" + remotePath;
        }

        if (!remotePath.endsWith("/")) {
            remotePath = remotePath + "/";
        }

        if (!filesHome.endsWith("/")) {
            filesHome += "/";
        }

        Map<Server, List<Instance>> instancesByServer = instanceType.getInstances().stream().collect(Collectors.groupingBy(Instance::getServer));

        for (Server server : instancesByServer.keySet()) {
            try {
                logger.info("Deploying " + instanceType.getName() + " to server: " + server.getServerName() + "[Number of instances: " + instancesByServer.get(server).size() + "]");
                List<Instance> instancesOnServer = instancesByServer.get(server);

                Session session = sshService.getSession(server.getIp());
                session.connect();

                // find out service-type (UPSTART or SYSTEMD)
                SshService.ServiceType serviceType = sshService.getServiceType(session);

                ChannelSftp sftpChannel = sshService.getSftpChannel(session);

                String remoteFilePath = remotePath + instanceType.getName().toLowerCase() + ".jar";

                sftpChannel.put(filesHome + instanceType.getName() + ".jar", remoteFilePath + "_tmp", new SftpProgressMonitor() {

                    private long bytes;
                    private long max;

                    @Override
                    public void init(int op, String src, String dest, long max) {
                        this.max = max;
                        logger.info("-- Starting upload... FileSize: " + FileUtils.byteCountToDisplaySize(max));
                    }

                    @Override
                    public void end() {
                        logger.info("-- Finished uploading!");
                    }

                    @Override
                    public boolean count(long bytes) {
                        this.bytes += bytes;
                        logger.info("Bytes transferred: " + FileUtils.byteCountToDisplaySize(this.bytes) + " of " + FileUtils.byteCountToDisplaySize(this.max));
                        return true;
                    }
                });

                // stop instances
                for (Instance i : instancesOnServer) {
                    logger.info("restarting instance: " + i.getIdentifier());
                    sshService.stopService(session, serviceType, i.getIdentifier());
                    logger.info("restarted  instance: " + i.getIdentifier());
                }

                // copy tmp to prod jar
                sftpChannel.rename(remoteFilePath, remoteFilePath + "_old");
                sftpChannel.rename(remoteFilePath + "_tmp", remoteFilePath);

                // start instances
                for (Instance i : instancesOnServer) {
                    logger.info("restarting instance: " + i.getIdentifier());
                    sshService.startService(session, serviceType, i.getIdentifier());
                    logger.info("restarted  instance: " + i.getIdentifier());
                }

                sftpChannel.exit();
                sftpChannel.disconnect();
                logger.info("Deployed " + instanceType.getName() + " successfully to server: " + server.getServerName());
            } catch (SftpException | JSchException e) {
                logger.error("Could not deploy " + instanceType.getName() + " to server: " + server.getServerName(), e);
            }

        }
    }

    public void setInstanceType(InstanceType instanceType) {
        this.instanceType = instanceType;
    }
}
