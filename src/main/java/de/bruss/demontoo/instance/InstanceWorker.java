package de.bruss.demontoo.instance;

import de.bruss.demontoo.domain.Domain;
import de.bruss.demontoo.server.Server;
import de.bruss.demontoo.server.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class InstanceWorker implements Runnable {
    protected InstanceRepository instanceRepository;
    protected ServerRepository serverRepository;

    protected static Logger logger = LoggerFactory.getLogger(InstanceWorker.class);

    protected Instance instance;

    public InstanceWorker(InstanceRepository instanceRepository, ServerRepository serverRepository, Instance instance) {
        super();
        this.instanceRepository = instanceRepository;
        this.serverRepository = serverRepository;
        this.instance = instance;
    }

    @Override
    @Transactional
    public void run() {
        try {
            logger.info("Updating instance [" + instance.toString() + "]...");

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

            logger.info("Server found: " + (server != null ? server.toString() : "null"));

            // check if server is already in database, if not add
            if (server == null) {
                server = new Server();
                server.setIp(instance.getServer().getIp());
                server.setServerName(instance.getServer().getServerName());
                serverRepository.save(server);
                logger.info("Server created: " + server.toString());
            }

            logger.info("Searching for Instance");
            Optional<Instance> persistedInstanceOpt = server.getInstances().stream().filter(i -> i.getIdentifier().equals(instance.getIdentifier())).findFirst();
            if (persistedInstanceOpt.isPresent()) {
                logger.info("Instance found: " + persistedInstanceOpt.get().toString());
                Instance persistedInstance = persistedInstanceOpt.get();
                persistedInstance.setDomains(instance.getDomains());
                persistedInstance.setLicensedFor(instance.getLicensedFor());
                persistedInstance.setProd(instance.isProd());
                persistedInstance.setType(instance.getType());
                persistedInstance.setVersion(instance.getVersion());

                if (persistedInstance.getServer().getId() != instance.getServer().getId()) {
                    persistedInstance.getServer().removeInstance(persistedInstance);
                    persistedInstance.setServer(server);
                    server.addInstance(persistedInstance);
                }

                persistedInstance.setLastMessage(LocalDateTime.now());

                persistedInstance.setDomains(instance.getDomains());
                for (Domain domain : persistedInstance.getDomains()) {
                    domain.setInstance(persistedInstance);
                }

                persistedInstance.setDetails(instance.getDetails());
                for (InstanceDetail detail : persistedInstance.getDetails()) {
                    detail.setInstance(persistedInstance);
                }

                instanceRepository.save(persistedInstance);
            } else {
                logger.info("Instance not found. Creating a new one..");
                instance.setLastMessage(LocalDateTime.now());
                server.addInstance(instance);
                instance.setServer(server);

                for (Domain domain : instance.getDomains()) {
                    domain.setInstance(instance);
                }

                for (InstanceDetail detail : instance.getDetails()) {
                    detail.setInstance(instance);
                }

                instanceRepository.save(instance);
                logger.info("Instance created: " + instance.toString());
            }

            logger.info("Instanceupdate successful! [" + instance.toString() + "]");
        } catch (Exception e) {
            logger.error("Error during instanceupdate", e);
        }
    }
}