package de.bruss.demontoo.instance;

import de.bruss.demontoo.domain.Domain;
import de.bruss.demontoo.server.Server;
import de.bruss.demontoo.server.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = "prototype")
public class InstanceWorker implements Runnable {
    @Autowired
    private InstanceRepository instanceRepository;
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private InstanceTypeService instanceTypeService;

    private static Logger logger = LoggerFactory.getLogger(InstanceWorker.class);

    private Instance instance;

    @Override
    @Transactional
    public void run() {
        try {
            logger.debug("Updating instance [" + instance.toString() + "]...");

            // find matching server in database (by id and name)
//        Server server = serverRepository.findByIpAndServerName(instance.getServer().getIp(), instance.getServer().getServerName());

            Server server = null;
            List<Server> servers = serverRepository.findAll();
            for (Server s : servers) {
                if (s.getIp().equals(instance.getServer().getIp()) && s.getServerName().equals(instance.getServer().getServerName())) {
                    server = s;
                    break;
                }
            }

            logger.debug("Server found: " + (server != null ? server.toString() : "null"));

            // check if server is already in database, if not add
            if (server == null) {
                server = new Server();
                server.setIp(instance.getServer().getIp());
                server.setServerName(instance.getServer().getServerName());
                serverRepository.save(server);
                logger.debug("Server created: " + server.toString());
            }

            server.setLastMessage(LocalDateTime.now());

            logger.debug("Searching for Instance");
            Instance persistedInstance;
            Optional<Instance> persistedInstanceOpt = server.getInstances().stream().filter(i -> i.getIdentifier().equals(instance.getIdentifier())).findFirst();
            if (persistedInstanceOpt.isPresent()) {
                logger.debug("Instance found: " + persistedInstanceOpt.get().toString());
                persistedInstance = persistedInstanceOpt.get();
            } else {
                logger.debug("Instance created");
                persistedInstance = new Instance();
            }

            persistedInstance.setLastMessage(LocalDateTime.now());

            persistedInstance.setLicensedFor(instance.getLicensedFor());
            persistedInstance.setProd(instance.isProd());
            persistedInstance.setVersion(instance.getVersion());
            persistedInstance.setIdentifier(instance.getIdentifier());

            if (persistedInstance.getServer() != null) {
                if (!persistedInstance.getServer().getIp().equals(instance.getServer().getIp()) || !persistedInstance.getServer().getServerName().equals(instance.getServer().getServerName())) {
                    persistedInstance.getServer().removeInstance(persistedInstance);
                }
            }

            persistedInstance.setServer(server);
            server.addInstance(persistedInstance);

            persistedInstance.getDomains().clear();
            persistedInstance.getDomains().addAll(instance.getDomains());
            for (Domain domain : persistedInstance.getDomains()) {
                domain.setInstance(persistedInstance);
            }

            persistedInstance.getDetails().clear();
            persistedInstance.getDetails().addAll(instance.getDetails());
            for (InstanceDetail detail : persistedInstance.getDetails()) {
                detail.setInstance(persistedInstance);
            }

            if (!StringUtils.isEmpty(instance.getType())) {
                InstanceType persistedType = instanceTypeService.findByName(instance.getType());
                if (persistedType == null) {
                    persistedType = new InstanceType(instance.getType());
                    instanceTypeService.save(persistedType);
                }

                persistedType.getInstances().add(persistedInstance);
                persistedInstance.setInstanceType(persistedType);
                instanceTypeService.save(persistedType);
            }

            instanceRepository.save(persistedInstance);


            logger.debug("Instanceupdate successful! [" + instance.toString() + "]");
        } catch (Exception e) {
            logger.error("Error during instanceupdate", e);
        }
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}