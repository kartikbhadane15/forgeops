package com.forgeops.approval.dto;

import com.forgeops.approval.ActionType;
import com.forgeops.approval.ApprovalStatus;
import com.forgeops.user.dto.UserDTO;

import java.time.Instant;
import java.util.UUID;

public record ActionRequestDTO(
        UUID id,
        String idempotencyKey,
        UUID incidentId,
        String incidentNumber,
        String incidentTitle,
        UUID targetServiceId,
        String targetServiceName,
        ActionType actionType,
        String reason,
        String riskLevel,
        ApprovalStatus approvalStatus,
        UserDTO requestedBy,
        UserDTO reviewedBy,
        String reviewComment,
        UserDTO executedBy,
        String executionResult,
        Instant requestedAt,
        Instant reviewedAt,
        Instant executedAt
) {}
