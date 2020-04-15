package de.bruss.demontoo.server.configContainer;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ApacheUrlConf extends DomainContainer {
    private List<String> filenames = new ArrayList<>();
}