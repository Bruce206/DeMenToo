package de.bruss.demontoo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bruss.demontoo.common.MonitoredSuperEntity;
import de.bruss.demontoo.instance.Instance;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Domain  extends MonitoredSuperEntity {
	
	@Id
	@GeneratedValue
	private Long id;
	
	private long node;
	private String name;	
	private String url;
		
	@JsonIgnore
	@ManyToOne
	private Instance instance;

    public Domain(String name, String url) {
        this.name = name;
        this.url = url;
    }

}
