package com.ar.crm2.adapter.out.ai.spring;

import com.ar.crm2.adapter.out.ai.AiToolContextHolder;
import com.ar.crm2.adapter.out.ai.policy.ToolCallBudgetPolicy;
import com.ar.crm2.adapter.out.ai.policy.TurnTimeoutPolicy;
import com.ar.crm2.adapter.out.ai.spring.mapper.SpringAiChatResponseMapper;
import com.ar.crm2.adapter.out.ai.spring.mapper.SpringAiPromptMapper;
import com.ar.crm2.application.ai.exception.AiAssistantException;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import com.ar.crm2.application.ai.port.out.dto.RespuestaAsistente;
import com.ar.crm2.application.ai.port.out.dto.ChatAsistenteRequest;
import com.ar.crm2.application.ai.port.out.GenerarChatAsistentePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;

/**
 * Spring AI / OpenAI implementation of {@link GenerarChatAsistentePort}.
 *
 * <p><b>After the audit-closing corrective split, this class is
 * intentionally narrow.</b> Its only responsibilities are:
 * <ul>
 *   <li>Ask {@link SpringAiPromptMapper} to build the user-facing
 *       prompt text from the application-owned request DTO.</li>
 *   <li>Bind the trusted {@link AiToolContext} (built from the
 *       application-owned {@link ChatAsistenteRequest}) onto the
 *       <b>current thread</b> via {@link AiToolContextHolder} BEFORE the
 *       {@link ChatClient} call, so any {@code @Tool} callback that
 *       Spring AI invokes synchronously on this thread can resolve
 *       the trusted scope through {@code AiToolContextAdapter}.</li>
 *   <li>Call Spring AI's {@link ChatClient} with that prompt and the
 *       registered tool objects so the model can reason and call
 *       tools. The call is <b>synchronous on the request thread</b> —
 *       see "Thread model" below.</li>
 *   <li>Always clear the holder in a {@code finally} block — including
 *       the failure path — so a pooled Tomcat thread cannot leak one
 *       request's actor/tenant scope into the next.</li>
 *   <li>Measure wall-clock latency and log the result for
 *       traceability.</li>
 *   <li>Catch framework/provider failures and map them to the
 *       application-owned {@link AiAssistantException} — which the
 *       {@code GlobalExceptionHandler} maps to HTTP 502 at the REST
 *       boundary.</li>
 *   <li>Hand the {@link ChatResponse} to
 *       {@link SpringAiChatResponseMapper} for projection back
 *       to the application-owned {@link RespuestaAsistente}.</li>
 * </ul>
 *
 * <p><b>Thread model (audit #1 closing).</b> Spring AI invokes
 * {@code @Tool} methods on the same thread that called
 * {@code ChatClient.call()}, and the production
 * {@link AiToolContextHolder} is a real {@link ThreadLocal}
 * (see {@code ThreadLocalAiToolContextHolder}). The chat call MUST
 * therefore run synchronously on the request thread; an earlier
 * implementation wrapped the call in a
 * {@code CompletableFuture.supplyAsync(...).orTimeout(...)} pair to
 * enforce the per-turn wall-clock budget, but that wrap jumped
 * execution to a {@code ForkJoinPool} thread where the bound
 * context was INVISIBLE — every production {@code @Tool} callback
 * would then call {@code AiToolContextAdapter.resolve()} and
 * {@code IllegalStateException}. The async wrap has been removed;
 * the per-turn wall-clock budget is owned by Spring AI's
 * auto-configured provider HTTP timeout
 * ({@code spring.ai.openai.chat.options.timeout}) instead. See
 * {@code apply-progress.md} "Slice 10 — AI tool-safety honesty
 * correction" and the design decision row in
 * {@code design.md} for the full rationale.
 *
 * <p><b>Per-turn safety limits (honest contract).</b> the adapter
 * accepts two policy collaborators — {@link ToolCallBudgetPolicy}
 * and {@link TurnTimeoutPolicy} — and exposes them as deterministic
 * test seams only:
 * <ul>
 *   <li>{@link #assertToolCallBudget(int)} — public; delegates to
 *       {@code ToolCallBudgetPolicy.assertWithinBudget(int)}. Spring
 *       AI 2.0 does not expose per-tool-call hooks in the
 *       {@code ChatClient} chain, so the policy is surfaced for
 *       verification (and for a future custom {@code CallAdvisor}
 *       follow-up) but it does NOT stop the tool-calling loop at
 *       runtime in this slice. See task {@code 9.10} and the design
 *       memo.</li>
 *   <li>{@link #getTurnTimeoutMillis()} — public; returns the
 *       configured wall-clock budget in milliseconds. The per-turn
 *       HTTP-timeout gate is the Spring AI
 *       {@code spring.ai.openai.chat.options.timeout} property
 *       (parsed as an ISO-8601 duration). Configuring it shorter
 *       than the per-handler wall-clock budget ensures Spring AI
 *       aborts first and surfaces the abort as an
 *       {@link AiAssistantException}.</li>
 * </ul>
 *
 * <p><b>What this adapter does NOT do (and why):</b>
 * <ul>
 *   <li>Prompt formatting — owned by
 *       {@link SpringAiPromptMapper}.</li>
 *   <li>Framework metadata extraction (content, model id, tokens)
 *       — owned by {@link SpringAiChatResponseMapper}.</li>
 *   <li>Business validation, tenant ownership, payload parsing,
 *       AI action semantic checks — owned by application services
 *       and domain entities.</li>
 *   <li>Derive actor / tenant / conv ids from anything other than
 *       the application-owned {@link ChatAsistenteRequest}.</li>
 *   <li>Wrap the chat call in {@code CompletableFuture.supplyAsync}
 *       / {@code .orTimeout}. Removing that wrap was the
 *       audit-closing fix — see "Thread model" above.</li>
 *   <li>Inline real per-tool-call budget enforcement — Spring AI
 *       2.0 does not expose per-tool-call hooks; real enforcement
 *       requires a custom {@code CallAdvisor} injected into the
 *       {@code ChatClient} chain. Tracked as follow-up.</li>
 * </ul>
 *
 * <p>Spring AI types remain confined to this package (adapter +
 * response mapper); the prompt mapper is application-DTO-only. Both
 * mappers live in the {@code adapter.out.ai.spring.mapper}
 * subpackage.
 *
 * <p><b>Configuration:</b> the {@code ChatClient} bean is created
 * by Spring AI's auto-configuration in the boot module and
 * injected here along with {@link SpringAiPromptMapper},
 * {@link SpringAiChatResponseMapper}, the configured {@code modelId}
 * (forwarded from the
 * {@code spring.ai.openai.chat.options.model} property), the
 * {@link AiToolContextHolder} bean (the singleton
 * {@code ThreadLocalAiToolContextHolder} registered in the boot
 * module's composition root), and the two per-turn safety policy
 * collaborators.
 */
