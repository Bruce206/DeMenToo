package de.bruss.demontoo.instance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.bruss.demontoo.common.CustomZonedDateTimeDeserializer;
import de.bruss.demontoo.common.CustomZonedDateTimeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class InstanceType {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Lob
    private byte[] image;

    private String updateFileName;

    private String updatePath;

    @JsonSerialize(using = CustomZonedDateTimeSerializer.class)
    @JsonDeserialize(using = CustomZonedDateTimeDeserializer.class)
    private ZonedDateTime updateTime;

    @Enumerated(EnumType.STRING)
    private AppType appType;
    private String healthUrl;

    @Column(columnDefinition = "text")
    private String apacheTemplate;

    @Column(columnDefinition = "text")
    private String applicationPropertiesTemplate;

    @Column(columnDefinition = "text")
    private String serviceTemplate;

    private Boolean certbot;

    private String databaseNameTemplate;

    /**
     * in minutes!
     */
    private Long messageInterval;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="instanceType", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Instance> instances = new HashSet<>();

    public InstanceType(String name) {
        this.name = name;
    }

    public Set<String> getInstanceDetailKeys () {
        Set<String> keys = new HashSet<>();

        for (Instance instance : instances) {
            for (InstanceDetail detail : instance.getDetails()) {
                keys.add(detail.getKey());
            }
        }

        return keys;
    }

    public void removeInstance(Instance instance) {
        this.getInstances().remove(instance);
    }

}
