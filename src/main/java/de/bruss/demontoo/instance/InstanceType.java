package de.bruss.demontoo.instance;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    private LocalDateTime updateTime;

    @Enumerated(EnumType.STRING)
    private AppType appType;
    private String healthUrl;

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
