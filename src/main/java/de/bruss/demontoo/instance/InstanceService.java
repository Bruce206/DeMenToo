package de.bruss.demontoo.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class InstanceService {
    @Autowired
    private InstanceRepository instanceRepository;

    private static Logger logger = LoggerFactory.getLogger(InstanceService.class);

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private ApplicationContext applicationContext;

    @Transactional
    public Instance findOne(Long id) {
        return instanceRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public Instance save(Instance instance) {
        Instance persistedInstance = instanceRepository.findById(instance.getId()).orElseThrow(NoSuchElementException::new);
        persistedInstance.setExcludeFromHealthcheck(instance.isExcludeFromHealthcheck());
        return persistedInstance;
    }

    @Transactional
    public void addToQueue(Instance instance) {
        InstanceWorker worker = applicationContext.getBean(InstanceWorker.class);
        worker.setInstance(instance);

        taskExecutor.execute(worker);
        logger.info("Instance-Thread added to Queue: " + instance.toString());
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Deleting instance with id: " + id);
        Instance instance = instanceRepository.findById(id).orElseThrow(NoSuchElementException::new);
        if (instance.getServer() != null) {
            instance.getServer().removeInstance(instance);
            instance.setServer(null);
        }

        if (instance.getInstanceType() != null) {
            instance.getInstanceType().removeInstance(instance);
            instance.setInstanceType(null);
        }
        instanceRepository.delete(instance);
    }

    @Transactional
    public List<Instance> findAll() {
        return instanceRepository.findAll().stream().sorted((a, b) -> {
            if (b.isExcludeFromHealthcheck() || b.getIdentifier() == null) {
                return -1;
            }

            if (a.isExcludeFromHealthcheck() || a.getIdentifier() == null) {
                return 1;
            }

            return a.getIdentifier().toLowerCase().compareTo(b.getIdentifier().toLowerCase());
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<Instance> findByType(InstanceType type) {
        return instanceRepository.findByInstanceType(type);
    }

    @Transactional
    public List<Instance> findByAndExcludeFromHealthcheckFalse() {
        return instanceRepository.findByAndExcludeFromHealthcheckFalse();
    }

    @Transactional
    public List<Instance> findByInstanceTypeAndExcludeFromHealthcheckFalse(InstanceType type) {
        return instanceRepository.findByInstanceTypeAndAndExcludeFromHealthcheckFalse(type);
    }

}