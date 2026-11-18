package com.forgeops.incident;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<IncidentEntity, UUID> {

    Optional<IncidentEntity> findByIncidentNumber(String incidentNumber);

    @Query("SELECT i FROM IncidentEntity i " +
           "WHERE (:status IS NULL OR i.status = :status) " +
           "AND (:severity IS NULL OR i.severity = :severity) " +
           "AND (:serviceId IS NULL OR i.service.id = :serviceId)")
    Page<IncidentEntity> findWithFilters(
            @Param("status") IncidentStatus status,
            @Param("severity") IncidentSeverity severity,
            @Param("serviceId") UUID serviceId,
            Pageable pageable
    );

    long countByStatus(IncidentStatus status);

    long countBySeverity(IncidentSeverity severity);

    long countByStatusNot(IncidentStatus status);

    List<IncidentEntity> findTop5ByStatusNotOrderByCreatedAtDesc(IncidentStatus status);

    @Query("SELECT MAX(CAST(SUBSTRING(i.incidentNumber, 10) AS integer)) FROM IncidentEntity i WHERE i.incidentNumber LIKE :prefix%")
    Integer findMaxIncidentSequenceNumber(@Param("prefix") String prefix);
}
