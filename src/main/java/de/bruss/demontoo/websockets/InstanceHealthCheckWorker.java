package de.bruss.demontoo.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Scope("prototype")
public class InstanceHealthCheckWorker implements Runnable {

    private Instance instance;

    @Autowired
    public InstanceHealthCheckWorker(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    private final SimpMessagingTemplate template;

    private final Logger logger = LoggerFactory.getLogger(InstanceHealthCheckWorker.class);

    @Override
    @Transactional
    public void run() {
        if (!instance.getDomains().isEmpty() && instance.getDomains().get(0) != null && !StringUtils.isEmpty(instance.getDomains().get(0).getUrl()) && instance.getInstanceType() != null && instance.getInstanceType().getHealthUrl() != null) {
            int timeout = 10000;
            HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(timeout);
            clientHttpRequestFactory.setReadTimeout(timeout);

            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
            
            String instanceUrl = instance.getDomains().get(0).getUrl();
            if (!instanceUrl.startsWith("http")) {
                instanceUrl = "http://" + instanceUrl;
            }

            logger.debug("checking health for: " + instanceUrl + instance.getInstanceType().getHealthUrl());
            int responseTime = 0;
            try {
                int tries = 0;
                HttpStatus status = HttpStatus.MOVED_PERMANENTLY;
                String url = instanceUrl + instance.getInstanceType().getHealthUrl();
                while (HttpStatus.MOVED_PERMANENTLY.equals(status) && tries++ < 10) {
                    LocalDateTime start = LocalDateTime.now();
                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                    responseTime += start.until(LocalDateTime.now(), ChronoUnit.MILLIS);
                    status = response.getStatusCode();
                    if (HttpStatus.MOVED_PERMANENTLY.equals(status)) {
                        url = response.getHeaders().getLocation().toString();
                        template.convertAndSend("/instancestatus", new InstanceHealthMessage(instance, response.getStatusCode().getReasonPhrase(), responseTime));
                    } else {
                        if (HttpStatus.OK.equals(status)) {
                            instance.setLastMessage(LocalDateTime.now());
                        }
                        template.convertAndSend("/instancestatus", new InstanceHealthMessage(instance, response.getStatusCode().getReasonPhrase(), responseTime));
                    }
                }

                if (tries == 10) {
                    template.convertAndSend("/instancestatus", new InstanceHealthMessage(instance, "Too many Redirects", responseTime));
                }
            } catch (ResourceAccessException ste) {
                if (ste.getMessage().contains("timed out")) {
                    template.convertAndSend("/instancestatus", new InstanceHealthMessage(instance, "Timeout", responseTime));
                } else if (ste.getMessage().contains("Connection refused")) {
                    template.convertAndSend("/instancestatus", new InstanceHealthMessage(instance, "Connection refused", responseTime));
                } else {
                    template.convertAndSend("/instancestatus", new InstanceHealthMessage(instance, ste.getMessage(), responseTime));
                }
            } catch (HttpClientErrorException | HttpServerErrorException hcee) {
                template.convertAndSend("/instancestatus", new InstanceHealthMessage(instance, hcee.getMessage(), responseTime));
            } catch (Exception e) {
                logger.warn("Error occured while fetching " + instance.getIdentifier() + " [" + instance.getDomains().get(0).getUrl() + "]", e);
                template.convertAndSend("/instancestatus", new InstanceHealthMessage(instance, "Error occured", responseTime));
            }
        } else {
            template.convertAndSend("/instancestatus", new InstanceHealthMessage(instance, "Missing domain or instanceType", 0));
        }

    }
}
