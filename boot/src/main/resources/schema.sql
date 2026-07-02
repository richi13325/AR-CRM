-- Schema compatibility fix for the Ficha Kanban-card refactor.
--
-- Ficha no longer owns duplicated business metadata. These fields now belong to
-- Tarea/Trato, while Ficha stores only card position/state:
-- id, columna_id, tipo_ficha, trato_id, tarea_id, actualizado_en.
--
-- Hibernate ddl-auto=update does not drop obsolete columns or old NOT NULL
-- constraints, so existing local databases can still reject inserts because
-- legacy columns are missing values. Keep this script idempotent so it is safe
-- on fresh databases and repeated startups.

ALTER TABLE IF EXISTS fichas DROP COLUMN IF EXISTS responsable_id;
ALTER TABLE IF EXISTS fichas DROP COLUMN IF EXISTS creado_por;
ALTER TABLE IF EXISTS fichas DROP COLUMN IF EXISTS creado_en;

-- Agenda reminder fields for email notifications
ALTER TABLE IF EXISTS agendas ADD COLUMN IF NOT EXISTS recordatorio_habilitado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS agendas ADD COLUMN IF NOT EXISTS minutos_antes INTEGER;
ALTER TABLE IF EXISTS agendas ADD COLUMN IF NOT EXISTS recordatorio_estado VARCHAR(20);
ALTER TABLE IF EXISTS agendas ADD COLUMN IF NOT EXISTS recordatorio_enviado_en TIMESTAMP;
ALTER TABLE IF EXISTS agendas ADD COLUMN IF NOT EXISTS ultimo_intento_en TIMESTAMP;

-- ColumnaTablero: drop the obsolete contextual semantic state columns. The
-- column-board relation no longer carries estadoTarea/estadoTrato; only the
-- catalog Columna holds semantic state.
ALTER TABLE IF EXISTS columnas_tablero DROP COLUMN IF EXISTS estado_tarea;
ALTER TABLE IF EXISTS columnas_tablero DROP COLUMN IF EXISTS estado_trato;

-- Etiqueta catalog (add-ficha-etiquetas slice 2)
-- Idempotent: safe on fresh databases and repeated startups.
-- Hibernate ddl-auto=update creates the table but not the index
-- on fichas_etiquetas.etiqueta_id, which is needed for the
-- cascade-delete "delete all relations referencing this etiqueta"
-- path. We add it explicitly here.
CREATE TABLE IF NOT EXISTS etiquetas (
    id              VARCHAR(36)  NOT NULL,
    nombre          VARCHAR(50)  NOT NULL,
    tipo_etiqueta   VARCHAR(20)  NOT NULL,
    color           VARCHAR(7)   NOT NULL,
    creado_en       TIMESTAMP    NOT NULL,
    CONSTRAINT pk_etiquetas PRIMARY KEY (id),
    CONSTRAINT uk_etiquetas_nombre_tipo UNIQUE (nombre, tipo_etiqueta)
);

