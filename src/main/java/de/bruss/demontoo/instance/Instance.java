package de.bruss.demontoo.instance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.bruss.demontoo.common.CustomLocalDateTimeDeserializer;
import de.bruss.demontoo.common.CustomLocalDateTimeSerializer;
import de.bruss.demontoo.common.MonitoredSuperEntity;
import de.bruss.demontoo.domain.Domain;
import de.bruss.demontoo.server.Server;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.ocpsoft.prettytime.PrettyTime;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
public class Instance extends MonitoredSuperEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String identifier;
    private String version;
    private String licensedFor;
    private String port;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean excludeFromHealthcheck = false;

    @ManyToOne
    @JsonIgnoreProperties("instances")
    private InstanceType instanceType;

    @Transient
    private String type;

    @Column(name = "prod", columnDefinition = "boolean NOT NULL DEFAULT false")
    private boolean prod;

    @OneToMany(mappedBy = "instance", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private List<Domain> domains = new ArrayList<>();

    @OneToMany(mappedBy = "instance", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private Set<InstanceDetail> details = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties("instances")
    private Server server;

    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime lastMessage;

    @Override
    public String toString() {
        if (this.server != null) {
            return this.server.getIp() + "\t" + this.server.getServerName() + "\t" + this.identifier;
        } else {
            return "MISSING SERVER " + this.identifier;
        }
    }

    public void addDomain(Domain domain) {
        this.domains.add(domain);
    }

    public Map<String, List<InstanceDetail>> getInstanceDetailsByCategory() {
        return this.details.stream().sorted(Comparator.comparing(InstanceDetail::getCategory)).collect(Collectors.groupingBy(InstanceDetail::getCategory));
    }

    public Map<String, List<InstanceDetail>> getInstanceDetailsByKey() {
        return this.details.stream().sorted(Comparator.comparing(InstanceDetail::getKey)).collect(Collectors.groupingBy(InstanceDetail::getKey));
    }

    public String getTimeAgo() {
        if (this.lastMessage == null) {
            return "n.A.";
        }

        PrettyTime p = new PrettyTime(new Locale("de"));
        return p.format(Date.from(this.lastMessage.atZone(ZoneId.systemDefault()).toInstant()));
    }

    public boolean getLastMessageCritical() {
        if (ChronoUnit.MINUTES.between(lastMessage, LocalDateTime.now()) > (this.instanceType.getMessageInterval() == null ? 60 : this.instanceType.getMessageInterval())) {
            if (this.instanceType != null &&
                    ChronoUnit.MINUTES.between(lastMessage,
                            LocalDateTime.now())
                            > (this.instanceType.getMessageInterval() == null ? 60
                            : this.instanceType.getMessageInterval())) {
                return true;
            }
            return false;
        }

        return false;
    }

    public InstanceType getInstanceType() {
        return instanceType != null ? instanceType : new InstanceType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return Objects.equals(id, instance.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
