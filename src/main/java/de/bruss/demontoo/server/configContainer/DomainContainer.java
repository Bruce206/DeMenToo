package de.bruss.demontoo.server.configContainer;

import lombok.Data;

@Data
public class DomainContainer {
    private String url;
    private boolean https = false;
    private boolean http = false;
}