@Slf4j
public class OpenAiChatAdapter implements GenerarChatAsistentePort {

    private final ChatClient chatClient;
    private final List<Object> toolObjects;
    private final SpringAiPromptMapper promptMapper;
    private final SpringAiChatResponseMapper responseMapper;
    /**
     * Configured model id (forwarded from the wiring's
     * {@code spring.ai.openai.chat.options.model} property). Kept as
     * a field for trace / metric labels; the adapter does NOT use it
     * as a silent fallback when the framework omits the model id
     * — that is owned by {@link SpringAiChatResponseMapper}.
     */
    @SuppressWarnings("unused")
    private final String modelId;
    /**
     * Infrastructure-owned holder that carries the trusted
     * {@link AiToolContext} from this adapter onto the same thread
     * that {@link ChatClient#call() call()} uses to invoke
     * {@code @Tool} methods. The holder MUST be the same singleton
     * the production {@code AiToolContextAdapter} reads from, which
     * is why the boot module wires a single
     * {@link com.ar.crm2.adapter.out.ai.ThreadLocalAiToolContextHolder}
     * bean and injects it here.
     */
    private final AiToolContextHolder toolContextHolder;
    /**
     * Per-turn tool-call budget policy. Read from
     * {@code ai-assistant.max-tool-calls-per-turn} by
     * {@code AiWiringConfig}. Exposed as a test seam via
     * {@link #assertToolCallBudget(int)} so callers and tests can
     * verify the contract end-to-end without a live LLM. The
     * policy is currently a verified configuration surface only;
     * real loop-stopping enforcement requires a custom
     * {@code CallAdvisor} (see design decision memo).
     */
    private final ToolCallBudgetPolicy toolCallBudgetPolicy;
    /**
     * Per-turn wall-clock timeout policy. Read from
     * {@code ai-assistant.turn-timeout-ms} by {@code AiWiringConfig}.
     * Exposed via {@link #getTurnTimeoutMillis()} as a deterministic
     * seam — the runtime timeout is owned by Spring AI's
     * {@code spring.ai.openai.chat.options.timeout} property so the
     * adapter stays synchronous and the production thread-local
     * context propagates correctly into {@code @Tool} callbacks.
     */
    private final TurnTimeoutPolicy turnTimeoutPolicy;

