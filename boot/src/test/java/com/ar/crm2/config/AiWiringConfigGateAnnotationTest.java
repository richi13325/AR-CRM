package com.ar.crm2.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Annotation;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reflection-based proof that the AI assistant bean graph is gated
 * by the {@code ai-assistant.enabled} master kill-switch.
 *
 * <p>This unit test does NOT load any Spring context — it inspects
 * the {@link AiWiringConfig} class directly via reflection so it
 * stays fast and isolated from the existing boot composition-root
 * tests (which have separate, pre-existing wiring brittleness
 * outside the PR4 corrective slice scope).
 *
 * <p>The {@link ConditionalOnProperty} annotation on
 * {@link AiWiringConfig} is the load-bearing guard that closes the
 * blocker-4 regression introduced by the previous PR4 wiring
 * attempt (AI beans were unconditionally created regardless of the
 * flag, breaking {@code FichaWiringTest}).
 */
class AiWiringConfigGateAnnotationTest {

    @Test
    void aiWiringConfig_isAnnotatedWithConditionalOnPropertyMatchingAiAssistantEnabledFlag() {
        Optional<ConditionalOnProperty> annotation = findAnnotation(
            AiWiringConfig.class, ConditionalOnProperty.class
        );

        assertThat(annotation)
            .as("AiWiringConfig MUST carry @ConditionalOnProperty so the master "
                + "kill-switch ai-assistant.enabled actually disables the AI surface")
            .isPresent();

        ConditionalOnProperty a = annotation.get();
        assertThat(a.name())
            .as("@ConditionalOnProperty name MUST be the ai-assistant.enabled flag")
            .containsExactly("ai-assistant.enabled");
        assertThat(a.havingValue())
            .as("@ConditionalOnProperty MUST require havingValue=true")
            .isEqualTo("true");
        assertThat(a.matchIfMissing())
            .as("@ConditionalOnProperty matchIfMissing MUST be false so the default "
                + "ai-assistant.enabled=false keeps the AI surface disabled")
            .isFalse();
    }

    @Test
    void wiringConfig_isAnnotatedWithImportingAiWiringConfig() throws Exception {
        // The AI beans only enter the boot context when AiWiringConfig
        // is imported by WiringConfig. Verify the import is present so
        // enabling the flag actually wires the AI graph.
        Class<?> wiringConfigClass = WiringConfig.class;
        org.springframework.context.annotation.Import importAnnotation =
            wiringConfigClass.getAnnotation(org.springframework.context.annotation.Import.class);
        assertThat(importAnnotation)
            .as("WiringConfig MUST @Import(AiWiringConfig.class) so the AI graph "
                + "is part of the boot context when the flag is true")
            .isNotNull();
        assertThat(importAnnotation.value())
            .as("WiringConfig's @Import must reference AiWiringConfig")
            .contains(AiWiringConfig.class);
    }

    private static <A extends Annotation> Optional<A> findAnnotation(
            Class<?> type, Class<A> annotationType
    ) {
        A direct = type.getAnnotation(annotationType);
        if (direct != null) {
            return Optional.of(direct);
        }
        for (Annotation meta : type.getAnnotations()) {
            A found = meta.annotationType().getAnnotation(annotationType);
            if (found != null) {
                return Optional.of(found);
            }
        }
        return Optional.empty();
    }
}
