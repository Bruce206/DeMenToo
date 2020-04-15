package de.bruss.demontoo.server;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import de.bruss.demontoo.instance.InstanceService;
import de.bruss.demontoo.server.configContainer.ApacheUrlConf;
import de.bruss.demontoo.server.configContainer.XibisOneDomain;
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
import java.util.regex.Pattern;
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
        server.getApacheConfs().stream().map(ApacheUrlConf::getUrl).forEach(url -> ping(url, server.getIp()));
    }

    public void pingXibisOneDomains(Long id) {
        Server server = serverRepository.getOne(id);
        server.getXibisOneDomains().stream().map(XibisOneDomain::getUrl).forEach(url -> ping(url, server.getIp()));
    }

    private void ping(String url, String serverIp) {
        try {
            logger.info("Pinging {}...", url);
            InetAddress address = InetAddress.getByName(url);
            logger.info("Response for {}: {}", url, address.getHostAddress());
            template.convertAndSend("/status/iptest", new IPResolverResponse(url, address.getHostAddress(), address.getHostAddress().equals(serverIp)));
        } catch (UnknownHostException e) {
            template.convertAndSend("/status/iptest", new IPResolverResponse(url, "Unknown Host", false));
        }
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

    @Transactional
    public Collection<XibisOneDomain> checkXibisOneDomains(Long id) throws JSchException {
        Server server = serverRepository.getOne(id);

        Session session = this.sshService.getSession(server.getIp());
        session.connect();

        List<String> psql_l = Arrays.asList(sshService.sendCommand(session, "su - postgres -c \"psql -l\"").split("\\r?\\n"));

        Set<String> databaseNames = new HashSet<>();
        for (String line : psql_l.subList(3, psql_l.size() - 2)) {
            String dbName = line.substring(0, line.indexOf("|")).trim();
            if (!StringUtils.isEmpty(dbName)) {
                databaseNames.add(dbName);
            }
        }

        List<XibisOneDomain> xibisOneDomains = new ArrayList<>();
        for (String db : databaseNames) {
            List<String> nodesDomains = Arrays.asList(sshService.sendCommand(session, " psql -U postgres " + db + " -c \"select domain, secure, name from nodes_domains left join nodes on nodes_domains.node = nodes.id\"").split("\\r?\\n"));

            if (nodesDomains.size() == 1 && nodesDomains.get(0).trim().length() == 0) {
                // not a cms database
                continue;
            }

            try {
                for (String line : nodesDomains.subList(2, nodesDomains.size() - 1)) {
                    String[] data = line.split(Pattern.quote("|"));
                    XibisOneDomain xod = new XibisOneDomain();
                    xod.setUrl(data[0].trim());
                    xod.setHttp(data[1].trim().equals("f"));
                    xod.setHttps(data[1].trim().equals("t"));
                    xod.setNode(data[2].trim());
                    xod.setDatabase(db);
                    xibisOneDomains.add(xod);
                }
            } catch (IllegalArgumentException iae) {
                logger.warn("Something doesnt fit with the response from postgres: {} in database {}", String.join(", ", nodesDomains), db, iae.getMessage());
            }


        }

        server.setXibisOneDomains(xibisOneDomains);
        return xibisOneDomains;
    }


    @Data
    @AllArgsConstructor
    public static class IPResolverResponse {
        private String url;
        private String ip;
        private boolean matchesServer;
    }
}

