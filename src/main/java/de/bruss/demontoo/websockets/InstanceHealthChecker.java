package de.bruss.demontoo.websockets;

import de.bruss.demontoo.instance.Instance;
import de.bruss.demontoo.instance.InstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InstanceHealthChecker {
    @Autowired
    private InstanceService instanceService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private SessionListener sessionListener;

    private final Logger logger = LoggerFactory.getLogger(InstanceHealthChecker.class);

    @Transactional
    @Async
    @Scheduled(cron = "59 * * * * ?")
    public void checkHealthStatus() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Currently connected subscribers: " + sessionListener.getCurrentUsers());
        if (sessionListener.getCurrentUsers() > 0) {
            RestTemplate restTemplate = new RestTemplate();
            List<Instance> instances = instanceService.findAll();

            for (Instance i : instances) {
                if (!i.getDomains().isEmpty() && i.getInstanceType().getHealthUrl() != null) {
                    String instanceUrl = i.getDomains().get(0).getUrl();
                    if (!instanceUrl.startsWith("http")) {
                        instanceUrl = "http://" + instanceUrl;
                    }

                    logger.info("checking health for: " + instanceUrl + i.getInstanceType().getHealthUrl());
                    try {
                        int tries = 0;
                        HttpStatus status = HttpStatus.MOVED_PERMANENTLY;
                        String url = instanceUrl + i.getInstanceType().getHealthUrl();
                        while (HttpStatus.MOVED_PERMANENTLY.equals(status) && tries++ < 10) {
                            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                            status = response.getStatusCode();
                            if (HttpStatus.MOVED_PERMANENTLY.equals(status)) {
                                url = response.getHeaders().getLocation().toString();
                                template.convertAndSend("/instancestatus", new InstanceService.InstanceHealthMessage(i, response.getStatusCode()));
                            } else {
                                if (HttpStatus.OK.equals(status)) {
                                    i.setLastMessage(LocalDateTime.now());
                                }
                                template.convertAndSend("/instancestatus", new InstanceService.InstanceHealthMessage(i, response.getStatusCode()));
                            }
                        }

                        if (tries == 10) {
                            template.convertAndSend("/instancestatus", new InstanceService.InstanceHealthMessage(i, HttpStatus.PERMANENT_REDIRECT));
                        }
                    } catch (ResourceAccessException | HttpClientErrorException rea) {
                        template.convertAndSend("/instancestatus", new InstanceService.InstanceHealthMessage(i, HttpStatus.NOT_FOUND));
                    }
                }
            }
        }
    }
}
