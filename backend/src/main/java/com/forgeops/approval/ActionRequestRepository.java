package com.forgeops.approval;

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
public interface ActionRequestRepository extends JpaRepository<ActionRequestEntity, UUID> {

    Optional<ActionRequestEntity> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT a FROM ActionRequestEntity a " +
           "WHERE (:status IS NULL OR a.approvalStatus = :status) " +
           "AND (:incidentId IS NULL OR a.incident.id = :incidentId)")
    Page<ActionRequestEntity> findWithFilters(
            @Param("status") ApprovalStatus status,
            @Param("incidentId") UUID incidentId,
            Pageable pageable
    );

    long countByApprovalStatus(ApprovalStatus status);

    List<ActionRequestEntity> findByIncidentIdOrderByRequestedAtDesc(UUID incidentId);
}
