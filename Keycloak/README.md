# Keycloak Local Development Environment

Local Keycloak 25.x instance for CRM2 development, with a dedicated PostgreSQL 16 database.

## Prerequisites

- Docker & Docker Compose
- CRM2 running on `http://localhost:8080`
- (Frontend) running on `http://localhost:5173` if you use the `crm2-frontend` SPA client

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

## Fresh Start for a New Teammate

The Keycloak container stores realm state in the named volume `crm2-keycloak-data`. If that volume already exists from a previous run, `--import-realm` is **ignored** on subsequent startups — Keycloak uses whatever is in the database. To get a clean state that matches `realm-export.json` exactly, wipe the volume first:

```bash
docker compose down                  # stop containers (keep volumes)
docker volume rm crm2_crm2-keycloak-data crm2_crm2-keycloak-pgdata
docker compose up -d keycloak keycloak-db
```

Then verify the realm is present:

```bash
curl -s http://localhost:8180/realms/crm2-local/.well-known/openid-configuration \
  | jq -r .issuer
# expected: http://localhost:8180/realms/crm2-local
```

You should also see the **CRM2** branded login page (Spanish copy: *"Ingresa tus credenciales para entrar a CRM2"*) at `http://localhost:8180/realms/crm2-local/protocol/openid-connect/auth?client_id=crm2-frontend&response_type=code&redirect_uri=http://localhost:5173/`.

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

## CRM2 Login Theme

The realm uses a custom `crm2` theme for the **login** and **email** screens. Theme files live in `Keycloak/themes/crm2/` and are mounted into the Keycloak container at `/opt/keycloak/themes` (read-only) by `docker-compose.yml`. This means:

- The theme is version-controlled and reproducible from a fresh clone.
- The container image can be the standard `quay.io/keycloak/keycloak:25.0.6` — no custom build is required.
- The theme overrides whatever is baked into the image, so re-tagging the image is safe.

Files:

- `Keycloak/themes/crm2/login/` — Freemarker templates (`login.ftl`, `template.ftl`, `error.ftl`, etc.) + `theme.properties` + `resources/css/styles.css` + `resources/img/logo.svg`.
- `Keycloak/themes/crm2/email/` — Freemarker templates for verification / password-reset emails + `messages_es.properties` (Spanish copy).

To preview without breaking the running container, change the realm's `loginTheme` to `crm2` via the admin console and reload the login page.

## Frontend Integration & CORS

The realm ships with a pre-configured SPA client called `crm2-frontend` that the React/Vite frontend on `http://localhost:5173` should use:

| Setting | Value |
|---------|-------|
| `clientId` | `crm2-frontend` |
| `publicClient` | `true` |
| `standardFlowEnabled` | `true` (Authorization Code) |
| `directAccessGrantsEnabled` | `true` (Resource Owner Password — handy for `curl`/Postman) |
| `pkce.code.challenge.method` | `S256` |
| `redirectUris` | `http://localhost:5173/*` |
| `webOrigins` | `http://localhost:5173` |
| `rootUrl` / `baseUrl` / `adminUrl` | `http://localhost:5173` |
| Default client scopes | `web-origins`, `acr`, `profile`, `roles`, `email` |
| Protocol mappers | `crm2-api-audience-mapper` (so the SPA can call the backend with a single audience) |

In the frontend (e.g. `keycloak-js`), point at:

```ts
{
  url: 'http://localhost:8180',
  realm: 'crm2-local',
  clientId: 'crm2-frontend',
}
```

**CORS smoke test** — once the realm is imported, the preflight for the Account API should be allowed:

```bash
curl -sS -i -X OPTIONS http://localhost:8180/realms/crm2-local/account \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: authorization" \
  | grep -i 'access-control'
# expected: Access-Control-Allow-Origin: http://localhost:5173
#           Access-Control-Allow-Credentials: true
```

If you see a CORS error in the browser console after login (typically on `GET /realms/crm2-local/account` or `GET /realms/crm2-local/account/profile`), it almost always means one of three things:

1. The teammate is hitting a **stale Keycloak volume** — run the *Fresh Start* commands above so `--import-realm` actually re-runs.
2. The frontend is sending `Origin` from a host other than `http://localhost:5173` (e.g. `127.0.0.1` vs `localhost`) — add the exact value to `crm2-frontend.webOrigins` or use `+` for permissive dev mode.
3. The bearer token in the request does not carry the `allowed-origins` claim because `web-origins` was removed from `crm2-frontend.defaultClientScopes` — re-import the realm.

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

## SMTP Configuration (Email Sending)

To enable Keycloak to send verification emails, configure SMTP in the realm settings.

### Option 1: Gmail with App Password

1. **Enable 2FA** on your Gmail account
2. **Generate an App Password**:
   - Go to Google Account → Security
   - Enable 2-Step Verification
   - Search "App Passwords" → Create new app password for "Mail"
3. **Configure in Keycloak UI**:
   - Open http://localhost:8180 → Administration Console
   - Select **crm2-local** realm → **Email** tab
   - Fill in:
     - **Host**: `smtp.gmail.com`
     - **Port**: `587`
     - **From**: `your-email@gmail.com`
     - **From Display Name**: `CRM2 Local`
     - **Enable SSL**: `OFF`
     - **Enable StartTLS**: `ON`
     - **Authentication**: `ON`
     - **Username**: `your-email@gmail.com`
     - **Password**: `your-app-password` (16-character App Password, NOT your regular password)

### Option 2: Console (No Real Email)

For development without real email, use the dummy SMTP server:

```bash
# Start dummy SMTP on port 1025
docker run -d -p 1025:8025 --name maildev axllent/maildev
```

Then configure Keycloak:
- **Host**: `host.docker.internal` (Windows/Mac) or `172.17.0.1` (Linux)
- **Port**: `1025`
- **Enable SSL/TLS**: `OFF`
- **Authentication**: `OFF`

### ⚠️ Important: UI Settings Persistence

> **Warning**: SMTP settings configured through the Keycloak UI are stored **only in the Keycloak database** (PostgreSQL). This data lives in a Docker volume and **will NOT be committed to the repository**.
>
> After recreating containers with `docker compose down -v`, you must reconfigure SMTP manually through the UI.

To persist SMTP config across restarts, keep the Docker volumes by avoiding `docker compose down -v`.

For team sharing, document the SMTP settings but **do not commit real SMTP passwords or exported realm files containing secrets**.

## Tear Down

```bash
docker compose down -v  # removes named volumes
```

To keep data between restarts, omit `-v`. To reset completely (fresh Keycloak state):

```bash
docker compose down -v && docker compose up -d
```
