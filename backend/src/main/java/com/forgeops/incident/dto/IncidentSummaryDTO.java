package com.forgeops.incident.dto;

import com.forgeops.incident.IncidentSeverity;
import com.forgeops.incident.IncidentStatus;
import com.forgeops.user.dto.UserDTO;

import java.time.Instant;
import java.util.UUID;

public record IncidentSummaryDTO(
        UUID id,
        String incidentNumber,
        String title,
        UUID serviceId,
        String serviceName,
        UUID environmentId,
        String environmentName,
        IncidentSeverity severity,
        IncidentStatus status,
        UserDTO assignee,
        UserDTO createdBy,
        Instant createdAt,
        Instant updatedAt
) {}
