package de.bruss.demontoo.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ServerService {
    @Autowired
    ServerRepository serverRepository;

    @Transactional
    public Server getOne(Long id) {
        Server server = serverRepository.getOne(id);
        return server;
    }

    @Transactional
    public void save(Server server) {
        serverRepository.save(server);
    }

    @Transactional
    public void create(Server server) {
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