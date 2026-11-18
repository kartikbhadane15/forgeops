package com.forgeops.ai.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnalysisResultDTO(
        UUID id,
        UUID incidentId,
        UUID requestedById,
        String requestedByName,
        String riskLevel,
        String summary,
        List<String> possibleCauses,
        List<String> investigationSteps,
        List<ProposedActionDTO> proposedActions,
        List<String> limitations,
        Instant createdAt
) {}
