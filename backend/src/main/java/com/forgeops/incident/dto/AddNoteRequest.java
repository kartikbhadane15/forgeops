package com.forgeops.incident.dto;

import jakarta.validation.constraints.NotBlank;

public record AddNoteRequest(
        @NotBlank String noteBody
) {}
