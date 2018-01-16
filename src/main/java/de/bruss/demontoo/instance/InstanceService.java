package de.bruss.demontoo.instance;

import de.bruss.demontoo.server.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public void addToQueue(Instance instance) {
        this.service.submit(new InstanceWorker(instanceRepository, serverRepository, instance));
        logger.info("Instance-Thread added to Queue: " + instance.toString());
    }

    private ExecutorService service;

    @EventListener(ApplicationReadyEvent.class)
    public void startInstanceWorker() {
        logger.info("Starting Instance-Worker...");
        service = Executors.newSingleThreadExecutor();
        logger.info("Instance-Worker started!");
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