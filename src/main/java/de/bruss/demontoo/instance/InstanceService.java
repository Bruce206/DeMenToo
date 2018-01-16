package de.bruss.demontoo.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    public Instance findOne(Long id) {
        return instanceRepository.findOne(id);
    }

    @Transactional
    public Instance save(Instance instance) {
        return instanceRepository.save(instance);
    }

    public void addToQueue(Instance instance) {
        InstanceWorker worker = applicationContext.getBean(InstanceWorker.class);
        worker.setInstance(instance);

        taskExecutor.execute(worker);
        logger.info("Instance-Thread added to Queue: " + instance.toString());
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