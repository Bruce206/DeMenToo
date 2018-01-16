package de.bruss.demontoo.instance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstanceTypeRepository extends JpaRepository<InstanceType, Long> {

    InstanceType findByName(String name);
}
