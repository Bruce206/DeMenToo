package de.bruss.demontoo.instance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bruss.demontoo.domain.Domain;
import de.bruss.demontoo.server.Server;
import de.bruss.demontoo.server.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Controller
public class OldApiController {
    private static Logger logger = LoggerFactory.getLogger(OldApiController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    InstanceService instanceService;
    @Autowired
    private InstanceRepository instanceRepository;
    @Autowired
    private ServerRepository serverRepository;


    @RequestMapping(value = "/api/instances", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<Instance> addInstance(@RequestBody String json, HttpServletRequest request) throws IOException {
        JsonNode node = mapper.readTree(json);

        // dont show local pcs
        if (node.path("serverName").asText().startsWith("PC-")) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        Instance instance = new Instance();
        Server server;

        // check if server is already in database, if not add
        if ((server = serverRepository.findByIpAndServerName(request.getHeader("X-Forwarded-For"), node.path("serverName").asText())) == null) {
            server = new Server();
            server.setIp(request.getHeader("X-Forwarded-For"));
            server.setServerName(node.path("serverName").asText());
            serverRepository.save(server);
        }

        server.addInstance(instance);
        instance.setServer(server);
        instance.setType("XibisOne");
        instance.setIdentifier(node.path("base").asText());
        instance.setVersion(node.path("version").asText());
        instance.setLicensedFor(node.path("licensedFor").asText());
        instance.setProd(node.path("prod").asBoolean());

        // find & delete older reports
        List<Instance> previousInstanceEntities = instanceRepository.findByServerAndIdentifier(instance.getServer(), instance.getIdentifier());
        logger.info(instanceRepository.findByServerAndIdentifier(instance.getServer(), instance.getIdentifier()).size() + "");
        for (Instance instanceToRemove : previousInstanceEntities) {
            server.removeInstance(instanceToRemove);
        }
        instanceRepository.delete(previousInstanceEntities);

        for (JsonNode domainNode : node.path("domains")) {
            Domain domain = new Domain();
            domain.setName(domainNode.path("name").asText());
            domain.setNode(domainNode.path("id").asLong());
            domain.setUrl(domainNode.path("url").asText());
            domain.setInstance(instance);
            instance.addDomain(domain);
        }

        instanceRepository.save(instance);

        logger.info("Instanceupdate successful! [" + instance.toString() + "]");

        return new ResponseEntity<>(HttpStatus.CREATED);
    }


}

