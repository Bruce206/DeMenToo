package de.bruss.demontoo.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public Instance getOne(Long id) {
        return instanceRepository.getOne(id);
    }

    @Transactional
    public Instance save(Instance instance) {
        Instance persistedInstance = instanceRepository.getOne(instance.getId());
        persistedInstance.setExcludeFromHealthcheck(instance.isExcludeFromHealthcheck());
        return persistedInstance;
    }

    @Transactional
    public void addToQueue(Instance instance) {
        InstanceWorker worker = applicationContext.getBean(InstanceWorker.class);
        worker.setInstance(instance);

        taskExecutor.execute(worker);
        logger.debug("Instance-Thread added to Queue: " + instance.toString());
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Deleting instance with id: " + id);
        Instance instance = instanceRepository.getOne(id);
        instance.getServer().removeInstance(instance);
        instance.setServer(null);

        instance.getInstanceType().removeInstance(instance);
        instance.setInstanceType(null);
        instanceRepository.delete(instance);
    }

    @Transactional
    public List<Instance> findAll() {
        return instanceRepository.findAll();
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