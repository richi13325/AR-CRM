# syntax=docker/dockerfile:1

# ============================================================================
# Keycloak image with the CRM2 theme baked in.
#
# The official image has no way to load filesystem themes remotely, and EasyPanel
# does not run our docker-compose bind-mount (`./Keycloak/themes:/opt/keycloak/themes`).
# So we COPY the theme into the image: it travels with the build, is reproducible
# from a clean clone, and redeploys automatically on every git push.
#
# Build context MUST be the `Keycloak/` directory (so `themes/` and
# `realm-export.json` resolve). In EasyPanel set Build Path / Context = `Keycloak`.
# ============================================================================
FROM quay.io/keycloak/keycloak:25.0.6

# Branded login + email theme (referenced by the realm as loginTheme/emailTheme = crm2).
COPY themes /opt/keycloak/themes

# Realm to import on first startup (ignored if the realm already exists in the DB).
COPY realm-export.json /opt/keycloak/data/import/realm-export.json
