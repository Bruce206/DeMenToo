package de.bruss.demontoo.server;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import de.bruss.demontoo.instance.InstanceService;
import de.bruss.demontoo.server.configContainer.ApacheUrlConf;
import de.bruss.demontoo.ssh.SshService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServerService {
    Logger logger = LoggerFactory.getLogger(ServerService.class);

    private final ServerRepository serverRepository;
    private final InstanceService instanceService;
    private final SshService sshService;
    private final SimpMessagingTemplate template;

    @Autowired
    public ServerService(ServerRepository serverRepository, InstanceService instanceService, SshService sshService, SimpMessagingTemplate template) {
        this.serverRepository = serverRepository;
        this.instanceService = instanceService;
        this.sshService = sshService;
        this.template = template;
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
        return serverRepository.findAllByBlacklistedIsFalseOrderByServerNameAsc();
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

    @Transactional
    public Collection<ApacheUrlConf> checkApacheConfigs(Long id) throws JSchException, SftpException, IOException {
        Server server = serverRepository.getOne(id);
        Map<String, ApacheUrlConf> apacheConfigs = new HashMap<>();

        Session session = this.sshService.getSession(server.getIp());
        session.connect();

        ChannelSftp channel = sshService.getSftpChannel(session);

        for (Object siteconf : channel.ls("/etc/apache2/sites-available")) {
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) siteconf;

            if (entry.getFilename().replace(".", "").trim().length() == 0) {
                continue;
            }

            logger.info("Reading Apache Config: " + entry.getFilename());
            try (InputStream is = channel.get("/etc/apache2/sites-available/" + entry.getFilename());

                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr)) {

                String line;
                boolean ssl = false;
                while ((line = br.readLine()) != null) {
                    if (line.contains(":80")) {
                        ssl = false;
                    } else if (line.contains(":443")) {
                        ssl = true;
                    }

                    if ((line.contains("ServerName") || line.contains("ServerAlias")) && !line.trim().startsWith("#")) {
                        line = line.trim();
                        String url = line.substring(line.indexOf(" ")).trim();

                        if (!StringUtils.isEmpty(url)) {
                            ApacheUrlConf apacheUrlConf;
                            if (apacheConfigs.containsKey(url)) {
                                apacheUrlConf = apacheConfigs.get(url);
                            } else {
                                apacheUrlConf = new ApacheUrlConf();
                                apacheUrlConf.setUrl(url);
                                apacheConfigs.put(url, apacheUrlConf);
                            }

                            if (ssl) {
                                apacheUrlConf.setHttps(true);
                            } else {
                                apacheUrlConf.setHttp(true);
                            }

                            apacheUrlConf.getFilenames().add(entry.getFilename());
                        }
                    }

                }
            }
        }

        session.disconnect();

        server.setApacheConfs(apacheConfigs.values().stream().sorted(Comparator.comparing(ApacheUrlConf::getUrl)).collect(Collectors.toList()));

        return server.getApacheConfs();
    }


    public void pingApacheConfigs(Long id) {
        Server server = serverRepository.getOne(id);

        server.getApacheConfs().forEach(ac -> {
            try {
                logger.info("Pinging {}...", ac.getUrl());
                InetAddress address = InetAddress.getByName(ac.getUrl());
                logger.info("Response for {}: {}", ac.getUrl(), address.getHostAddress());
                template.convertAndSend("/status/iptest", new IPResolverResponse(ac.getUrl(), address.getHostAddress(), address.getHostAddress().equals(server.getIp())));
            } catch (UnknownHostException e) {
                template.convertAndSend("/status/iptest", new IPResolverResponse(ac.getUrl(), "Unknown Host", false));
            }

        });
    }

    @Transactional
    public void testSSHConnection(Long id) throws JSchException {
        Server server = serverRepository.getOne(id);

        Session session = this.sshService.getSession(server.getIp());
        session.connect();

        if (StringUtils.isEmpty(server.getServerName())) {
            server.setServerName(sshService.sendCommand(session, "hostname"));
        }
    }

    @Data
    @AllArgsConstructor
    public static class IPResolverResponse {
        private String url;
        private String ip;
        private boolean matchesServer;
    }
}

