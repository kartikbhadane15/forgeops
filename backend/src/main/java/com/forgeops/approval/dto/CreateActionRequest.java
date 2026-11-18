package com.forgeops.approval.dto;

import com.forgeops.approval.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateActionRequest(
        @NotBlank @Size(max = 64) String idempotencyKey,
        @NotNull UUID incidentId,
        @NotNull UUID targetServiceId,
        @NotNull ActionType actionType,
        @NotBlank String reason,
        @NotBlank String riskLevel
) {}
