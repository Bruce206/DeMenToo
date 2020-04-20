package de.bruss.demontoo.server;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import de.bruss.demontoo.instance.InstanceService;
import de.bruss.demontoo.server.configContainer.*;
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
        return checkApacheConfigs(server);
    }

    @Transactional
    public Collection<ApacheUrlConf> checkApacheConfigs(Server server) throws JSchException, SftpException, IOException {
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
                                apacheUrlConf.setServerId(server.getId());
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
        server.getApacheConfs().stream().map(ApacheUrlConf::getUrl).forEach(url -> pingAndSendToWebsocket(url, server.getIp()));
    }

    public void pingXibisOneDomains(Long id) {
        Server server = serverRepository.getOne(id);
        server.getXibisOneDomains().stream().map(XibisOneDomain::getUrl).forEach(url -> pingAndSendToWebsocket(url, server.getIp()));
    }

    public void pingAllDomainsAndSendToWebsocket(Long id) {
        Server server = serverRepository.getOne(id);

        Set<DomainContainer> allDomains = new HashSet<>();
        allDomains.addAll(server.getApacheConfs());
        allDomains.addAll(server.getXibisOneDomains());

        allDomains.forEach(conf -> pingAndSendToWebsocket(conf.getUrl(), server.getIp()));
    }

    private void pingAndSendToWebsocket(String url, String serverIp) {
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
        return checkXibisOneDomains(server);
    }

    @Transactional
    public Collection<XibisOneDomain> checkXibisOneDomains(Server server) throws JSchException {
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
        Set<String> foundUrls = new HashSet<>();
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
                    xod.setServerId(server.getId());

                    String url = data[0].trim();
                    if (!foundUrls.contains(url)) {
                        foundUrls.add(url);
                        xod.setUrl(url);
                    } else {
                        String random = "-duplicate-" + (Math.random() * 10000);
                        xod.setUrl(url + random);
                    }

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

    @Transactional
    public List<CombinedDomainContainer> checkCombinedDomains(Long id) throws JSchException, SftpException, IOException {
        Server server = serverRepository.getOne(id);
        return checkCombinedDomains(server);
    }

    @Transactional
    public List<CombinedDomainContainer> checkCombinedDomains(Server server) throws JSchException, SftpException, IOException {
        Map<String, ApacheUrlConf> apacheUrlConfs = checkApacheConfigs(server).stream().collect(Collectors.toMap(ApacheUrlConf::getUrl, dc -> dc));
        Map<String, XibisOneDomain> xibisOneDomains = checkXibisOneDomains(server).stream().collect(Collectors.toMap(XibisOneDomain::getUrl, dc -> dc));

        Set<String> allUrls = new HashSet<>();
        allUrls.addAll(apacheUrlConfs.keySet());
        allUrls.addAll(xibisOneDomains.keySet());

        List<CombinedDomainContainer> combined = new ArrayList<>();
        for (String url : allUrls) {
            CombinedDomainContainer cdc = new CombinedDomainContainer();

            try {
                InetAddress address = InetAddress.getByName(url);
                cdc.setIp(address.getHostAddress());
                if (address.getHostAddress().equals(server.getIp())) {
                    cdc.setPingStatus(PingStatus.SAME_SERVER);
                } else {
                    List<Server> byIp = serverRepository.findByIp(address.getHostAddress());
                    if (byIp.isEmpty()) {
                        cdc.setPingStatus(PingStatus.FOREIGN_SERVER);
                    } else {
                        cdc.setPingStatus(PingStatus.OTHER_SERVER);
                        cdc.setActualServerName(byIp.get(0).getServerName());
                    }
                }
            } catch (UnknownHostException e) {
                cdc.setPingStatus(PingStatus.UNKNOWN_HOST);
            }

            cdc.setServerId(server.getId());
            cdc.setServerName(StringUtils.isEmpty(server.getDisplayName()) ? server.getServerName() : server.getDisplayName());
            cdc.setUrl(url);
            cdc.setInApache(apacheUrlConfs.containsKey(url));
            cdc.setInXibisOne(xibisOneDomains.containsKey(url));
            combined.add(cdc);
        }

        if (server.getCombinedDomains() != null) {
            server.getCombinedDomains().clear();
        }
        server.setCombinedDomains(combined);

        return combined;
    }

    @Transactional
    public Collection<CombinedDomainContainer> checkCombinedDomains() throws JSchException, SftpException, IOException {
        List<Server> servers = serverRepository.findAllByBlacklistedIsFalseOrderByServerNameAsc().stream().filter(s -> !s.isActiveCheckDisabled()).collect(Collectors.toList());

        List<CombinedDomainContainer> allDomains = new ArrayList<>();

        for (Server server : servers) {
            allDomains.addAll(checkCombinedDomains(server));
        }

        return allDomains.stream().sorted(Comparator.comparing(CombinedDomainContainer::getUrl)).collect(Collectors.toList());
    }

    @Transactional
    public Collection<CombinedDomainContainer> getCombinedDomains() {
        List<Server> servers = serverRepository.findAllByBlacklistedIsFalseOrderByServerNameAsc().stream().filter(s -> !s.isActiveCheckDisabled()).collect(Collectors.toList());

        List<CombinedDomainContainer> allDomains = new ArrayList<>();

        for (Server server : servers) {
            if (server.getCombinedDomains() != null) {
                allDomains.addAll(server.getCombinedDomains());
            }
        }

        return allDomains.stream().sorted(Comparator.comparing(CombinedDomainContainer::getUrl)).collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    public static class IPResolverResponse {
        private String url;
        private String ip;
        private boolean matchesServer;
    }
}

