package de.bruss.demontoo.server;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import de.bruss.demontoo.server.configContainer.ApacheUrlConf;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;
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

    @GetMapping("/check-apache-confs/{id}")
    public Collection<ApacheUrlConf> checkApacheConfigs(@PathVariable Long id) throws JSchException, SftpException, IOException {
        return serverService.checkApacheConfigs(id);
    }

    @GetMapping("/ping-apache-confs/{id}")
    public void pingApacheConfigs(@PathVariable Long id) {
        serverService.pingApacheConfigs(id);
    }

    @GetMapping("/test-ssh-connection/{id}")
    public void testSSHConnection(@PathVariable Long id) throws JSchException {
        serverService.testSSHConnection(id);
    }

}
