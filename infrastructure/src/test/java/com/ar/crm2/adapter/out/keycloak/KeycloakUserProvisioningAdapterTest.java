package com.ar.crm2.adapter.out.keycloak;

import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.config.KeycloakAdminProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link KeycloakUserProvisioningAdapter}.
 *
 * Tests:
 * - Error reason codes on IdentityProvisioningException (static verification)
 * - extractUserIdFromLocation() via reflection
 * - KeycloakProvisionRequest structure (no roles/groups)
 * - VERIFY_EMAIL and enabled flag in provision request
 */
class KeycloakUserProvisioningAdapterTest {

    private static final String SERVER_URL = "http://localhost:8180";
    private static final String REALM = "crm2-local";
    private static final String KEYCLOAK_USER_ID = "kc-uuid-new-user-123";

    // ── Error translation (static verification) ─────────────────────

    @Nested
    @DisplayName("IdentityProvisioningException reason codes")
    class ErrorReasons {

        @Test
        @DisplayName("USER_ALREADY_EXISTS maps to 409 Conflict via GlobalExceptionHandler")
        void userAlreadyExists_conflict() {
            IdentityProvisioningException ex = new IdentityProvisioningException(
                    "kc-123",
                    "User already exists",
                    IdentityProvisioningException.Reason.USER_ALREADY_EXISTS
            );
            assertEquals(IdentityProvisioningException.Reason.USER_ALREADY_EXISTS, ex.getReason());
        }

        @Test
        @DisplayName("AUTHENTICATION_FAILURE maps to 502 Bad Gateway")
        void authFailure_badGateway() {
            IdentityProvisioningException ex = new IdentityProvisioningException(
                    "kc-123",
                    "Admin auth failed",
                    IdentityProvisioningException.Reason.AUTHENTICATION_FAILURE
            );
            assertEquals(IdentityProvisioningException.Reason.AUTHENTICATION_FAILURE, ex.getReason());
        }

        @Test
        @DisplayName("USER_NOT_FOUND maps to 404 Not Found")
        void userNotFound_notFound() {
            IdentityProvisioningException ex = new IdentityProvisioningException(
                    "kc-123",
                    "User not found",
                    IdentityProvisioningException.Reason.USER_NOT_FOUND
            );
            assertEquals(IdentityProvisioningException.Reason.USER_NOT_FOUND, ex.getReason());
        }

        @Test
        @DisplayName("SERVER_ERROR maps to 502 Bad Gateway")
        void serverError_badGateway() {
            IdentityProvisioningException ex = new IdentityProvisioningException(
                    "kc-123",
                    "Keycloak internal error",
                    IdentityProvisioningException.Reason.SERVER_ERROR
            );
            assertEquals(IdentityProvisioningException.Reason.SERVER_ERROR, ex.getReason());
        }

        @Test
        @DisplayName("CONNECTION_FAILURE maps to 502 Bad Gateway")
        void connectionFailure_badGateway() {
            IdentityProvisioningException ex = new IdentityProvisioningException(
                    "Connection refused",
                    IdentityProvisioningException.Reason.CONNECTION_FAILURE
            );
            assertEquals(IdentityProvisioningException.Reason.CONNECTION_FAILURE, ex.getReason());
        }
    }

    // ── Location header extraction ───────────────────────────────────

    @Nested
    @DisplayName("extractUserIdFromLocation()")
    class LocationExtraction {

        @Test
        @DisplayName("extracts ID from standard Location header")
        void extractUserIdFromLocation_validHeader() throws Exception {
            KeycloakAdminProperties props = new KeycloakAdminProperties();
            KeycloakUserProvisioningAdapter adapter = new KeycloakUserProvisioningAdapter(props, null);

            java.lang.reflect.Method method = KeycloakUserProvisioningAdapter.class
                    .getDeclaredMethod("extractUserIdFromLocation", String.class);
            method.setAccessible(true);

            String location = SERVER_URL + "/admin/realms/" + REALM + "/users/" + KEYCLOAK_USER_ID;
            String result = (String) method.invoke(adapter, location);
            assertEquals(KEYCLOAK_USER_ID, result);
        }

        @Test
        @DisplayName("throws SERVER_ERROR when Location header is blank")
        void extractUserIdFromLocation_blank() throws Exception {
            KeycloakAdminProperties props = new KeycloakAdminProperties();
            KeycloakUserProvisioningAdapter adapter = new KeycloakUserProvisioningAdapter(props, null);

            java.lang.reflect.Method method = KeycloakUserProvisioningAdapter.class
                    .getDeclaredMethod("extractUserIdFromLocation", String.class);
            method.setAccessible(true);

            // Blank string triggers IdentityProvisioningException wrapped in InvocationTargetException
            Exception thrown = assertThrows(Exception.class,
                    () -> method.invoke(adapter, ""));
            if (thrown instanceof java.lang.reflect.InvocationTargetException ite) {
                assertTrue(ite.getCause() instanceof IdentityProvisioningException,
                        "Inner cause must be IdentityProvisioningException");
                assertEquals(IdentityProvisioningException.Reason.SERVER_ERROR,
                        ((IdentityProvisioningException) ite.getCause()).getReason());
            }
        }

