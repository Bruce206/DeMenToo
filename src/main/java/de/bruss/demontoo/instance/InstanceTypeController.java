package de.bruss.demontoo.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/instanceType")
public class InstanceTypeController {

    @Autowired
    private InstanceTypeService instancetypeService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<InstanceType> findAllInstanceTypes() {
        return instancetypeService.findAll();
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public InstanceType getInstanceType(@PathVariable String name) {
        return instancetypeService.findByName(name);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteInstanceType(@PathVariable long id) {
        instancetypeService.delete(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void updateInstanceType(@PathVariable long id, @RequestBody InstanceType instanceType) {
        instancetypeService.save(instanceType);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void saveInstanceType(@RequestBody InstanceType instanceType) {
        instancetypeService.save(instanceType);
    }

}
