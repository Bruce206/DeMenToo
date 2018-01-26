package de.bruss.demontoo.server;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.bruss.demontoo.common.CustomLocalDateTimeDeserializer;
import de.bruss.demontoo.common.CustomLocalDateTimeSerializer;
import de.bruss.demontoo.common.MonitoredSuperEntity;
import de.bruss.demontoo.instance.Instance;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ocpsoft.prettytime.PrettyTime;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime lastMessage;
	
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

    public String getTimeAgo() {
        if (this.lastMessage == null) {
            return "n.A.";
        }

        PrettyTime p = new PrettyTime(new Locale("de"));
        return p.format(Date.from(this.lastMessage.atZone(ZoneId.systemDefault()).toInstant()));
    }
}
