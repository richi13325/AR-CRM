# Security Specification

## Purpose

Defines the API security and authorization policies, specifically ensuring path-accurate endpoint protection against regression and drift.

## Requirements

### Requirement: Path-Accurate Endpoint Security Coverage

The system MUST verify the behavior of real endpoint paths through the actual security filter chain to prevent runtime route drifts.

#### Scenario: Security filter chain authorizes valid endpoints

- GIVEN the application security configuration is active
- WHEN an authenticated request with a valid JWT is made to the real adapter paths (e.g., `/api/tableros/get-all`, `/api/roles/get-all`)
- THEN the security filter chain MUST authorize the request

#### Scenario: Security filter chain rejects unauthenticated endpoints

- GIVEN the application security configuration is active
- WHEN an unauthenticated request without a token is made to the real adapter paths
- THEN the security filter chain MUST reject the request
- AND the system MUST return a 401 Unauthorized status
