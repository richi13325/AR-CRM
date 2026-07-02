package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.exception.AccionInvalidaException;
import com.ar.crm2.application.contacto.command.CreateContactoCommand;
import com.ar.crm2.application.ficha.command.MoverColumnaFichaCommand;
import com.ar.crm2.application.tarea.command.CreateTareaCommand;
import com.ar.crm2.application.trato.command.CreateTratoCommand;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoContrato;
import com.ar.crm2.model.enums.TipoTarea;
import com.ar.crm2.model.vo.AiAccionId;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ConfirmarAccionMapper}.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Happy path: each tipo's JSON maps cleanly into the matching
 *       existing CRM command with all fields populated.</li>
 *   <li>Missing required field rejected with {@link AccionInvalidaException}.</li>
 *   <li>Type-coercion errors rejected.</li>
 *   <li>Optional fields map to {@code null} when absent.</li>
 * </ul>
 *
 * <p>Business validation (required UUIDs, name length, etc.) is
 * asserted to live in the existing CRM commands themselves, NOT here —
 * these tests only verify the JSON -> command boundary behaviour.
 */
class ConfirmarAccionMapperTest {

    private final UsuarioId actor = UsuarioId.create();
    private final EmpresaId empresa = EmpresaId.create();
    private final String waConv = "wa-conv-1";

