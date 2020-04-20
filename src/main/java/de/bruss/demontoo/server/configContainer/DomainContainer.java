package de.bruss.demontoo.server.configContainer;

import lombok.Data;

@Data
public class DomainContainer {
    private String url;
    private String ip;
    private boolean https = false;
    private boolean http = false;
    private Long serverId;
    private String serverName;
}
