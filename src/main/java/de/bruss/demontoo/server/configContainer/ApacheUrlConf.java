package de.bruss.demontoo.server.configContainer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApacheUrlConf {
    private String url;
    private boolean https = false;
    private boolean http = false;
    private List<String> filenames = new ArrayList<>();
}