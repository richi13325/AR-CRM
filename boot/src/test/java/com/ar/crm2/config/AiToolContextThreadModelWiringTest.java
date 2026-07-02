package com.ar.crm2.config;

import com.ar.crm2.adapter.out.ai.ThreadLocalAiToolContextHolder;
import com.ar.crm2.adapter.out.ai.spring.OpenAiChatAdapter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reflection-based contract test for the phase-1 AI tool-context carrier.
 *
 * <p>The current AI tool invocation model is explicitly synchronous and
 * boot wiring MUST pin that assumption to the concrete
 * {@link ThreadLocalAiToolContextHolder}. If a future change wants async or
 * reactive tool execution, that change must start with a new design update
 * rather than silently swapping the carrier behind the same boot methods.
 */
class AiToolContextThreadModelWiringTest {

    @Test
    void aiToolContextHolderFactory_isPinnedToConcreteThreadLocalCarrier() throws Exception {
        Method method = AiWiringConfig.class.getMethod("aiToolContextHolder");

        assertThat(method.getReturnType())
            .as("AiWiringConfig.aiToolContextHolder() must declare the concrete "
                + "ThreadLocalAiToolContextHolder return type so the synchronous "
                + "phase-1 carrier is explicit at the composition root")
            .isEqualTo(ThreadLocalAiToolContextHolder.class);

        Object bean = new AiWiringConfig().aiToolContextHolder();
        assertThat(bean)
            .isInstanceOf(ThreadLocalAiToolContextHolder.class);
    }

    @Test
    void aiToolContextAdapterFactory_requiresConcreteThreadLocalCarrier() throws Exception {
        Method method = AiWiringConfig.class.getMethod(
            "aiToolContextAdapter",
            ThreadLocalAiToolContextHolder.class
        );

        assertThat(method.getParameterTypes())
            .containsExactly(ThreadLocalAiToolContextHolder.class);
    }

    @Test
    void openAiChatAdapterFactory_requiresConcreteThreadLocalCarrier() throws Exception {
        Method method = findMethod(
            AiWiringConfig.class,
            "openAiChatAdapter",
            ThreadLocalAiToolContextHolder.class
        );

        assertThat(method)
            .as("AiWiringConfig.openAiChatAdapter(...) must require the concrete "
                + "ThreadLocalAiToolContextHolder so future async/reactive carrier "
                + "changes are an explicit design edit, not a silent bean swap")
            .isNotNull();
        assertThat(method.getReturnType()).isEqualTo(OpenAiChatAdapter.class);
    }

    private static Method findMethod(Class<?> type, String name, Class<?> requiredParameter) {
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(name)) {
                continue;
            }
            for (Class<?> parameterType : method.getParameterTypes()) {
                if (parameterType.equals(requiredParameter)) {
                    return method;
                }
            }
        }
        return null;
    }
}
