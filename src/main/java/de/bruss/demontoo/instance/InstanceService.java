package de.bruss.demontoo.instance;

import de.bruss.demontoo.server.Server;
import de.bruss.demontoo.server.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class InstanceService {
    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private ServerRepository serverRepository;

    private static Logger logger = LoggerFactory.getLogger(InstanceService.class);

    @Transactional
    public Instance findOne(Long id) {
        return instanceRepository.findOne(id);
    }

    @Transactional
    public Instance save(Instance instance) {
        return instanceRepository.save(instance);
    }

    @Transactional
    public Instance save(Instance instance, HttpServletRequest request) {
        // dont show local pcs
        if (instance.getServer().getServerName().startsWith("PC-")) {
            return null;
        }

        Server server;

        // check if server is already in database, if not add
        if ((server = serverRepository.findByIpAndServerName(request.getHeader("X-Forwarded-For"), instance.getServer().getServerName())) == null) {
            server = new Server();
            server.setIp(request.getHeader("X-Forwarded-For"));
            server.setServerName(instance.getServer().getServerName());
            serverRepository.save(server);
        }

        Optional<Instance> persistedInstance = server.getInstances().stream().filter(i -> i.getIdentifier().equals(instance.getIdentifier())).findFirst();
        if (persistedInstance.isPresent()) {
            BeanUtils.copyProperties(instance, persistedInstance, "id", "server");
        } else {
            instanceRepository.save(instance);
            server.addInstance(instance);
            instance.setServer(server);
        }

        logger.info("Instanceupdate successful! [" + instance.toString() + "]");

        return instance;
    }

    @Transactional
    public void delete(Long id) {
        instanceRepository.delete(id);
    }

    @Transactional
    public List<Instance> findAll() {
        return instanceRepository.findAll();
    }

}