package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RenombrarConversacionRequest(@NotBlank String nombre) {}
