package de.bruss.demontoo.server.configContainer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XibisOneDomain extends DomainContainer{
    private String node;
    private String database;
}