    @Test
    @DisplayName("toCreateContacto mapea todos los campos opcionales y requeridos al CreateContactoCommand")
    void toCreateContacto_happyPath() {
        UUID responsable = UUID.randomUUID();
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":\"Juan\","
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + responsable + "\","
                + "\"telefono\":\"+54911\","
                + "\"correo\":\"juan@x.com\","
                + "\"cargo\":\"CEO\","
                + "\"comoNosConocio\":\"LinkedIn\"}";

        CreateContactoCommand cmd = ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json));

        assertEquals(empresa.value(), cmd.empresaId());
        assertEquals("Juan", cmd.nombre());
        assertEquals("juan@x.com", cmd.correo());
        assertEquals(EstadoRelacion.PROSPECTO, cmd.estadoRelacion());
        assertEquals(responsable, cmd.responsableId());
        assertEquals(actor.value(), cmd.creadoPor(),
                "el creadoPor del comando viene del solicitadaPor del AiAccion, no del JSON");
        assertEquals("+54911", cmd.telefono());
        assertEquals("CEO", cmd.cargo());
        assertEquals("LinkedIn", cmd.comoNosConocio());
    }

    @Test
    @DisplayName("toCreateContacto con campos opcionales ausentes los deja null")
    void toCreateContacto_optionalFieldsNull() {
        UUID responsable = UUID.randomUUID();
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":\"Juan\","
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + responsable + "\"}";

        CreateContactoCommand cmd = ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json));

        assertEquals("Juan", cmd.nombre());
        assertNull(cmd.correo());
        assertNull(cmd.telefono());
        assertNull(cmd.cargo());
        assertNull(cmd.comoNosConocio());
    }

    @Test
    @DisplayName("toCreateContacto con campo obligatorio faltante lanza AccionInvalidaException")
    void toCreateContacto_missingRequired_rechaza() {
        String json = "{\"empresaId\":\"" + empresa.value() + "\"}";
        assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json)));
    }

    @Test
    @DisplayName("toCreateContacto con UUID inválido lanza AccionInvalidaException")
    void toCreateContacto_invalidUuid_rechaza() {
        String json = "{\"empresaId\":\"no-es-uuid\",\"nombre\":\"x\","
                + "\"estadoRelacion\":\"PROSPECTO\",\"responsableId\":\""
                + UUID.randomUUID() + "\"}";
        assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json)));
    }

    @Test
    @DisplayName("toCreateTrato mapea decimal + enum + fecha al CreateTratoCommand")
    void toCreateTrato_happyPath() {
        UUID contactoId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        String json = "{\"contactoId\":\"" + contactoId + "\","
                + "\"responsableId\":\"" + responsableId + "\","
                + "\"nombre\":\"Demo\","
                + "\"valorEstimado\":\"50000.00\","
                + "\"probabilidad\":75,"
                + "\"fechaCierreEsperada\":\"2030-12-31\","
                + "\"tipoContrato\":\"LICENCIA\"}";

        CreateTratoCommand cmd = ConfirmarAccionMapper.toCreateTrato(accion("CREATE_TRATO", json));

        assertEquals(contactoId, cmd.contactoId());
        assertEquals(responsableId, cmd.responsableId());
        assertEquals("Demo", cmd.nombre());
        assertEquals(new BigDecimal("50000.00"), cmd.valorEstimado());
        assertEquals(75, cmd.probabilidad());
        assertEquals("2030-12-31", cmd.fechaCierreEsperada().toString());
        assertEquals(TipoContrato.LICENCIA, cmd.tipoContrato());
    }

    @Test
    @DisplayName("toCreateTarea mapea tipo + prioridad + fechaLimite al CreateTareaCommand")
    void toCreateTarea_happyPath() {
        UUID tratoId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        String json = "{\"tratoId\":\"" + tratoId + "\","
                + "\"responsableId\":\"" + responsableId + "\","
                + "\"titulo\":\"Llamar\","
                + "\"descripcion\":\"Llamar al cliente mañana\","
                + "\"tipo\":\"GENERAL\","
                + "\"prioridad\":\"ALTA\","
                + "\"fechaLimite\":\"2030-12-31T10:00\"}";

        CreateTareaCommand cmd = ConfirmarAccionMapper.toCreateTarea(accion("CREATE_TAREA", json));

        assertEquals(tratoId, cmd.tratoId());
        assertEquals(responsableId, cmd.responsableId());
        assertEquals("Llamar", cmd.titulo());
        assertEquals("Llamar al cliente mañana", cmd.descripcion());
        assertEquals(TipoTarea.GENERAL, cmd.tipo());
        assertEquals(PrioridadTarea.ALTA, cmd.prioridad());
        assertNotNull(cmd.fechaLimite());
    }

    @Test
    @DisplayName("toMoverFicha mapea ambos UUIDs al MoverColumnaFichaCommand")
    void toMoverFicha_happyPath() {
        UUID fichaId = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        String json = "{\"fichaId\":\"" + fichaId + "\","
                + "\"targetColumnaId\":\"" + target + "\"}";

        MoverColumnaFichaCommand cmd = ConfirmarAccionMapper.toMoverFicha(accion("MOVE_KANBAN_FICHA", json));

        assertEquals(fichaId, cmd.fichaId());
        assertEquals(target, cmd.targetColumnaId());
    }

    @Test
    @DisplayName("toMoverFicha con UUID faltante lanza AccionInvalidaException")
    void toMoverFicha_missingField_rechaza() {
        String json = "{\"fichaId\":\"" + UUID.randomUUID() + "\"}";
        assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toMoverFicha(accion("MOVE_KANBAN_FICHA", json)));
    }

    // ── JSON parser robustness (Phase 12 — strict TDD slice) ────────

    @Test
    @DisplayName("parser desescapa comillas escapadas dentro de strings (substring naive fallaba)")
    void toCreateContacto_escapedQuotesInString_areUnescaped() {
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":\"Juan \\\"El Grande\\\"\","
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + UUID.randomUUID() + "\"}";

        CreateContactoCommand cmd = ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json));

        assertEquals("Juan \"El Grande\"", cmd.nombre());
    }

    @Test
    @DisplayName("parser desescapa barras invertidas y escapes de control dentro de strings")
    void toCreateContacto_escapedBackslashesAndControl_areUnescaped() {
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":\"line1\\nline2\\tcol\","
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + UUID.randomUUID() + "\"}";

        CreateContactoCommand cmd = ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json));

        assertEquals("line1\nline2\tcol", cmd.nombre());
    }

    @Test
    @DisplayName("parser desescapa secuencias unicode (\\uXXXX) dentro de strings")
    void toCreateContacto_unicodeEscape_isDecoded() {
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":\"Jos\\u00e9 Mar\\u00eda\","
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + UUID.randomUUID() + "\"}";

        CreateContactoCommand cmd = ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json));

        assertEquals("José María", cmd.nombre());
    }

    @Test
    @DisplayName("parser tolera whitespace arbitrario entre tokens, comas y dos puntos")
    void toCreateContacto_arbitraryWhitespace_isTolerated() {
        UUID responsable = UUID.randomUUID();
        String json = "{ \n  \"empresaId\" : \"" + empresa.value() + "\" ,\n"
                + "  \"nombre\" : \"Juan\" ,\n"
                + "  \"estadoRelacion\" : \"PROSPECTO\" ,\n"
                + "  \"responsableId\" : \"" + responsable + "\" \n} ";

        CreateContactoCommand cmd = ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json));

        assertEquals("Juan", cmd.nombre());
        assertEquals(empresa.value(), cmd.empresaId());
        assertEquals(responsable, cmd.responsableId());
    }

    @Test
    @DisplayName("parser ignora campos desconocidos adicionales en el objeto (objectos/objetos anidados/arrays)")
    void toCreateContacto_unknownExtraFields_areIgnored() {
        UUID responsable = UUID.randomUUID();
        String json = "{\"extra\":\"ignored\",\"meta\":{\"nested\":\"object\"},"
                + "\"tags\":[\"a\",\"b\"],"
                + "\"unknownNumber\":42,"
                + "\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":\"Juan\","
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + responsable + "\"}";

        CreateContactoCommand cmd = ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json));

        assertEquals("Juan", cmd.nombre());
        assertEquals(empresa.value(), cmd.empresaId());
        assertEquals(responsable, cmd.responsableId());
    }

    @Test
    @DisplayName("parser rechaza JSON con sintaxis inválida con AccionInvalidaException controlada (no leak de parser exception)")
    void toCreateContacto_invalidJsonSyntax_rejected() {
        String json = "{\"nombre\":\"Juan\","; // truncated, no closing brace

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json)));
        // Controlled boundary: no parser-level exception type leaks to the caller.
        assertTrue(ex.getMessage().contains("Comando de accion inválido"),
                "expected controlled AccionInvalidaException message, got: " + ex.getMessage());
    }

    @Test
    @DisplayName("parser rechaza campo string cuando el valor es un objeto anidado con AccionInvalidaException")
    void toCreateContacto_stringFieldAsNestedObject_rejected() {
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":{\"first\":\"Juan\"},"
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + UUID.randomUUID() + "\"}";

        assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json)));
    }

    @Test
    @DisplayName("parser rechaza campo string cuando el valor es un array con AccionInvalidaException")
    void toCreateContacto_stringFieldAsArray_rejected() {
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":[\"a\",\"b\"],"
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + UUID.randomUUID() + "\"}";

        assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json)));
    }

    @Test
    @DisplayName("parser acepta null literal como valor de campo opcional ausente")
    void toCreateContacto_nullLiteralOnOptionalField_isTreatedAsNull() {
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":\"Juan\","
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + UUID.randomUUID() + "\","
                + "\"correo\":null,\"telefono\":null,\"cargo\":null,\"comoNosConocio\":null}";

        CreateContactoCommand cmd = ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json));

        assertEquals("Juan", cmd.nombre());
        assertNull(cmd.correo());
        assertNull(cmd.telefono());
        assertNull(cmd.cargo());
        assertNull(cmd.comoNosConocio());
    }

    @Test
    @DisplayName("parser acepta numero entre comillas como string para campos UUID (forma que el modelo puede emitir)")
    void toCreateContacto_quotedNumberForUuid_isRejected() {
        // The contract says UUIDs are required and must be valid UUID strings.
        // A quoted number "75" is not a valid UUID, so the mapper must reject it.
        String json = "{\"empresaId\":\"12345\","
                + "\"nombre\":\"Juan\","
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + UUID.randomUUID() + "\"}";

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json)));
        assertTrue(ex.getMessage().contains("UUID inválido"),
                "expected UUID inválido message, got: " + ex.getMessage());
    }

    // ── Triangulation: other discriminators also benefit from the parser ──

    @Test
    @DisplayName("toCreateTrato tolera whitespace + comillas escapadas en 'nombre'")
    void toCreateTrato_robustJson_isHandled() {
        UUID contactoId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        String json = "{ \n"
                + "  \"contactoId\" : \"" + contactoId + "\",\n"
                + "  \"responsableId\" : \"" + responsableId + "\",\n"
                + "  \"nombre\" : \"Venta \\\"Premium\\\"\",\n"
                + "  \"valorEstimado\" : \"50000.00\",\n"
                + "  \"probabilidad\" : 75,\n"
                + "  \"fechaCierreEsperada\" : \"2030-12-31\",\n"
                + "  \"tipoContrato\" : \"LICENCIA\" \n"
                + "}";

        CreateTratoCommand cmd = ConfirmarAccionMapper.toCreateTrato(accion("CREATE_TRATO", json));

        assertEquals(contactoId, cmd.contactoId());
        assertEquals(responsableId, cmd.responsableId());
        assertEquals("Venta \"Premium\"", cmd.nombre());
        assertEquals(new BigDecimal("50000.00"), cmd.valorEstimado());
        assertEquals(75, cmd.probabilidad());
        assertEquals(TipoContrato.LICENCIA, cmd.tipoContrato());
    }

    @Test
    @DisplayName("toCreateTarea con JSON inválido lanza AccionInvalidaException controlada (no parser exception)")
    void toCreateTarea_invalidJson_rejected() {
        String json = "{\"titulo\":"; // truncated, no value

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateTarea(accion("CREATE_TAREA", json)));
        assertTrue(ex.getMessage().contains("Comando de accion inválido"),
                "expected controlled AccionInvalidaException message, got: " + ex.getMessage());
    }

    @Test
    @DisplayName("toCreateTarea tolera descripcion como string con escapes (acentos y saltos de linea)")
    void toCreateTarea_escapedDescription_isUnescaped() {
        UUID tratoId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        String json = "{\"tratoId\":\"" + tratoId + "\","
                + "\"responsableId\":\"" + responsableId + "\","
                + "\"titulo\":\"Llamar\","
                + "\"descripcion\":\"Llamar al cliente ma\\u00f1ana\\n(antes del cierre)\","
                + "\"tipo\":\"GENERAL\","
                + "\"prioridad\":\"ALTA\"}";

        CreateTareaCommand cmd = ConfirmarAccionMapper.toCreateTarea(accion("CREATE_TAREA", json));

        assertEquals(tratoId, cmd.tratoId());
        assertEquals(responsableId, cmd.responsableId());
        assertEquals("Llamar", cmd.titulo());
        assertEquals("Llamar al cliente mañana\n(antes del cierre)", cmd.descripcion());
    }

    @Test
    @DisplayName("toMoverFicha con targetColumnaId como objeto lanza AccionInvalidaException")
    void toMoverFicha_uuidFieldAsObject_rejected() {
        String json = "{\"fichaId\":\"" + UUID.randomUUID() + "\","
                + "\"targetColumnaId\":{\"nested\":\"object\"}}";

        assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toMoverFicha(accion("MOVE_KANBAN_FICHA", json)));
    }

    // ── Required-null-field rejection (audit gap closing) ──────────

    @Test
    @DisplayName("toCreateContacto con 'nombre': null lanza AccionInvalidaException (no leak de IllegalArgumentException del constructor)")
    void toCreateContacto_requiredNombreAsNullLiteral_throwsControlledAccionInvalida() {
        UUID responsable = UUID.randomUUID();
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":null,"
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + responsable + "\"}";

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json)));
        assertTrue(ex.getMessage().contains("campo obligatorio")
                        && ex.getMessage().contains("nombre"),
                "expected controlled 'campo obligatorio: nombre' message, got: "
                        + ex.getMessage());
    }

    @Test
    @DisplayName("toCreateContacto con 'estadoRelacion': null lanza AccionInvalidaException (enum requerido, no leak)")
    void toCreateContacto_requiredEstadoRelacionAsNullLiteral_throwsControlledAccionInvalida() {
        UUID responsable = UUID.randomUUID();
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":\"Juan\","
                + "\"estadoRelacion\":null,"
                + "\"responsableId\":\"" + responsable + "\"}";

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json)));
        assertTrue(ex.getMessage().contains("campo obligatorio")
                        && ex.getMessage().contains("estadoRelacion"),
                "expected controlled 'campo obligatorio: estadoRelacion' message, got: "
                        + ex.getMessage());
    }

    @Test
    @DisplayName("toCreateTrato con 'nombre': null lanza AccionInvalidaException (no leak de IllegalArgumentException del constructor)")
    void toCreateTrato_requiredNombreAsNullLiteral_throwsControlledAccionInvalida() {
        UUID contactoId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        String json = "{\"contactoId\":\"" + contactoId + "\","
                + "\"responsableId\":\"" + responsableId + "\","
                + "\"nombre\":null,"
                + "\"tipoContrato\":\"LICENCIA\"}";

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateTrato(accion("CREATE_TRATO", json)));
        assertTrue(ex.getMessage().contains("campo obligatorio")
                        && ex.getMessage().contains("nombre"),
                "expected controlled 'campo obligatorio: nombre' message, got: "
                        + ex.getMessage());
    }

    @Test
    @DisplayName("toCreateTrato con 'tipoContrato': null lanza AccionInvalidaException (enum requerido, no leak)")
    void toCreateTrato_requiredTipoContratoAsNullLiteral_throwsControlledAccionInvalida() {
        UUID contactoId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        String json = "{\"contactoId\":\"" + contactoId + "\","
                + "\"responsableId\":\"" + responsableId + "\","
                + "\"nombre\":\"Venta\","
                + "\"tipoContrato\":null}";

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateTrato(accion("CREATE_TRATO", json)));
        assertTrue(ex.getMessage().contains("campo obligatorio")
                        && ex.getMessage().contains("tipoContrato"),
                "expected controlled 'campo obligatorio: tipoContrato' message, got: "
                        + ex.getMessage());
    }

    @Test
    @DisplayName("toCreateTarea con 'titulo': null lanza AccionInvalidaException (no leak de IllegalArgumentException del constructor)")
    void toCreateTarea_requiredTituloAsNullLiteral_throwsControlledAccionInvalida() {
        UUID tratoId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        String json = "{\"tratoId\":\"" + tratoId + "\","
                + "\"responsableId\":\"" + responsableId + "\","
                + "\"titulo\":null,"
                + "\"descripcion\":\"Llamar al cliente\","
                + "\"tipo\":\"GENERAL\","
                + "\"prioridad\":\"ALTA\"}";

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateTarea(accion("CREATE_TAREA", json)));
        assertTrue(ex.getMessage().contains("campo obligatorio")
                        && ex.getMessage().contains("titulo"),
                "expected controlled 'campo obligatorio: titulo' message, got: "
                        + ex.getMessage());
    }

    @Test
    @DisplayName("toCreateTarea con 'descripcion': null lanza AccionInvalidaException (no leak de IllegalArgumentException del constructor)")
    void toCreateTarea_requiredDescripcionAsNullLiteral_throwsControlledAccionInvalida() {
        UUID tratoId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        String json = "{\"tratoId\":\"" + tratoId + "\","
                + "\"responsableId\":\"" + responsableId + "\","
                + "\"titulo\":\"Llamar\","
                + "\"descripcion\":null,"
                + "\"tipo\":\"GENERAL\","
                + "\"prioridad\":\"ALTA\"}";

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateTarea(accion("CREATE_TAREA", json)));
        assertTrue(ex.getMessage().contains("campo obligatorio")
                        && ex.getMessage().contains("descripcion"),
                "expected controlled 'campo obligatorio: descripcion' message, got: "
                        + ex.getMessage());
    }

    @Test
    @DisplayName("toCreateTarea con 'tipo': null lanza AccionInvalidaException (enum requerido, no leak)")
    void toCreateTarea_requiredTipoAsNullLiteral_throwsControlledAccionInvalida() {
        UUID tratoId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        String json = "{\"tratoId\":\"" + tratoId + "\","
                + "\"responsableId\":\"" + responsableId + "\","
                + "\"titulo\":\"Llamar\","
                + "\"descripcion\":\"Llamar al cliente\","
                + "\"tipo\":null,"
                + "\"prioridad\":\"ALTA\"}";

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateTarea(accion("CREATE_TAREA", json)));
        assertTrue(ex.getMessage().contains("campo obligatorio")
                        && ex.getMessage().contains("tipo"),
                "expected controlled 'campo obligatorio: tipo' message, got: "
                        + ex.getMessage());
    }

    @Test
    @DisplayName("toCreateTarea con 'prioridad': null lanza AccionInvalidaException (enum requerido, no leak)")
    void toCreateTarea_requiredPrioridadAsNullLiteral_throwsControlledAccionInvalida() {
        UUID tratoId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        String json = "{\"tratoId\":\"" + tratoId + "\","
                + "\"responsableId\":\"" + responsableId + "\","
                + "\"titulo\":\"Llamar\","
                + "\"descripcion\":\"Llamar al cliente\","
                + "\"tipo\":\"GENERAL\","
                + "\"prioridad\":null}";

        AccionInvalidaException ex = assertThrows(AccionInvalidaException.class,
                () -> ConfirmarAccionMapper.toCreateTarea(accion("CREATE_TAREA", json)));
        assertTrue(ex.getMessage().contains("campo obligatorio")
                        && ex.getMessage().contains("prioridad"),
                "expected controlled 'campo obligatorio: prioridad' message, got: "
                        + ex.getMessage());
    }

    @Test
    @DisplayName("toCreateContacto preserva el contrato opcional: null literal en 'correo' sigue mapeando a null")
    void toCreateContacto_optionalCorreoAsNullLiteral_stillMapsToNull_optionalContractPreserved() {
        UUID responsable = UUID.randomUUID();
        String json = "{\"empresaId\":\"" + empresa.value() + "\","
                + "\"nombre\":\"Juan\","
                + "\"estadoRelacion\":\"PROSPECTO\","
                + "\"responsableId\":\"" + responsable + "\","
                + "\"correo\":null}";

        CreateContactoCommand cmd = ConfirmarAccionMapper.toCreateContacto(accion("CREATE_CONTACTO", json));

        assertEquals("Juan", cmd.nombre());
        assertNull(cmd.correo(), "optional field with null literal must still map to null (no field, no value)");
    }

    // ── helpers ─────────────────────────────────────────────────────

    private AiAccion accion(String tipo, String payloadJson) {
        return AiAccion.reconstitute(
                AiAccionId.create(),
                empresa,
                actor,
                waConv,
                null,
                AiConversacionId.create(),
                tipo,
                payloadJson,
                "r",
                1,
                LocalDateTime.now().plusHours(1),
                null,
                null,
                EstadoAccion.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
