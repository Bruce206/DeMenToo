package de.bruss.demontoo.instance;

import com.jcraft.jsch.*;
import de.bruss.demontoo.server.Server;
import de.bruss.demontoo.ssh.SshService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InstanceTypeService {
    @Autowired
    private InstanceTypeRepository instancetypeRepository;

    @Autowired
    private SshService sshService;

    private final Logger logger = LoggerFactory.getLogger(InstanceTypeService.class);

    @Transactional
    public InstanceType save(InstanceType instancetype) {
        InstanceType persistedType;
        if (instancetype.getId() != null) {
            persistedType = instancetypeRepository.findOne(instancetype.getId());
        } else {
            persistedType = instancetype;
        }

        persistedType.setMessageInterval(instancetype.getMessageInterval());
        persistedType.setUpdatePath(instancetype.getUpdatePath());
        return instancetypeRepository.save(persistedType);
    }

    @Transactional
    public void create(InstanceType instancetype) {
        instancetypeRepository.save(instancetype);
    }

    @Transactional
    public void delete(Long id) {
        instancetypeRepository.delete(id);
    }

    @Transactional
    public List<InstanceType> findAll() {
        return instancetypeRepository.findAll();
    }

    @Transactional
    public InstanceType findByName(String name) {
        return instancetypeRepository.findByName(name);
    }

    @Transactional
    public void setFile(Long id, byte[] bytes) {
        instancetypeRepository.findOne(id).setImage(bytes);
    }

    @Transactional
    public void setUpdateFile(Long id, byte[] bytes, String originalFilename) {
        InstanceType type = instancetypeRepository.findOne(id);
        type.setUpdate(bytes);
        type.setUpdateFileName(originalFilename);
    }

    @Transactional
    public void deploy(Long id) throws JSchException, SftpException {
        InstanceType instanceType = instancetypeRepository.findOne(id);

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

        Map<Server, List<Instance>> instancesByServer = instanceType.getInstances().stream().collect(Collectors.groupingBy(Instance::getServer));

        for (Server server : instancesByServer.keySet()) {
            List<Instance> instancesOnServer = instancesByServer.get(server);

            Session session = sshService.getSession(server.getIp());
            session.connect();

            ChannelSftp sftpChannel = sshService.getSftpChannel(session);

            sftpChannel.put(new ByteArrayInputStream(instanceType.getUpdate()), remotePath + instanceType.getName().toLowerCase() + ".jar", new SftpProgressMonitor() {

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

            for (Instance i : instancesOnServer) {
                logger.info("restarting instance: " + i.getIdentifier());
                sshService.sendCommand(session, "service " + i.getIdentifier() + " restart");
                logger.info("restarted  instance: " + i.getIdentifier());
            }

            sftpChannel.exit();
            sftpChannel.disconnect();

        }
    }



}