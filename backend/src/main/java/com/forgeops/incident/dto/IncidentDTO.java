package com.forgeops.incident.dto;

import com.forgeops.incident.IncidentSeverity;
import com.forgeops.incident.IncidentStatus;
import com.forgeops.user.dto.UserDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IncidentDTO(
        UUID id,
        String incidentNumber,
        String title,
        String description,
        UUID serviceId,
        String serviceName,
        UUID environmentId,
        String environmentName,
        IncidentSeverity severity,
        IncidentStatus status,
        UserDTO assignee,
        UserDTO createdBy,
        String resolutionSummary,
        List<IncidentNoteDTO> notes,
        Instant createdAt,
        Instant updatedAt
) {}
