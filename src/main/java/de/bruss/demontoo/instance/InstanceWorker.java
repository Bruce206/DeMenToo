package de.bruss.demontoo.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class InstanceWorker implements Runnable {
    protected InstanceService instanceService;

    protected static Logger logger = LoggerFactory.getLogger(InstanceWorker.class);

    protected Instance instance;

    public InstanceWorker(Instance instance, InstanceService instanceService) {
        super();
        this.instance = instance;
        this.instanceService = instanceService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void run() {
        try {
            instanceService.update(instance);
        } catch (Exception e) {
            logger.error("Error during instanceupdate", e);
        }
    }
}