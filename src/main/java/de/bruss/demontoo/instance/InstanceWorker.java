package de.bruss.demontoo.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.BlockingQueue;

public class InstanceWorker implements Runnable {
    protected InstanceService instanceService;

    protected static Logger logger = LoggerFactory.getLogger(InstanceWorker.class);

    protected BlockingQueue<Instance> queue;

    public InstanceWorker(BlockingQueue<Instance> queue, InstanceService instanceService) {
        super();
        this.queue = queue;
        this.instanceService = instanceService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void run() {
        while (!Thread.interrupted()) {
            logger.info("Checking Queue... Found: " + queue.size() + " elements in queue.");
            if (!queue.isEmpty()) {
                Instance instance;

                try {
                    instance = queue.take();
                    instanceService.update(instance);
                } catch (InterruptedException e) {
                    logger.error("Error occured while waiting for Queue", e);
                }
            } else {
                try {
                    logger.info("Found no elements in queue. Sleeping for a minute...");
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    logger.error("Sleeping IndexWorker interrupted", e);
                }
            }
        }
    }
}
