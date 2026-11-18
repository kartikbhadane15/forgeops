package com.forgeops.serviceinventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnvironmentRepository extends JpaRepository<EnvironmentEntity, UUID> {
    List<EnvironmentEntity> findByServiceId(UUID serviceId);
    Optional<EnvironmentEntity> findByServiceIdAndName(UUID serviceId, String name);
}
