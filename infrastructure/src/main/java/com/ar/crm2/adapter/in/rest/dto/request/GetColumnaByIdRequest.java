package com.ar.crm2.adapter.in.rest.dto.request;

import java.util.UUID;

/**
 * REST request DTO for getting a Columna by its id.
 */
public record GetColumnaByIdRequest(
    UUID id
) {}