    /**
     * Audit-#1-closing constructor: takes the two per-turn safety
     * policy collaborators in addition to the prior wiring. The
     * exact shape is pinned by
     * {@code OpenAiChatAdapterTest#constructor_hasEightCollaborators}.
     *
     * <p>The constructor MUST NOT take any further collaborator. A
     * previous revision declared an {@code applyTurnTimeout(...)}
     * seam that invited wrapping the chat call in
     * {@code CompletableFuture.supplyAsync(...).orTimeout(...)} —
     * that wrap broke thread-local context propagation (audit
     * finding #1) and has been removed.
     */
    public OpenAiChatAdapter(
            ChatClient chatClient,
            List<Object> toolObjects,
            SpringAiPromptMapper promptMapper,
            SpringAiChatResponseMapper responseMapper,
            String modelId,
            AiToolContextHolder toolContextHolder,
            ToolCallBudgetPolicy toolCallBudgetPolicy,
            TurnTimeoutPolicy turnTimeoutPolicy
    ) {
        this.chatClient = chatClient;
        this.toolObjects = toolObjects;
        this.promptMapper = promptMapper;
        this.responseMapper = responseMapper;
        this.modelId = modelId;
        this.toolContextHolder = toolContextHolder;
        this.toolCallBudgetPolicy = toolCallBudgetPolicy;
        this.turnTimeoutPolicy = turnTimeoutPolicy;
    }

    /**
     * Test seam: delegates to the configured
     * {@link ToolCallBudgetPolicy#assertWithinBudget(int)}. Public so
     * callers and tests can prove the per-turn tool-call budget is
     * verifiable through a single point.
     *
     * <p><b>Honest contract.</b> Spring AI 2.0 does not expose a
     * per-tool-call hook in the {@code ChatClient} chain, so
     * {@code assertWithinBudget} is exposed for callers and tests
     * but does NOT stop the tool-calling loop in this slice. Real
     * loop-stopping enforcement requires a custom {@code CallAdvisor}
     * (see design decision memo + task {@code 9.10} follow-up).
     *
     * @param toolCallCount the cumulative number of tool calls the
     *                      adapter has dispatched in the current turn.
     * @throws AiAssistantException when the configured budget is
     *                              exceeded.
     */
    public void assertToolCallBudget(int toolCallCount) {
        toolCallBudgetPolicy.assertWithinBudget(toolCallCount);
    }

    /**
     * Test seam: returns the configured
     * {@link TurnTimeoutPolicy#getTurnTimeoutMillis()} so callers
     * and tests can read the current per-turn wall-clock budget.
     *
     * <p><b>Honest contract.</b> the value is a verified
     * configuration knob only. The runtime per-turn wall-clock
     * timeout is enforced by Spring AI's auto-configured HTTP
     * client via {@code spring.ai.openai.chat.options.timeout}.
     * Configuring it shorter than the per-handler budget ensures
     * the framework aborts first and surfaces the abort as an
     * {@link AiAssistantException}.
     *
     * @return the per-turn wall-clock timeout in milliseconds.
     */
    public long getTurnTimeoutMillis() {
        return turnTimeoutPolicy.getTurnTimeoutMillis();
    }

