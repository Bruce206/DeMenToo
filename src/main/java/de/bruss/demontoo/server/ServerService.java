package de.bruss.demontoo.server;

import de.bruss.demontoo.instance.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ServerService {
    private final ServerRepository serverRepository;
    private final InstanceService instanceService;

    @Autowired
    public ServerService(ServerRepository serverRepository, InstanceService instanceService) {
        this.serverRepository = serverRepository;
        this.instanceService = instanceService;
    }

    @Transactional
    public Server findOne(Long id) {
        return serverRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public void save(Server server) {
        serverRepository.save(server);
    }

    @Transactional
    public void delete(Long id) {
        serverRepository.deleteById(id);
    }

    @Transactional
    public List<Server> findAll() {
        return serverRepository.findAll();
    }

    @Transactional
    public List<Server> findAllNonBlacklisted() {
        return serverRepository.findAllByBlacklistedIsFalse();
    }

    @Transactional
    public void cleanUp() {
        List<Server> servers = findAll();

        List<Server> serversToCleanUp = servers.stream().filter(s -> s.getInstances().isEmpty() || s.getModified().isBefore(LocalDateTime.now().minusMonths(3))).collect(Collectors.toList());

        for (Server server : serversToCleanUp) {
            server.getInstances().forEach(i -> i.setServer(null));
            instanceService.deleteAll(server.getInstances());
            server.getInstances().clear();
        }

        serverRepository.deleteAll(serversToCleanUp);
    }

    @Transactional
    public void blacklist(long id) {
        Server server = serverRepository.getOne(id);
        server.setBlacklisted(true);
        server.setWhitelisted(false);
        server.setActiveCheckDisabled(true);
    }
}