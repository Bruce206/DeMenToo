package de.bruss.demontoo.instance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bruss.demontoo.domain.Domain;
import de.bruss.demontoo.server.Server;
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

@Controller
public class OldApiController {
    private static Logger logger = LoggerFactory.getLogger(OldApiController.class);

    @Autowired
    InstanceService instanceService;

    ObjectMapper mapper = new ObjectMapper();

    @Deprecated
    @RequestMapping(value = "/api/instances", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<Instance> addInstance(@RequestBody String json, HttpServletRequest request) throws IOException {
        JsonNode node = mapper.readTree(json);

        // dont show local pcs
        if (node.path("serverName").asText().startsWith("PC-") || node.path("serverName").asText().startsWith("pc-") || node.path("serverName").asText().startsWith("DESKTOP-")) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        Instance instance = new Instance();
        Server server;

        // check if server is already in database, if not add
        server = new Server();
        server.setIp(request.getHeader("X-Forwarded-For"));
        server.setServerName(node.path("serverName").asText());

        server.addInstance(instance);
        instance.setServer(server);
        instance.setType("XibisOne");
        instance.setIdentifier(node.path("base").asText());
        instance.setVersion(node.path("version").asText());
        instance.setLicensedFor(node.path("licensedFor").asText());
        instance.setProd(node.path("prod").asBoolean());

        for (JsonNode domainNode : node.path("domains")) {
            Domain domain = new Domain();
            domain.setName(domainNode.path("name").asText());
            domain.setUrl(domainNode.path("url").asText());
            domain.setInstance(instance);
            instance.addDomain(domain);
        }

        instanceService.addToQueue(instance);

        logger.info("Instance added to Queue! [" + instance.toString() + "]");

        return new ResponseEntity<>(HttpStatus.CREATED);
    }


}

