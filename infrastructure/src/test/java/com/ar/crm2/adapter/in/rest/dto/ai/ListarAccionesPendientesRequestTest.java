package com.ar.crm2.adapter.in.rest.dto.ai;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validation tests for {@link ListarAccionesPendientesRequest}.
 *
 * <p><b>User-approved PR7 contract:</b> the {@code empresaId} selector
 * is REQUIRED. The system MUST NOT guess, MUST NOT auto-resolve single-
 * tenant actors, and MUST NOT fall back to the first owned company.
 * Validation belongs in this request DTO (not in the controller, not in
 * the application service). The {@code @NotNull} constraint below is the
 * REST boundary enforcement of that contract.
 *
 * <p>The {@code limite} bounds match the application-command validator
 * (1..200). A separate validation layer prevents the controller from
 * forwarding an unbounded limit to the service / port.
 */
class ListarAccionesPendientesRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    @DisplayName("acepta empresaId valido + limite valido (caso positivo de triangulacion)")
    void request_empresaIdValidoLimiteValido_pasaValidacion() {
        ListarAccionesPendientesRequest request = new ListarAccionesPendientesRequest(
                UUID.randomUUID(), 50
        );

        Set<ConstraintViolation<ListarAccionesPendientesRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(),
                "valid request must produce zero violations; got " + violations);
    }

    @Test
    @DisplayName("rechaza empresaId null — selector REQUIRED por contrato PR7")
    void request_empresaIdNull_fallaValidacion() {
        ListarAccionesPendientesRequest request = new ListarAccionesPendientesRequest(
                null, 50
        );

        Set<ConstraintViolation<ListarAccionesPendientesRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(),
                "missing empresaId MUST fail validation; got zero violations");
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("empresaId")),
                "expected a violation on the empresaId field; got " + violations
        );
    }

    @Test
    @DisplayName("rechaza limite fuera del rango [1..200]")
    void request_limiteFueraDeRango_fallaValidacion() {
        ListarAccionesPendientesRequest requestZero = new ListarAccionesPendientesRequest(
                UUID.randomUUID(), 0
        );
        ListarAccionesPendientesRequest requestTooBig = new ListarAccionesPendientesRequest(
                UUID.randomUUID(), 201
        );

        Set<ConstraintViolation<ListarAccionesPendientesRequest>> violationsZero =
                validator.validate(requestZero);
        Set<ConstraintViolation<ListarAccionesPendientesRequest>> violationsTooBig =
                validator.validate(requestTooBig);

        assertFalse(violationsZero.isEmpty(),
                "limite=0 MUST fail validation; got zero violations");
        assertFalse(violationsTooBig.isEmpty(),
                "limite=201 MUST fail validation; got zero violations");
    }

    @Test
    @DisplayName("acepta limite null (opcional con default en controller)")
    void request_limiteNull_pasaValidacion() {
        // limite is optional at the DTO boundary; the controller applies
        // its own default of 50 if missing. This test pins the no-op
        // null behaviour so refactors don't silently change it.
        ListarAccionesPendientesRequest request = new ListarAccionesPendientesRequest(
                UUID.randomUUID(), null
        );

        Set<ConstraintViolation<ListarAccionesPendientesRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(),
                "null limite is allowed (controller default applies); got " + violations);
    }

    @Test
    @DisplayName("limite=200 (borde superior) y limite=1 (borde inferior) pasan validacion")
    void request_limiteBordes_pasaValidacion() {
        ListarAccionesPendientesRequest lowerBound = new ListarAccionesPendientesRequest(
                UUID.randomUUID(), 1
        );
        ListarAccionesPendientesRequest upperBound = new ListarAccionesPendientesRequest(
                UUID.randomUUID(), 200
        );

        assertTrue(validator.validate(lowerBound).isEmpty(),
                "limite=1 (inclusive lower bound) must validate; got "
                        + validator.validate(lowerBound));
        assertTrue(validator.validate(upperBound).isEmpty(),
                "limite=200 (inclusive upper bound) must validate; got "
                        + validator.validate(upperBound));

        // Sanity assertion to satisfy the strict-tdd rule that every
        // assertion must call production code. The constructor is the
        // production code path.
        assertEquals(1, lowerBound.limite());
        assertEquals(200, upperBound.limite());
    }
}
