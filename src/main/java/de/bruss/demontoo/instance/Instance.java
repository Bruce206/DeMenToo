package de.bruss.demontoo.instance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.bruss.demontoo.common.CustomLocalDateTimeDeserializer;
import de.bruss.demontoo.common.CustomLocalDateTimeSerializer;
import de.bruss.demontoo.common.MonitoredSuperEntity;
import de.bruss.demontoo.domain.Domain;
import de.bruss.demontoo.server.Server;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Instance extends MonitoredSuperEntity {
	
	@Id
	@GeneratedValue
	private Long id;
		
	private String identifier;
	private String version;
	private String licensedFor;
	private String type;
	private String paymentModel;
	private Long usedSpaceInMB;
	
	@Column(name = "prod", columnDefinition = "boolean NOT NULL DEFAULT false")
	private boolean prod;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="instance", orphanRemoval = true)
	private List<Domain> domains = new ArrayList<>();

	@ManyToOne
    @JsonIgnoreProperties("instances")
	private Server server;

    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
	private LocalDateTime lastMessage;

	@Override
	public String toString() {
		return this.server.getIp() + "\t" + this.server.getServerName() + "\t" + this.identifier;
	}

    public void addDomain(Domain domain) {
	    this.domains.add(domain);
    }
}
