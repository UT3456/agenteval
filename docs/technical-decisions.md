# Verified framework decisions

Research date: 2026-10-22. Maven Central metadata reports stable Boot 4.1.2, Spring AI 2.0.2, Micrometer 1.17.2, OpenTelemetry 1.67.0, Flyway 13.11.1, Testcontainers 2.0.5. Latest does not imply compatible.

We select Java 21, Boot **3.5.16**, AI **1.1.8**, with Boot-managed infrastructure versions. The official Spring AI 1.1 getting-started documentation explicitly supports Boot 3.4/3.5. Boot 4 / AI 2 is a separate compatibility track, not claimed supported by this starter. Maven resolution and tests are the final authority for the selected artifacts.

Sources: [Spring AI observation docs source](https://github.com/spring-projects/spring-ai/blob/1.1.x/spring-ai-docs/src/main/antora/modules/ROOT/pages/observability/index.adoc), [compatibility](https://github.com/spring-projects/spring-ai/blob/1.1.x/spring-ai-docs/src/main/antora/modules/ROOT/pages/getting-started.adoc), [Maven Central](https://repo.maven.apache.org/maven2/). The docs.spring.io host was blocked in the build environment; official repository documentation was read instead.

## Instrumentation

Actuator registers ObservationHandler beans. We consume existing observations; no business-method proxies or model wrappers are installed. ChatClient emits spring.ai.chat.client; advisors emit spring.ai.advisor; supported model providers emit gen_ai.client.operation; DefaultToolCallingManager emits spring.ai.tool. Vector stores expose their own observations. ToolCallingObservationContext exposes the tool definition, arguments and result. ChatModelObservationContext exposes Prompt and ChatResponse, including provider-reported usage. Typed contexts allow opt-in capture without enabling unsafe Spring AI payload logging. No arbitrary high-cardinality baggage is exported.

Input/output capture defaults off. Masking always runs before queueing and again at ingestion; redaction.enabled=false does not disable mandatory baseline masking. Custom regexes supplement baseline rules. Regex masking is not complete PII detection. Provider omissions, direct calls bypassing Spring AI, custom models without observation support, and unpropagated asynchronous context cannot be reconstructed. For custom code use an Observation with an agentscope.* name and proper scope. Native OpenTelemetry current trace context is adopted when available; local observation span IDs retain hierarchy. AgentScope does not install a second global OTel SDK or promise OTLP ingestion.

Costs use explicit per-model input/output prices per million tokens; absent prices mean unknown (represented as zero estimate), not a provider billing statement. Raw prompts are never logged by AgentScope.

## Product research

[Langfuse](https://github.com/langfuse/langfuse) provides tracing, prompt management and evaluations; [Phoenix](https://github.com/Arize-ai/phoenix) provides OpenTelemetry observability and evaluation; [LangSmith](https://github.com/langchain-ai/langsmith-sdk) provides tracing and evaluation SDKs. Their official repository READMEs were reviewed. AgentScope differentiates on native Spring Observation contexts, tool-contract assertions and reproducible no-key Java regression demos. It does not claim feature parity or access to a model's internal reasoning.
