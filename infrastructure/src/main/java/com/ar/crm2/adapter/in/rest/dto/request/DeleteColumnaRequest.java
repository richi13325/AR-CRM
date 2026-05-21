package com.ar.crm2.adapter.in.rest.dto.request;

import java.util.UUID;

/**
 * REST request DTO for deleting a Columna.
 */
public record DeleteColumnaRequest(
    UUID id
) {}