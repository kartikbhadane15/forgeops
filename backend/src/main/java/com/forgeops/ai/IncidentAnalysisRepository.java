package com.forgeops.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentAnalysisRepository extends JpaRepository<IncidentAnalysisEntity, UUID> {
    List<IncidentAnalysisEntity> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId);
}
