package de.bruss.demontoo.instance;

import com.jcraft.jsch.*;
import de.bruss.demontoo.ssh.SshService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.util.List;

@Service
public class InstanceTypeService {
    @Autowired
    private InstanceTypeRepository instancetypeRepository;

    @Autowired
    private SshService sshService;

    private final Logger logger = LoggerFactory.getLogger(InstanceTypeService.class);

    @Transactional
    public InstanceType findOne(Long id) {
        return instancetypeRepository.findOne(id);
    }

    @Transactional
    public InstanceType save(InstanceType instancetype) {
        InstanceType persistedType = instancetypeRepository.findOne(instancetype.getId());
        persistedType.setMessageInterval(instancetype.getMessageInterval());
        return persistedType;
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
    public void setUpdateFile(Long id, byte[] bytes) {
        instancetypeRepository.findOne(id).setUpdate(bytes);
    }

    @Transactional
    public void deploy(Long id) throws JSchException, SftpException {
        Session session = sshService.getSession("http://brucenet.de");
        session.connect();

        ChannelSftp sftpChannel = sshService.getSftpChannel(session);

        InstanceType instanceType = instancetypeRepository.findOne(id);

        sftpChannel.put(new ByteArrayInputStream(instanceType.getUpdate()), "/tmp/test.jar", new SftpProgressMonitor() {

            private double bytes;
            private double max;

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
                logger.info("Bytes transferred: " + FileUtils.byteCountToDisplaySize(bytes));
                return true;
            }
        });

        sftpChannel.exit();
        sftpChannel.disconnect();
    }
}