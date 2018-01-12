package de.bruss.demontoo.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/api/instance")
public class InstanceController {

    @Autowired
    private InstanceService instanceService;

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

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void updateInstance(@PathVariable long id, @RequestBody Instance instance) {
        instanceService.save(instance);
    }

    /**
     * takes the reports from instances and stores them in database, replaces older entries of instancereports
     */
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> createInstance(@RequestBody Instance instance, HttpServletRequest request) {
        // dont show local pcs
//        if (instance.getServer().getServerName().startsWith("PC-") || instance.getServer().getServerName().startsWith("DESKTOP-")) {
//            return new ResponseEntity<>(HttpStatus.CREATED);
//        }

        instance.getServer().setIp(request.getHeader("X-Forwarded-For"));

        instanceService.addToQueue(instance);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
