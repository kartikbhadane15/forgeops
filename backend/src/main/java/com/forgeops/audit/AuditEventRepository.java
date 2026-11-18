package com.forgeops.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    @Query("SELECT a FROM AuditEventEntity a " +
           "WHERE (:eventType IS NULL OR a.eventType = :eventType) " +
           "AND (:entityType IS NULL OR a.entityType = :entityType) " +
           "AND (:entityId IS NULL OR a.entityId = :entityId)")
    Page<AuditEventEntity> findWithFilters(
            @Param("eventType") String eventType,
            @Param("entityType") String entityType,
            @Param("entityId") String entityId,
            Pageable pageable
    );
}
