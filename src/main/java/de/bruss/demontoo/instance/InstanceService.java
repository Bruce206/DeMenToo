package de.bruss.demontoo.instance;

import de.bruss.demontoo.domain.Domain;
import de.bruss.demontoo.server.Server;
import de.bruss.demontoo.server.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class InstanceService {
    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private ServerRepository serverRepository;

    private static Logger logger = LoggerFactory.getLogger(InstanceService.class);

    private BlockingQueue<Instance> instancesToAdd = new LinkedBlockingQueue<>();

    @Transactional
    public Instance findOne(Long id) {
        return instanceRepository.findOne(id);
    }

    @Transactional
    public Instance save(Instance instance) {
        return instanceRepository.save(instance);
    }

    public void addToQueue(Instance instance) {
        this.service.submit(new InstanceWorker(instance, this));
        logger.info("Instance added to Queue: " + instance.toString());
    }

    private ExecutorService service;

    @EventListener(ApplicationReadyEvent.class)
    public void startInstanceWorker() {
        logger.info("Starting Instance-Worker...");
        service = Executors.newSingleThreadExecutor();

//        InstanceWorker instanceWorker = new InstanceWorker(this.instancesToAdd, this);
//        Thread thread = new Thread(instanceWorker);
//        thread.start();
        logger.info("Instance-Worker started!");
    }

    /**
     * Updates / Creates an Instance after monitoring-request
     *
     * @param instance the instance to monitor
     * @return monitored instance
     */
    @Transactional
    public Instance update(Instance instance) {
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
            persistedInstance.setPaymentModel(instance.getPaymentModel());
            persistedInstance.setProd(instance.isProd());
            persistedInstance.setUsedSpaceInMB(instance.getUsedSpaceInMB());
            persistedInstance.setType(instance.getType());
            persistedInstance.setVersion(instance.getVersion());

            if (persistedInstance.getServer().getId() != instance.getServer().getId()) {
                persistedInstance.getServer().removeInstance(persistedInstance);
                persistedInstance.setServer(server);
                server.addInstance(persistedInstance);
            }

            persistedInstance.setLastMessage(LocalDateTime.now());

            persistedInstance.getDomains().clear();
            persistedInstance.setDomains(instance.getDomains());
            for (Domain domain : persistedInstance.getDomains()) {
                domain.setInstance(persistedInstance);
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

            instanceRepository.save(instance);
            logger.info("Instance created: " + instance.toString());
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