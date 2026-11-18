package com.forgeops.serviceinventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    Optional<ServiceEntity> findByServiceKey(String key);
    boolean existsByServiceKey(String key);
    Page<ServiceEntity> findByNameContainingIgnoreCaseOrServiceKeyContainingIgnoreCase(String name, String key, Pageable p);
    List<ServiceEntity> findAllByLifecycleStatus(LifecycleStatus status);
}
