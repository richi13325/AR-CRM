# Keycloak Local Development Environment

Local Keycloak 25.x instance for CRM2 development, with a dedicated PostgreSQL 16 database.

## Prerequisites

- Docker & Docker Compose
- CRM2 running on `http://localhost:8080`

## Startup

```bash
docker compose up -d keycloak keycloak-db
```

Or start all services:

```bash
docker compose up -d
```

Wait for Keycloak to be ready (health check may take ~30 seconds):

```bash
docker compose ps
```

## Import Realm

The `start-dev --import-realm` flag (in `docker-compose.yml`) **automatically imports** the `crm2-local` realm on first startup. No manual action is required under normal conditions.

If the realm is not visible in the Administration Console after startup completes, import it manually:

1. Open **http://localhost:8180** → **Administration Console**
2. Login with `admin` / `admin`
3. Select **Import** from the realm dropdown (top-left)
4. Choose `realm-export.json` and click **Import**

Or import via REST API:

```bash
curl -X POST http://localhost:8180/admin/realms \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $(curl -s -X POST http://localhost:8180/realms/master/protocol/openid-connect/token \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=admin&password=admin&grant_type=password&client_id=admin-cli" | jq -r .access_token)" \
  -d @realm-export.json
```

## Test Users

| Username | Password | Roles | usuario_id | super_usuario_id |
|----------|----------|-------|------------|------------------|
| admin | admin | SUPER_USUARIO | 550e8400-e29b-41d4-a716-446655440001 | 550e8400-e29b-41d4-a716-446655440000 |
| user | user | USUARIO | 550e8400-e29b-41d4-a716-446655440002 | — |

## Obtain Access Token

```bash
curl -X POST http://localhost:8180/realms/crm2-local/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=crm2-api" \
  -d "client_secret=crm2-api-secret-local" \
  -d "username=admin" \
  -d "password=admin"
```

Response:

```json
{
  "access_token": "eyJ...",
  "expires_in": 300,
  "refresh_token": "eyJ...",
  "token_type": "Bearer",
  "session_state": "...",
  "scope": "profile email roles"
}
```

## Verify Token Audience

The token should contain the standard JWT `aud` claim with `crm2-api`. Verify with:

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/crm2-local/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=crm2-api" \
  -d "client_secret=crm2-api-secret-local" \
  -d "username=admin" \
  -d "password=admin" | jq -r .access_token)

# Decode JWT payload (no signature verification)
echo $TOKEN | cut -d. -f2 | base64 -d | jq
```

Expected custom claims in the decoded token:
```json
{
  "sub": "...",
  "preferred_username": "admin",
  "email": "admin@crm2.local",
  "usuario_id": "550e8400-e29b-41d4-a716-446655440001",
  "super_usuario_id": "550e8400-e29b-41d4-a716-446655440000",
  "roles": ["SUPER_USUARIO", "USUARIO"],
  "aud": ["crm2-api"]
}
```

## Smoke Test: Call CRM2 API

```bash
# With valid token — expect 200/403 (auth OK, authorization depends on endpoint)
curl -X GET http://localhost:8080/api/tableros \
  -H "Authorization: Bearer $TOKEN"

# Without token — expect 401
curl -X GET http://localhost:8080/api/tableros
```

## Ports

| Service | Host Port | Container Port |
|---------|-----------|----------------|
| Keycloak | 8180 | 8180 |
| Keycloak DB | 5434 | 5432 |
| CRM Postgres | 5433 | 5432 |

## Validate Configuration

```bash
docker compose config
```

## Credential Notes

> **Development only.** These credentials are hardcoded for local convenience and must never appear in production. The Keycloak database is isolated from the CRM application database.

## Tear Down

```bash
docker compose down -v  # removes named volumes
```

To keep data between restarts, omit `-v`. To reset completely (fresh Keycloak state):

```bash
docker compose down -v && docker compose up -d
```