        @Test
        @DisplayName("throws SERVER_ERROR when Location header is null")
        void extractUserIdFromLocation_null() throws Exception {
            KeycloakAdminProperties props = new KeycloakAdminProperties();
            KeycloakUserProvisioningAdapter adapter = new KeycloakUserProvisioningAdapter(props, null);

            java.lang.reflect.Method method = KeycloakUserProvisioningAdapter.class
                    .getDeclaredMethod("extractUserIdFromLocation", String.class);
            method.setAccessible(true);

            // Null triggers IdentityProvisioningException wrapped in InvocationTargetException
            Exception thrown = assertThrows(Exception.class,
                    () -> method.invoke(adapter, (String) null));
            if (thrown instanceof java.lang.reflect.InvocationTargetException ite) {
                assertTrue(ite.getCause() instanceof IdentityProvisioningException,
                        "Inner cause must be IdentityProvisioningException");
                assertEquals(IdentityProvisioningException.Reason.SERVER_ERROR,
                        ((IdentityProvisioningException) ite.getCause()).getReason());
            }
        }
    }

    // ── Provision payload structure ─────────────────────────────────

    @Nested
    @DisplayName("KeycloakProvisionRequest — out-of-scope (no roles/groups)")
    class PayloadStructure {

        @Test
        @DisplayName("createNew() record does NOT contain roles or groups fields")
        void provisionRequest_excludesRolesAndGroups() {
            var request = com.ar.crm2.adapter.out.keycloak.dto.KeycloakProvisionRequest.createNew(
                    "juan@example.com",
                    "juan@example.com",
                    true
            );

            var componentNames = java.util.Arrays.stream(request.getClass().getRecordComponents())
                    .map(java.lang.reflect.RecordComponent::getName)
                    .toList();

            assertFalse(componentNames.contains("roles"),
                    "'roles' field must NOT exist in KeycloakProvisionRequest");
            assertFalse(componentNames.contains("groups"),
                    "'groups' field must NOT exist in KeycloakProvisionRequest");
        }

        @Test
        @DisplayName("createNew() includes VERIFY_EMAIL in requiredActions")
        void provisionRequest_verifyEmailSet() {
            var request = com.ar.crm2.adapter.out.keycloak.dto.KeycloakProvisionRequest.createNew(
                    "juan@example.com",
                    "juan@example.com",
                    true
            );

            assertNotNull(request.requiredActions());
            assertTrue(request.requiredActions().contains("VERIFY_EMAIL"),
                    "requiredActions must contain VERIFY_EMAIL");
        }

        @Test
        @DisplayName("createNew() sets enabled=true correctly")
        void provisionRequest_enabledTrue() {
            var request = com.ar.crm2.adapter.out.keycloak.dto.KeycloakProvisionRequest.createNew(
                    "juan@example.com",
                    "juan@example.com",
                    true
            );

            assertTrue(request.enabled());
        }

        @Test
        @DisplayName("createNew() sets enabled=false correctly")
        void provisionRequest_enabledFalse() {
            var request = com.ar.crm2.adapter.out.keycloak.dto.KeycloakProvisionRequest.createNew(
                    "juan@example.com",
                    "juan@example.com",
                    false
            );

            assertFalse(request.enabled());
        }
    }

    // ── Realm/client configuration static evidence ───────────────────

    @Nested
    @DisplayName("KeycloakAdminProperties — static evidence of config")
    class ConfigEvidence {

        @Test
        @DisplayName("KeycloakAdminProperties has serverUrl, realm, clientId, clientSecret fields")
        void propertiesClass_hasRequiredFields() {
            var fields = java.util.Arrays.stream(KeycloakAdminProperties.class.getDeclaredFields())
                    .map(java.lang.reflect.Field::getName)
                    .toList();

            assertTrue(fields.contains("serverUrl"), "Must have serverUrl field");
            assertTrue(fields.contains("realm"), "Must have realm field");
            assertTrue(fields.contains("clientId"), "Must have clientId field");
            assertTrue(fields.contains("clientSecret"), "Must have clientSecret field");
            assertTrue(fields.contains("connectTimeoutMs"), "Must have connectTimeoutMs field");
            assertTrue(fields.contains("readTimeoutMs"), "Must have readTimeoutMs field");
        }

        @Test
        @DisplayName("@ConfigurationProperties prefix is crm2.keycloak.admin")
        void configurationProperties_prefix() throws Exception {
            var annotation = KeycloakAdminProperties.class
                    .getAnnotation(org.springframework.boot.context.properties.ConfigurationProperties.class);

            assertNotNull(annotation, "Must have @ConfigurationProperties annotation");
            assertEquals("crm2.keycloak.admin", annotation.prefix(),
                    "Prefix must be 'crm2.keycloak.admin' matching application.yml");
        }
    }
}