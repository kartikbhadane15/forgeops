package com.forgeops.ai.dto;

import java.util.UUID;

public record ProposedActionDTO(
        String actionType,
        UUID targetServiceId,
        String targetServiceName,
        String reason,
        String riskLevel,
        String rollbackStrategy
) {}
