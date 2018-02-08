package de.bruss.demontoo.websockets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bruss.demontoo.instance.Instance;
import de.bruss.demontoo.instance.InstanceService;
import de.bruss.demontoo.instance.InstanceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
        logger.info("Instance-Thread added to Health-Check-Queue: " + instance.toString());
    }

    @Async
    @Scheduled(cron = "0 * * * * ?")
    public void checkHealthStatus() {
        logger.info("Currently connected subscribers: " + sessionListener.getCurrentUsers());
        if (sessionListener.getCurrentUsers() > 0) {
            List<Instance> instances = instanceService.findAll();

            for (Instance i : instances) {
                addToQueue(i);
            }
        }
    }

    @Async
    public void checkHealthStatus(InstanceType type) {
        logger.info("Currently connected subscribers: " + sessionListener.getCurrentUsers());
        if (sessionListener.getCurrentUsers() > 0) {
            List<Instance> instances = instanceService.findByType(type);

            for (Instance i : instances) {
                addToQueue(i);
            }
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class InstanceHealthMessage {
        @JsonIgnoreProperties("domains")
        private Instance instance;
        private String status;
        private Integer responseTime;
    }
}