CREATE TABLE IF NOT EXISTS fichas_etiquetas (
    id              VARCHAR(36)  NOT NULL,
    ficha_id        VARCHAR(36)  NOT NULL,
    etiqueta_id     VARCHAR(36)  NOT NULL,
    tipo_etiqueta   VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_fichas_etiquetas PRIMARY KEY (id),
    CONSTRAINT uk_fichas_etiquetas_ficha_etiqueta UNIQUE (ficha_id, etiqueta_id),
    CONSTRAINT fk_fichas_etiquetas_ficha
        FOREIGN KEY (ficha_id) REFERENCES fichas (id) ON DELETE CASCADE,
    CONSTRAINT fk_fichas_etiquetas_etiqueta
        FOREIGN KEY (etiqueta_id) REFERENCES etiquetas (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fichas_etiquetas_etiqueta
    ON fichas_etiquetas (etiqueta_id);

-- WhatsApp module tables (ambarcrm-2-0 feature/whatsapp-module)
CREATE TABLE IF NOT EXISTS wa_canal_whatsapp (
    id              VARCHAR(36)  NOT NULL,
    empresa_id      VARCHAR(36)  NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    instance_name   VARCHAR(100) NOT NULL,
    proveedor       VARCHAR(30)  NOT NULL,
    estado          VARCHAR(30)  NOT NULL,
    api_url         VARCHAR(500) NOT NULL,
    api_key         VARCHAR(500) NOT NULL,
    creado_en       TIMESTAMP    NOT NULL,
    actualizado_en  TIMESTAMP    NOT NULL,
    CONSTRAINT pk_wa_canal_whatsapp PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS wa_conversacion (
    id              VARCHAR(36)  NOT NULL,
    canal_id        VARCHAR(36)  NOT NULL,
    contacto_id     VARCHAR(36),
    numero_telefono VARCHAR(30)  NOT NULL,
    nombre_contacto VARCHAR(150),
    estado          VARCHAR(30)  NOT NULL,
    asignado_a      VARCHAR(36),
    no_leidos            INT       NOT NULL DEFAULT 0,
    ultimo_mensaje_at    TIMESTAMP,
    ultimo_mensaje_texto VARCHAR(200),
    labels          VARCHAR(500),
    bot_activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMP    NOT NULL,
    actualizado_en  TIMESTAMP    NOT NULL,
    CONSTRAINT pk_wa_conversacion PRIMARY KEY (id)
);

ALTER TABLE wa_conversacion ADD COLUMN IF NOT EXISTS labels VARCHAR(500);
ALTER TABLE wa_conversacion ADD COLUMN IF NOT EXISTS bot_activo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE wa_conversacion ADD COLUMN IF NOT EXISTS csat_score INT;
ALTER TABLE wa_conversacion ADD COLUMN IF NOT EXISTS csat_enviado_en TIMESTAMP;
ALTER TABLE wa_conversacion ADD COLUMN IF NOT EXISTS aviso_fuera_en TIMESTAMP;
ALTER TABLE wa_conversacion ADD COLUMN IF NOT EXISTS foto_url VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_wa_conversacion_canal
    ON wa_conversacion (canal_id);

-- UNIQUE: evita conversaciones duplicadas del mismo contacto en el mismo canal
-- cuando dos mensajes llegan casi a la vez (ver GetOrCreateConversacionService).
-- Se reemplaza el indice no-unico anterior (mismo par de columnas) por uno UNICO.
-- NOTA: si la BD ya tuviera duplicados preexistentes, el CREATE UNIQUE fallaria
-- al desplegar; en ese caso hay que limpiarlos antes (no deberia haber, el
-- modulo es nuevo).
DROP INDEX IF EXISTS idx_wa_conversacion_telefono_canal;
CREATE UNIQUE INDEX IF NOT EXISTS uk_wa_conversacion_telefono_canal
    ON wa_conversacion (numero_telefono, canal_id);

CREATE TABLE IF NOT EXISTS wa_mensaje (
    id              VARCHAR(36)  NOT NULL,
    conversacion_id VARCHAR(36)  NOT NULL,
    wa_message_id   VARCHAR(100) NOT NULL,
    tipo            VARCHAR(30)  NOT NULL,
    direccion       VARCHAR(20)  NOT NULL,
    contenido       VARCHAR(4096),
    media_url       VARCHAR(1000),
    status          VARCHAR(20)  NOT NULL,
    enviado_por     VARCHAR(36),
    interna         BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_en       TIMESTAMP    NOT NULL,
    CONSTRAINT pk_wa_mensaje PRIMARY KEY (id),
    CONSTRAINT uk_wa_mensaje_wa_message_id UNIQUE (wa_message_id)
);

ALTER TABLE wa_mensaje ADD COLUMN IF NOT EXISTS interna BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_wa_mensaje_conversacion
    ON wa_mensaje (conversacion_id, creado_en);

CREATE TABLE IF NOT EXISTS wa_grupo (
    id                VARCHAR(36)  NOT NULL,
    canal_id          VARCHAR(36),
    jid               VARCHAR(100) NOT NULL,
    nombre            VARCHAR(200) NOT NULL,
    no_leidos         INT          NOT NULL DEFAULT 0,
    ultimo_mensaje_at TIMESTAMP,
    ultimo_mensaje_texto VARCHAR(200),
    creado_en         TIMESTAMP    NOT NULL,
    CONSTRAINT pk_wa_grupo PRIMARY KEY (id),
    CONSTRAINT uk_wa_grupo_jid UNIQUE (jid)
);

ALTER TABLE wa_grupo ADD COLUMN IF NOT EXISTS ultimo_mensaje_texto VARCHAR(200);

CREATE TABLE IF NOT EXISTS wa_mensaje_grupo (
    id            VARCHAR(36)  NOT NULL,
    grupo_id      VARCHAR(36)  NOT NULL,
    direccion     VARCHAR(20)  NOT NULL,
    tipo          VARCHAR(30)  NOT NULL,
    contenido     VARCHAR(4096),
    media_url     VARCHAR(1000),
    remitente     VARCHAR(200),
    remitente_tel VARCHAR(50),
    status        VARCHAR(20)  NOT NULL,
    wa_message_id VARCHAR(100),
    timestamp     TIMESTAMP    NOT NULL,
    creado_en     TIMESTAMP    NOT NULL,
    CONSTRAINT pk_wa_mensaje_grupo PRIMARY KEY (id),
    CONSTRAINT uk_wa_mensaje_grupo_wa_message_id UNIQUE (wa_message_id)
);

CREATE INDEX IF NOT EXISTS idx_wa_mensaje_grupo_grupo
    ON wa_mensaje_grupo (grupo_id, timestamp);

CREATE TABLE IF NOT EXISTS wa_ajustes (
    id                  VARCHAR(36)  NOT NULL,
    auto_asignar        BOOLEAN      NOT NULL DEFAULT FALSE,
    bienvenida_activa   BOOLEAN      NOT NULL DEFAULT FALSE,
    bienvenida_texto    VARCHAR(2000),
    horario_activo      BOOLEAN      NOT NULL DEFAULT FALSE,
    horario_inicio      VARCHAR(5),
    horario_fin         VARCHAR(5),
    horario_dias        VARCHAR(30),
    fuera_horario_texto VARCHAR(2000),
    csat_activo         BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_wa_ajustes PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS wa_plantilla (
    id        VARCHAR(36)  NOT NULL,
    titulo    VARCHAR(120) NOT NULL,
    contenido VARCHAR(2000) NOT NULL,
    creado_en TIMESTAMP    NOT NULL,
    CONSTRAINT pk_wa_plantilla PRIMARY KEY (id)
);

-- Bots de n8n (Agent Bot estilo Chatwoot): webhook saliente + token de respuesta.
CREATE TABLE IF NOT EXISTS wa_bot (
    id                VARCHAR(36)  NOT NULL,
    nombre            VARCHAR(100) NOT NULL,
    canal_id          VARCHAR(36),
    webhook_url       VARCHAR(500) NOT NULL,
    api_access_token  VARCHAR(100) NOT NULL,
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en         TIMESTAMP    NOT NULL,
    actualizado_en    TIMESTAMP    NOT NULL,
    CONSTRAINT pk_wa_bot PRIMARY KEY (id),
    CONSTRAINT uk_wa_bot_api_access_token UNIQUE (api_access_token)
);

CREATE INDEX IF NOT EXISTS idx_wa_bot_canal
    ON wa_bot (canal_id);

-- AI Assistant tables (add-crm-ai-assistant-spring-ai, PR 2)
-- Idempotent: safe on fresh databases and repeated startups.
-- Table name: ai_accion (not ai_accion_propuesta) — matches the
-- domain class AiAccion after the PR 1 naming cleanup. The previous
-- name was never deployed, so no backward-compat migration is needed.
CREATE TABLE IF NOT EXISTS ai_accion (
    id                      VARCHAR(36)  NOT NULL,
    empresa_id              VARCHAR(36)  NOT NULL,
    solicitada_por          VARCHAR(36)  NOT NULL,
    wa_conversacion_id      VARCHAR(64)  NOT NULL,
    wa_mensaje_id           VARCHAR(64),
    ai_conversacion_id      VARCHAR(36)  NOT NULL DEFAULT '',
    tipo_accion             VARCHAR(50)  NOT NULL,
    estado                  VARCHAR(20)  NOT NULL,
    payload_json            TEXT         NOT NULL,
    rationale               TEXT         NOT NULL,
    version                 INT          NOT NULL,
    expires_at              TIMESTAMP,
    resultado_entidad_id    VARCHAR(64),
    error_reason            TEXT,
    creado_en               TIMESTAMP    NOT NULL,
    actualizado_en          TIMESTAMP    NOT NULL,
    CONSTRAINT pk_ai_accion PRIMARY KEY (id),
    CONSTRAINT ck_ai_accion_estado CHECK (estado IN
        ('PENDING','CONFIRMED','REJECTED','EXPIRED','EXECUTED','FAILED'))
);

-- Audit-link fields added after PR1/PR2 verification: every proposal must
-- store the source AI conversation id (required) and the optional source
-- WhatsApp message id. Additive: safe on existing databases (legacy rows
-- keep ai_conversacion_id='' if they predate this change; the application
-- layer always sets a real UUID on new proposals via AiAccionMapper).
ALTER TABLE ai_accion ADD COLUMN IF NOT EXISTS wa_mensaje_id VARCHAR(64);
ALTER TABLE ai_accion ADD COLUMN IF NOT EXISTS ai_conversacion_id VARCHAR(36) NOT NULL DEFAULT '';

CREATE INDEX IF NOT EXISTS idx_ai_accion_solicitada_por_estado
    ON ai_accion (solicitada_por, estado);

CREATE INDEX IF NOT EXISTS idx_ai_accion_expires_at_estado
    ON ai_accion (expires_at, estado);

CREATE INDEX IF NOT EXISTS idx_ai_accion_empresa_wa_conv
    ON ai_accion (empresa_id, wa_conversacion_id);

CREATE INDEX IF NOT EXISTS idx_ai_accion_ai_conversacion_id
    ON ai_accion (ai_conversacion_id);

-- AI conversation table (PR 2, Phase 3).
-- One row per AI assistant session, scoped to (actor, empresa, wa_conversacion, contacto?).
CREATE TABLE IF NOT EXISTS ai_conversacion (
    id                  VARCHAR(36)  NOT NULL,
    empresa_id          VARCHAR(36)  NOT NULL,
    actor_usuario_id    VARCHAR(36)  NOT NULL,
    wa_conversacion_id  VARCHAR(64)  NOT NULL,
    contacto_id         VARCHAR(36),
    archivada           BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_en           TIMESTAMP    NOT NULL,
    actualizado_en      TIMESTAMP    NOT NULL,
    CONSTRAINT pk_ai_conversacion PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_ai_conversacion_actor_wa_conv
    ON ai_conversacion (actor_usuario_id, wa_conversacion_id);

CREATE INDEX IF NOT EXISTS idx_ai_conversacion_empresa_actualizado
    ON ai_conversacion (empresa_id, actualizado_en);

-- AI message table (PR 2, Phase 3). One row per turn inside an AI
-- conversation (user, assistant, system or tool). Never replaces
-- wa_mensaje as the canonical WhatsApp transcript.
CREATE TABLE IF NOT EXISTS ai_mensaje (
    id                  VARCHAR(36)  NOT NULL,
    ai_conversacion_id  VARCHAR(36)  NOT NULL,
    rol                 VARCHAR(20)  NOT NULL,
    contenido           TEXT         NOT NULL,
    modelo              VARCHAR(100),
    prompt_tokens       INT,
    completion_tokens   INT,
    latency_ms          BIGINT,
    tool_call_json      TEXT,
    creado_en           TIMESTAMP    NOT NULL,
    CONSTRAINT pk_ai_mensaje PRIMARY KEY (id),
    CONSTRAINT ck_ai_mensaje_rol CHECK (rol IN ('USER','ASSISTANT','SYSTEM','TOOL'))
);

CREATE INDEX IF NOT EXISTS idx_ai_mensaje_conversacion_creado
    ON ai_mensaje (ai_conversacion_id, creado_en);

-- AI context summary table (PR 2, Phase 3). Latest summary per AI
-- conversation; the application layer decides whether it must be
-- regenerated based on the source_watermark monotonic counter.
CREATE TABLE IF NOT EXISTS ai_resumen_contexto (
    id                  VARCHAR(36)  NOT NULL,
    actor_usuario_id    VARCHAR(36)  NOT NULL,
    empresa_id          VARCHAR(36)  NOT NULL,
    wa_conversacion_id  VARCHAR(64)  NOT NULL,
    contacto_id         VARCHAR(36),
    facts               TEXT         NOT NULL,
    inferences          TEXT         NOT NULL,
    source_wa_mensaje_id VARCHAR(64),
    source_watermark    BIGINT       NOT NULL,
    ai_conversacion_id  VARCHAR(36)  NOT NULL,
    creado_en           TIMESTAMP    NOT NULL,
    actualizado_en      TIMESTAMP    NOT NULL,
    CONSTRAINT pk_ai_resumen_contexto PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_ai_resumen_conversacion_actualizado
    ON ai_resumen_contexto (ai_conversacion_id, actualizado_en);

-- AI memory table (PR 2, Phase 3). One atomic idea per row. Memory
-- is always private to (actor, empresa, wa_conversacion OR contacto).
CREATE TABLE IF NOT EXISTS ai_memoria (
    id                  VARCHAR(36)  NOT NULL,
    actor_usuario_id    VARCHAR(36)  NOT NULL,
    empresa_id          VARCHAR(36)  NOT NULL,
    wa_conversacion_id  VARCHAR(64),
    contacto_id         VARCHAR(36),
    visibilidad         VARCHAR(30)  NOT NULL,
    contenido           TEXT         NOT NULL,
    origen_tipo         VARCHAR(30)  NOT NULL,
    origen_id           VARCHAR(64),
    version             BIGINT       NOT NULL,
    creado_en           TIMESTAMP    NOT NULL,
    actualizado_en      TIMESTAMP    NOT NULL,
    expires_at          TIMESTAMP    NOT NULL,
    superseded_by       VARCHAR(36),
    superseded          BOOLEAN      NOT NULL DEFAULT FALSE,
    expirada            BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_ai_memoria PRIMARY KEY (id),
    CONSTRAINT ck_ai_memoria_visibilidad CHECK (visibilidad IN ('CONVERSACION_SCOPED','CONTACTO_SCOPED'))
);

CREATE INDEX IF NOT EXISTS idx_ai_memoria_scope_activa
    ON ai_memoria (actor_usuario_id, empresa_id, wa_conversacion_id, superseded, expirada);

CREATE INDEX IF NOT EXISTS idx_ai_memoria_expires_at
    ON ai_memoria (expires_at);
