package de.bruss.demontoo.instance;

import de.bruss.demontoo.websockets.InstanceHealthChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/api/instance")
public class InstanceController {

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private InstanceTypeService instanceTypeService;

    @Autowired
    private InstanceHealthChecker instanceHealthChecker;

    @Autowired
    private InstanceChecker instanceChecker;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<Instance> findAllInstances() {
        return instanceService.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Instance getInstance(@PathVariable long id) {
        return instanceService.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteInstance(@PathVariable long id) {
        instanceService.delete(id);
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void updateInstance(@RequestBody Instance instance) {
        instanceService.save(instance);
    }

    @RequestMapping(value = "refresh", method = RequestMethod.GET)
    public void refreshHealthChecks() {
        instanceHealthChecker.checkHealthStatus();
    }

    @RequestMapping(value = "refresh/{type}", method = RequestMethod.GET)
    public void refreshHealthChecks(@PathVariable String type) {
        instanceHealthChecker.checkHealthStatus(instanceTypeService.findByName(type));
    }

    /**
     * takes the reports from instances and stores them in database, replaces older entries of instancereports
     */
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> createInstance(@RequestBody Instance instance, HttpServletRequest request) {
        // dont show local pcs
        if (instance.getServer().getServerName().startsWith("PC-") || instance.getServer().getServerName().startsWith("pc-") || instance.getServer().getServerName().startsWith("DESKTOP-")) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        String remoteAddr = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(remoteAddr)) {
            remoteAddr = request.getRemoteAddr();
        }
        instance.getServer().setIp(remoteAddr);

        instanceService.addToQueue(instance);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

//    @RequestMapping(value = "/test", method = RequestMethod.GET)
//    public ResponseEntity<String> createDummyInstance() {
//        Instance instance = new Instance();
//
//        instance.setType("SkinGo");
//        instance.setIdentifier("IDENT1");
//        instance.getDetails().add(new InstanceDetail("cat1", "key1", "val1"));
//        instance.getDetails().add(new InstanceDetail("cat1", "key2", "val2"));
//        instance.getDetails().add(new InstanceDetail("cat2", "key3", "val3"));
//        instance.getDetails().add(new InstanceDetail("cat2", "key4", "val4"));
//        instance.getDomains().add(new Domain("domain1", "domain1.localhost"));
//        instance.setServer(new Server("LOCAL", "127.0.0.1"));
//
//        instanceService.addToQueue(instance);
//        return new ResponseEntity<>(HttpStatus.CREATED);
//    }

    @RequestMapping(value = "/checkActive", method = RequestMethod.GET)
    public void checkActive() {
        instanceChecker.activeCheck();
    }
}
