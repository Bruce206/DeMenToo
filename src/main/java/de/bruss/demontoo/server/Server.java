package de.bruss.demontoo.server;

import de.bruss.demontoo.common.MonitoredSuperEntity;
import de.bruss.demontoo.instance.Instance;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Server extends MonitoredSuperEntity {

	@Id
	@GeneratedValue
	private long id;
	
	private String serverName;
	private String ip;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="server", fetch = FetchType.EAGER)
	private List<Instance> instances = new ArrayList<>();
	
	public void addInstance(Instance instance) {
		this.instances.add(instance);		
	}

	public void removeInstance(Instance instance) {
		this.instances.remove(instance);		
	}

    public Server(String serverName, String ip) {
        this.serverName = serverName;
        this.ip = ip;
    }
}
