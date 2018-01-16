package de.bruss.demontoo.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface ServerRepository extends JpaRepository<Server, Long>, JpaSpecificationExecutor<Server> {
	Server findByIpAndServerName(String remoteAddr, String asText);
}
