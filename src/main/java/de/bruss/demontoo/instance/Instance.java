package de.bruss.demontoo.instance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bruss.demontoo.common.MonitoredSuperEntity;
import de.bruss.demontoo.domain.Domain;
import de.bruss.demontoo.server.Server;
import lombok.Data;

import javax.persistence.*;
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
	
	@Column(name = "prod", columnDefinition = "boolean NOT NULL DEFAULT false")
	private boolean prod;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="instance", fetch = FetchType.EAGER)
	private List<Domain> domains = new ArrayList<>();

	@ManyToOne
    @JsonIgnoreProperties("instances")
	private Server server;

	@Override
	public String toString() {
		return this.server.getIp() + "\t" + this.server.getServerName() + "\t" + this.identifier;
	}

    public void addDomain(Domain domain) {
	    this.domains.add(domain);
    }
}
