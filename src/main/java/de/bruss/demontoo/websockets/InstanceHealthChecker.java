package de.bruss.demontoo.websockets;

import de.bruss.demontoo.instance.Instance;
import de.bruss.demontoo.instance.InstanceService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    @Scheduled(cron = "59 */5 * * * ?")
    public void checkHealthStatus() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Currently connected subscribers: " + sessionListener.getCurrentUsers());
        if (sessionListener.getCurrentUsers() > 0) {
            int timeout = 500;
            HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(timeout);
            clientHttpRequestFactory.setReadTimeout(timeout);

            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
            List<Instance> instances = instanceService.findAll();

            for (Instance i : instances) {
                if (!i.getDomains().isEmpty() && i.getDomains().get(0) != null && !StringUtils.isEmpty(i.getDomains().get(0).getUrl()) && i.getInstanceType().getHealthUrl() != null) {
                    String instanceUrl = i.getDomains().get(0).getUrl();
                    if (!instanceUrl.startsWith("http")) {
                        instanceUrl = "http://" + instanceUrl;
                    }

                    logger.info("checking health for: " + instanceUrl + i.getInstanceType().getHealthUrl());
                    int responseTime = 0;
                    try {
                        int tries = 0;
                        HttpStatus status = HttpStatus.MOVED_PERMANENTLY;
                        String url = instanceUrl + i.getInstanceType().getHealthUrl();
                        while (HttpStatus.MOVED_PERMANENTLY.equals(status) && tries++ < 10) {
                            LocalDateTime start = LocalDateTime.now();
                            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                            responseTime += start.until(LocalDateTime.now(), ChronoUnit.MILLIS);
                            status = response.getStatusCode();
                            if (HttpStatus.MOVED_PERMANENTLY.equals(status)) {
                                url = response.getHeaders().getLocation().toString();
                                template.convertAndSend("/instancestatus", new InstanceHealthMessage(i, response.getStatusCode().getReasonPhrase(), responseTime));
                            } else {
                                if (HttpStatus.OK.equals(status)) {
                                    i.setLastMessage(LocalDateTime.now());
                                }
                                template.convertAndSend("/instancestatus", new InstanceHealthMessage(i, response.getStatusCode().getReasonPhrase(), responseTime));
                            }
                        }

                        if (tries == 10) {
                            template.convertAndSend("/instancestatus", new InstanceHealthMessage(i, "Too many Redirects", responseTime));
                        }
                    } catch (ResourceAccessException ste) {
                        if (ste.getMessage().contains("timed out")) {
                            template.convertAndSend("/instancestatus", new InstanceHealthMessage(i, "Timeout", responseTime));
                        } else {
                            template.convertAndSend("/instancestatus", new InstanceHealthMessage(i, ste.getMessage(), responseTime));
                        }
                    } catch (HttpClientErrorException | HttpServerErrorException hcee) {
                        template.convertAndSend("/instancestatus", new InstanceHealthMessage(i, hcee.getMessage(), responseTime));
                    } catch (Exception e) {
                        logger.warn("Error occured while fetching " + i.getIdentifier() + " [" + i.getDomains().get(0).getUrl() + "]", e);
                        template.convertAndSend("/instancestatus", new InstanceHealthMessage(i, "Error occured", responseTime));
                    }
                } else {
                    template.convertAndSend("/instancestatus", new InstanceHealthMessage(i, "No Domain", 0));
                }
            }
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class InstanceHealthMessage {
        private Instance instance;
        private String status;
        private Integer responseTime;
    }
}
