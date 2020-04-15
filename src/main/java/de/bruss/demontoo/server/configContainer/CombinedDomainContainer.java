package de.bruss.demontoo.server.configContainer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CombinedDomainContainer extends DomainContainer {
    private boolean isInApache = false;
    private boolean isInXibisOne = false;
}
