package com.forgeops.incident.dto;

import com.forgeops.incident.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateIncidentRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String description,
        @NotNull UUID serviceId,
        @NotNull UUID environmentId,
        @NotNull IncidentSeverity severity
) {}
