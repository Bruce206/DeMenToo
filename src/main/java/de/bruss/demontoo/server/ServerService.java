package de.bruss.demontoo.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ServerService {
    private final ServerRepository serverRepository;

    @Autowired
    public ServerService(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
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

}