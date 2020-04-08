package de.bruss.demontoo.websockets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InstanceHealthMessage {
    @JsonIgnoreProperties("domains")
    private Instance instance;
    private String status;
    private Integer responseTime;
}