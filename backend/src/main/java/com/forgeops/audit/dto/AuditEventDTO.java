package com.forgeops.audit.dto;

import com.forgeops.user.dto.UserDTO;

import java.time.Instant;
import java.util.UUID;

public record AuditEventDTO(
        UUID id,
        UserDTO actor,
        String eventType,
        String entityType,
        String entityId,
        String correlationId,
        String safeSummary,
        String detailsJson,
        Instant createdAt
) {}
