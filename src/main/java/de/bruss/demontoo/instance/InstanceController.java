package de.bruss.demontoo.instance;

import org.springframework.beans.factory.annotation.Autowired;
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
    public String createInstance(@RequestBody Instance instance, HttpServletRequest request) {
        instanceService.save(instance, request);
        return "OK";
    }

}
