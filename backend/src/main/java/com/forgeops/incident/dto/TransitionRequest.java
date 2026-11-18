package com.forgeops.incident.dto;

import com.forgeops.incident.IncidentStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionRequest(
        @NotNull IncidentStatus targetStatus,
        String resolutionSummary
) {}
