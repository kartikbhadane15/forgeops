package com.forgeops.incident.dto;

import java.time.Instant;
import java.util.UUID;

public record IncidentNoteDTO(
        UUID id,
        UUID incidentId,
        UUID authorId,
        String authorName,
        String authorEmail,
        String noteBody,
        Instant createdAt
) {}
