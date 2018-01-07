package de.bruss.demontoo.instance;

import de.bruss.demontoo.server.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface InstanceRepository extends JpaRepository<Instance, Long>, JpaSpecificationExecutor<Instance> {
	List<Instance> findByServerAndIdentifier(Server server, String identifier);
}
