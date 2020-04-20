package de.bruss.demontoo.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface ServerRepository extends JpaRepository<Server, Long>, JpaSpecificationExecutor<Server> {
	Server findByIpAndServerName(String remoteAddr, String asText);

    List<Server> findAllByBlacklistedIsFalseOrderByServerNameAsc();

    List<Server> findByIp(String hostAddress);
}
