package de.bruss.demontoo.server;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import de.bruss.demontoo.server.configContainer.ApacheUrlConf;
import de.bruss.demontoo.server.configContainer.CombinedDomainContainer;
import de.bruss.demontoo.server.configContainer.XibisOneDomain;
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

    @GetMapping("/check-xibisone-domains/{id}")
    public Collection<XibisOneDomain> checkXibisOneDomains(@PathVariable Long id) throws JSchException {
        return serverService.checkXibisOneDomains(id);
    }

    @GetMapping("/check-combined-domains/{id}")
    public Collection<CombinedDomainContainer> checkCombinedDomainContainersForServers(@PathVariable Long id) throws JSchException, IOException, SftpException {
        return serverService.checkCombinedDomains(id);
    }

    @GetMapping("/check-combined-domains")
    public Collection<CombinedDomainContainer> checkCombinedDomainContainers() throws JSchException, IOException, SftpException {
        return serverService.checkCombinedDomains();
    }

    @GetMapping("/get-combined-domains")
    public Collection<CombinedDomainContainer> getCombinedDomainContainers() {
        return serverService.getCombinedDomains();
    }

    @GetMapping("/ping-{type}/{id}")
    public void pingApacheConfigs(@PathVariable Long id, @PathVariable String type) {
        switch (type) {
            case "apache":
                serverService.pingApacheConfigs(id);
                break;
            case "xibisone":
                serverService.pingXibisOneDomains(id);
                break;
            case "combined":
                serverService.pingAllDomainsAndSendToWebsocket(id);
                break;
        }
    }

    @GetMapping("/test-ssh-connection/{id}")
    public void testSSHConnection(@PathVariable Long id) throws JSchException {
        serverService.testSSHConnection(id);
    }

}
