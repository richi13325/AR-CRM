package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MoverColumnaRequest(
    @NotNull(message = "targetColumnaId is required")
    UUID targetColumnaId
) {}
