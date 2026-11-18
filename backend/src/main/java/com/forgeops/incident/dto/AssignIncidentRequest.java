package com.forgeops.incident.dto;

import java.util.UUID;

public record AssignIncidentRequest(
        UUID assigneeId
) {}
