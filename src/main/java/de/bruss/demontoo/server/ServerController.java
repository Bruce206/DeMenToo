package de.bruss.demontoo.server;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/server")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping
    public List<Server> findAllServers() {
        return serverService.findAllNonBlacklisted();
    }

    @GetMapping("/{id}")
    public Server getServer(@PathVariable long id) {
        return serverService.findOne(id);
    }

    @DeleteMapping("/{id}")
    public void deleteServer(@PathVariable long id) {
        serverService.delete(id);
    }

    @PostMapping("/blacklist/{id}")
    public void blacklistServer(@PathVariable long id) {
        serverService.blacklist(id);
    }

    @PutMapping("/{id}")
    public void updateServer(@PathVariable long id, @RequestBody Server server) {
        serverService.save(server);
    }

    @PostMapping
    public void saveServer(@RequestBody Server server) {
        serverService.save(server);
    }

    @PostMapping("/clean-up")
    public void cleanUpServers() {
        serverService.cleanUp();
    }

}
