package de.bruss.demontoo.instance;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bruss.demontoo.server.Server;
import de.bruss.demontoo.server.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
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
        logger.info("Instance added to Queue: " + instance.toString());
        this.instancesToAdd.add(instance);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        logger.info("Starting Instance-Worker...");
        InstanceWorker instanceWorker = new InstanceWorker(this.instancesToAdd, this);
        Thread thread = new Thread(instanceWorker);
        thread.start();
        logger.info("Instance-Worker started!");
    }

    /**
     * Updates / Creates an Instance after monitoring-request
     * @param instance
     * @return
     */
    @Transactional
    public Instance update(Instance instance) {
        // find matching server in database (by id and name)
        Server server = serverRepository.findByIpAndServerName(instance.getServer().getIp(), instance.getServer().getServerName());

        // check if server is already in database, if not add
        if (server == null) {
            server = new Server();
            server.setIp(instance.getServer().getIp());
            server.setServerName(instance.getServer().getServerName());
            serverRepository.save(server);
        }

        Optional<Instance> persistedInstance = server.getInstances().stream().filter(i -> i.getIdentifier().equals(instance.getIdentifier())).findFirst();
        if (persistedInstance.isPresent()) {
            BeanUtils.copyProperties(instance, persistedInstance, "id", "server");
        } else {
            server.addInstance(instance);
            instance.setServer(server);
            instanceRepository.save(instance);

        }

        logger.info("Instanceupdate successful! [" + instance.toString() + "]");

        return instance;
    }

    private ObjectMapper mapper = new ObjectMapper();

    @Transactional
    public void delete(Long id) {
        instanceRepository.delete(id);
    }

    @Transactional
    public List<Instance> findAll() {
        return instanceRepository.findAll();
    }

}