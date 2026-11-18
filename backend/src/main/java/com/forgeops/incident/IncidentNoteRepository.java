package com.forgeops.incident;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentNoteRepository extends JpaRepository<IncidentNoteEntity, UUID> {
    List<IncidentNoteEntity> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
