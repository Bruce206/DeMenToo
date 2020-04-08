package de.bruss.demontoo.websockets;

import de.bruss.demontoo.instance.InstanceService;
import de.bruss.demontoo.instance.InstanceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class InstanceHealthChecker {
    @Autowired
    private InstanceService instanceService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private SessionListener sessionListener;

    @Autowired
    @Qualifier("HealthCheckTaskExcecuter")
    private TaskExecutor taskExecutor;

    @Autowired
    private ApplicationContext applicationContext;

    private final Logger logger = LoggerFactory.getLogger(InstanceHealthChecker.class);

    private Queue<Instance> instanceQueue = new LinkedBlockingDeque<>();

    public void addToQueue(Instance instance) {
        InstanceHealthCheckWorker worker = applicationContext.getBean(InstanceHealthCheckWorker.class);
        worker.setInstance(instance);

        taskExecutor.execute(worker);
        logger.debug("Instance-Thread added to Health-Check-Queue: " + instance.toString());
    }

    @Async
    @Scheduled(cron = "0 * * * * ?")
    public void checkHealthStatus() {
        logger.debug("Currently connected subscribers: " + sessionListener.getCurrentUsers());
        if (sessionListener.getCurrentUsers() > 0) {
            List<Instance> instances = instanceService.findByAndExcludeFromHealthcheckFalse();

            for (Instance i : instances) {
                addToQueue(i);
            }
        }
    }

    @Async
    public void checkHealthStatus(InstanceType type) {
        logger.debug("Currently connected subscribers: " + sessionListener.getCurrentUsers());
        if (sessionListener.getCurrentUsers() > 0) {
            List<Instance> instances = instanceService.findByInstanceTypeAndExcludeFromHealthcheckFalse(type);

            for (Instance i : instances) {
                addToQueue(i);
            }
        }
    }


}
