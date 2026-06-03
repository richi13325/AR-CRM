package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank(message = "correo is required")
    @Email(message = "correo must be a valid email")
    String correo
) {}
