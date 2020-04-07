package de.bruss.demontoo.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/server")
public class ServerController {

    @Autowired
    private ServerService serverService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<Server> findAllServers() {
        return serverService.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Server getServer(@PathVariable long id) {
        return serverService.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteServer(@PathVariable long id) {
        serverService.delete(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void updateServer(@PathVariable long id, @RequestBody Server server) {
        serverService.save(server);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void saveServer(@RequestBody Server server) {
        serverService.save(server);
    }

}
