package com.forgeops.approval.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewActionRequest(
        @NotBlank String comment
) {}