    /**
     * Runs the assistant for the supplied application-owned request.
     *
     * <p><b>Threading.</b> this method runs entirely on the calling
     * thread. The chat client call is synchronous; the only
     * multi-thread contract in this slice is Spring AI's
     * synchronous {@code @Tool} callback dispatch on the same
     * thread, which is what makes the
     * {@link AiToolContextHolder} {@link ThreadLocal} propagation
     * safe. See class-level Javadoc — "Thread model".
     *
     * @param solicitud application-owned request — must not be null
     * @return application-owned response — never contains Spring AI types
     * @throws AiAssistantException when the framework returns an
     *         unexpected or partially-empty response that cannot be
     *         faithfully projected to a {@link RespuestaAsistente},
     *         or when {@code ChatClient.call()} itself fails.
     */
    @Override
    public RespuestaAsistente generar(ChatAsistenteRequest solicitud) {
        long started = System.currentTimeMillis();

        // Prompt construction is delegated to the mapper. The adapter
        // never sees a ChatAsistenteRequest field-by-field — it only
        // forwards the mapper's output to ChatClient.
        String userText = promptMapper.toUserPrompt(solicitud);

        // ── Trusted tool context binding ─────────────────────────────
        // Build the AiToolContext from the application-owned
        // ChatAsistenteRequest (NOT from the model payload) and bind
        // it to the current thread BEFORE the ChatClient call, so any
        // @Tool callback Spring AI invokes synchronously on this thread
        // resolves the trusted scope via AiToolContextAdapter. The
        // try/finally guarantees the holder is cleared on BOTH success
        // and failure paths so a pooled thread cannot leak one
        // request's actor/tenant scope into the next.
        AiToolContext trustedContext = new AiToolContext(
            solicitud.actorUsuarioId(),
            solicitud.empresaId(),
            solicitud.aiConversacionId(),
            solicitud.waConversacionId()
        );
        toolContextHolder.set(trustedContext);
        ChatResponse response;
        try {
            // ── Synchronous ChatClient.call() (audit #1 closing) ─────
            // The call runs on the calling thread so the
            // ThreadLocalAiToolContextHolder binding set above is
            // observable to any @Tool callback Spring AI dispatches on
            // this thread. The per-turn wall-clock budget is enforced
            // by Spring AI's auto-configured provider HTTP timeout
            // (spring.ai.openai.chat.options.timeout). A previous
            // revision wrapped this call in
            // CompletableFuture.supplyAsync(...).orTimeout(...); that
            // wrap jumped execution to a ForkJoinPool thread where the
            // production thread-local context was INVISIBLE — every
            // real @Tool callback would then throw IllegalStateException
            // from AiToolContextAdapter.resolve(). The wrap has been
            // removed; see apply-progress.md Slice 10.
            response = chatClient.prompt()
                .user(userText)
                .tools(toolObjects.toArray())
                .call()
                .chatResponse();
        } catch (RuntimeException ex) {
            // Spring AI / OpenAI provider failures flow to the caller as
            // the application-owned AiAssistantException so the boundary
            // stays framework-free. The original cause is preserved for
            // diagnostics; no Spring AI type crosses the port.
            // GlobalExceptionHandler maps AiAssistantException to HTTP
            // 502 Bad Gateway at the REST boundary (audit #3 closing).
            if (ex instanceof AiAssistantException aiEx) {
                throw aiEx;
            }
            throw AiAssistantException.upstreamFailure(
                "ChatClient call failed: " + ex.getMessage(), ex
            );
        } finally {
            // Always clear — including the failure path — so the next
            // pooled request that lands on this thread starts with no
            // inherited actor/tenant scope. This MUST run after the
            // AiAssistantException mapping so even re-thrown errors
            // leave the holder empty.
            toolContextHolder.clear();
        }

        long latencyMs = System.currentTimeMillis() - started;

        // Response translation is delegated to the response mapper. It
        // owns the structural null/empty checks; if it throws
        // AiAssistantException we let it propagate to the caller.
        RespuestaAsistente out = responseMapper.toResponse(response, latencyMs);

        log.info(
            "AI chat completed: aiConversacionId={} actorUsuarioId={} empresaId={} "
                + "waConversacionId={} model={} latencyMs={} promptTokens={} completionTokens={}",
            solicitud.aiConversacionId(),
            solicitud.actorUsuarioId(),
            solicitud.empresaId(),
            solicitud.waConversacionId(),
            out.modelo(),
            out.latencyMs(),
            out.promptTokens(),
            out.completionTokens()
        );

        return out;
    }
}
