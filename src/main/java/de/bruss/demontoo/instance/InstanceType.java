package de.bruss.demontoo.instance;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Blob;
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
    private Blob imagePath;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="instanceType", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Instance> instances = new HashSet<>();

    public InstanceType(String name) {
        this.name = name;
    }
}