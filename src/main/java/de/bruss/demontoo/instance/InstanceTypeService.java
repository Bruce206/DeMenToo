package de.bruss.demontoo.instance;

import de.bruss.demontoo.ssh.SshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class InstanceTypeService {
    @Autowired
    private InstanceTypeRepository instancetypeRepository;

    @Value("${files.home}")
    private String filesHome;

    @Autowired
    private TaskScheduler taskExecutor;

    @Autowired
    private SshService sshService;

    private final Logger logger = LoggerFactory.getLogger(InstanceTypeService.class);

    private Map<String, ScheduledFuture> scheduledFutures = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void scheduleUpdateTasks() {
        logger.info("Scheduling Updates...");
        List<InstanceType> types = instancetypeRepository.findAll();

        for (InstanceType t : types) {
            if (t.getUpdateTime() != null && t.getUpdateTime().isAfter(ZonedDateTime.now()) && !StringUtils.isEmpty(t.getUpdateFileName())) {
                addUpdateTask(t);
            }
        }
    }

    @Transactional
    public InstanceType save(InstanceType instancetype) {
        InstanceType persistedType;
        if (instancetype.getId() != null) {
            persistedType = instancetypeRepository.findOne(instancetype.getId());
        } else {
            persistedType = instancetype;
        }

        persistedType.setMessageInterval(instancetype.getMessageInterval());
        persistedType.setUpdatePath(instancetype.getUpdatePath());
        persistedType.setAppType(instancetype.getAppType());

        persistedType.setApacheTemplate(instancetype.getApacheTemplate());
        persistedType.setServiceTemplate(instancetype.getServiceTemplate());
        persistedType.setCertbot(instancetype.getCertbot());
        persistedType.setApplicationPropertiesTemplate(instancetype.getApplicationPropertiesTemplate());

        if (instancetype.getUpdateTime() != null && instancetype.getUpdateTime().isAfter(ZonedDateTime.now())) {
            // if updateTime was changed, set new time and schedule update, else leave as is
            if (!instancetype.getUpdateTime().withSecond(0).withNano(0).equals(persistedType.getUpdateTime())) {
                persistedType.setUpdateTime(instancetype.getUpdateTime().withSecond(0).withNano(0));
                addUpdateTask(persistedType);
            }
        } else {
            persistedType.setUpdateTime(null);
            cancelUpdateTask(persistedType);
        }

        if (AppType.SPRING_BOOT.equals(persistedType.getAppType())) {
            persistedType.setHealthUrl("/management/health");
        } else {
            persistedType.setHealthUrl(instancetype.getHealthUrl());
        }

        return instancetypeRepository.save(persistedType);
    }

    private void cancelUpdateTask(InstanceType instanceType) {
        if (this.scheduledFutures.containsKey(instanceType.getName())) {
            this.scheduledFutures.get(instanceType.getName()).cancel(false);
            this.scheduledFutures.remove(instanceType.getName());
        }
    }

    @Transactional
    public void create(InstanceType instancetype) {
        instancetypeRepository.save(instancetype);
    }

    @Transactional
    public void delete(Long id) {
        instancetypeRepository.delete(id);
    }

    @Transactional
    public List<InstanceType> findAll() {
        return instancetypeRepository.findAll();
    }

    @Transactional
    public InstanceType findByName(String name) {
        return instancetypeRepository.findByName(name);
    }

    @Transactional
    public void setFile(Long id, byte[] bytes) {
        instancetypeRepository.findOne(id).setImage(bytes);
    }


    @Transactional
    public void setUpdateFile(Long id, MultipartFile file) throws IOException {
        InstanceType type = instancetypeRepository.findOne(id);

        if (!filesHome.endsWith("/")) {
            filesHome += "/";
        }

        file.transferTo(new File(filesHome + type.getName() + ".jar"));
        type.setUpdateFileName(file.getOriginalFilename());
    }

    private void addUpdateTask(InstanceType instanceType) {
        cancelUpdateTask(instanceType);

        logger.info("Scheduling update for " + instanceType.getName() + " on " + instanceType.getUpdateTime().toString());

        UpdateWorker updateWorker = new UpdateWorker(sshService, filesHome);
        updateWorker.setInstanceType(instanceType);
        ScheduledFuture future = taskExecutor.schedule(updateWorker, Date.from(instanceType.getUpdateTime().toInstant()));

        scheduledFutures.put(instanceType.getName(), future);
    }

    @Transactional
    public void installNewInstance(Long instanceTypeId, InstanceTypeController.InstanceInstallationRequest request) throws IOException {
        InstanceType instanceType = instancetypeRepository.findOne(instanceTypeId);

        String apacheTemplate = instanceType.getApacheTemplate();
        apacheTemplate = apacheTemplate.replace("[DOMAIN]", request.getDomain());
        apacheTemplate = apacheTemplate.replace("[PORT]", request.getPort());

        Files.write(Paths.get("C:/tmp/demontooTests/virt-" + request.getIdentifier() + ".conf"), apacheTemplate.getBytes());

        String applicationPropertiesTemplate = instanceType.getApplicationPropertiesTemplate();
        applicationPropertiesTemplate = applicationPropertiesTemplate.replace("[DOMAIN]", request.getDomain());
        applicationPropertiesTemplate = applicationPropertiesTemplate.replace("[PORT]", request.getPort());
        applicationPropertiesTemplate = applicationPropertiesTemplate.replace("[RAND]", randomString(32));
        applicationPropertiesTemplate = applicationPropertiesTemplate.replace("[CUSTOMER]", request.getCustomer());
        applicationPropertiesTemplate = applicationPropertiesTemplate.replace("[IDENTIFIER]", request.getIdentifier());

        for (InstanceTypeController.InstanceInstallationRequest.ApplicationProperty prop : request.getAdditionalApplicationProperties()) {
            applicationPropertiesTemplate = applicationPropertiesTemplate.replace("[ADD_" + prop.getKey() + "]", prop.getValue());
        }

        if (!StringUtils.isEmpty(request.getActiveSpringProfiles())) {
            applicationPropertiesTemplate = applicationPropertiesTemplate + "\nspring.profiles.active=" + request.getActiveSpringProfiles();
        }

        Files.write(Paths.get("C:/tmp/demontooTests/application.properties"), applicationPropertiesTemplate.getBytes());

        String serviceTemplate = instanceType.getServiceTemplate();
        serviceTemplate = serviceTemplate.replace("[IDENTIFIER]", request.getIdentifier());
        serviceTemplate = serviceTemplate.replace("[INSTANCE_TYPE_UPDATE_PATH]", instanceType.getUpdatePath());
        serviceTemplate = serviceTemplate.replace("[INSTANCE_TYPE_NAME]", instanceType.getName().toLowerCase());

        Files.write(Paths.get("C:/tmp/demontooTests/" + request.getIdentifier() + ".service"), serviceTemplate.getBytes());
    }

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!§$%&/()?#*+~:;_-";
    private static SecureRandom rnd = new SecureRandom();

    String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}