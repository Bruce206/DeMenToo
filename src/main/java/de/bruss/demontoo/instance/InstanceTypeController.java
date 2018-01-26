package de.bruss.demontoo.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/instanceType")
public class InstanceTypeController {

    @Autowired
    private InstanceTypeService instancetypeService;

    private final Logger logger = LoggerFactory.getLogger(InstanceTypeController.class);

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
    public InstanceType updateInstanceType(@PathVariable long id, @RequestBody InstanceType instanceType) {
        return instancetypeService.save(instanceType);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public InstanceType saveInstanceType(@RequestBody InstanceType instanceType) {
        return instancetypeService.save(instanceType);
    }

    @RequestMapping(value="/image/{id}", method = RequestMethod.POST)
    public void setImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        instancetypeService.setFile(id, file.getBytes());
    }
}
