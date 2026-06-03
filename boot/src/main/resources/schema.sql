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
