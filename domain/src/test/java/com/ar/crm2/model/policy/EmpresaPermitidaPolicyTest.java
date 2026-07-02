package com.ar.crm2.model.policy;

import com.ar.crm2.exception.TenantScopeViolationException;
import com.ar.crm2.model.vo.EmpresaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link EmpresaPermitidaPolicy}.
 */
class EmpresaPermitidaPolicyTest {

    private final EmpresaId empresa1 = EmpresaId.create();
    private final EmpresaId empresa2 = EmpresaId.create();

    @Nested
    @DisplayName("when an explicit empresaId is supplied")
    class ExplicitEmpresaId {

        @Test
        @DisplayName("returns the requested id when it belongs to the actor")
        void explicitOwnedId_returnsRequested() {
            EmpresaId result = EmpresaPermitidaPolicy.seleccionarEmpresaPermitida(
                    List.of(empresa1, empresa2), empresa2.value()
            );

            assertThat(result).isEqualTo(empresa2);
        }

        @Test
        @DisplayName("throws TenantScopeViolationException when the id is not owned")
        void explicitNotOwned_throws() {
            UUID ajena = UUID.randomUUID();

            assertThatThrownBy(() -> EmpresaPermitidaPolicy.seleccionarEmpresaPermitida(
                    List.of(empresa1, empresa2), ajena
            ))
                    .isInstanceOf(TenantScopeViolationException.class)
                    .hasMessageContaining(ajena.toString());
        }

        @Test
        @DisplayName("throws when owned list is empty")
        void explicitWithEmptyOwned_throws() {
            assertThatThrownBy(() -> EmpresaPermitidaPolicy.seleccionarEmpresaPermitida(
                    List.of(), empresa1.value()
            )).isInstanceOf(TenantScopeViolationException.class);
        }
    }

    @Nested
    @DisplayName("when no explicit empresaId is supplied")
    class NoExplicitEmpresaId {

        @Test
        @DisplayName("returns the first owned company when several are available")
        void noExplicit_returnsFirstOwned() {
            EmpresaId result = EmpresaPermitidaPolicy.seleccionarEmpresaPermitida(
                    List.of(empresa1, empresa2), null
            );

            assertThat(result).isEqualTo(empresa1);
        }

        @Test
        @DisplayName("returns the only owned company when one is available")
        void noExplicit_singleCompany_returnsIt() {
            EmpresaId result = EmpresaPermitidaPolicy.seleccionarEmpresaPermitida(
                    List.of(empresa2), null
            );

            assertThat(result).isEqualTo(empresa2);
        }

        @Test
        @DisplayName("throws when actor owns no companies")
        void noExplicit_noCompanies_throws() {
            assertThatThrownBy(() -> EmpresaPermitidaPolicy.seleccionarEmpresaPermitida(List.of(), null))
                    .isInstanceOf(TenantScopeViolationException.class);
        }
    }

    @Test
    @DisplayName("utility class cannot be instantiated publicly")
    void utilityClass_privateConstructor() throws Exception {
        var ctor = EmpresaPermitidaPolicy.class.getDeclaredConstructor();

        assertThat(Modifier.isPrivate(ctor.getModifiers())).isTrue();
    }
}
