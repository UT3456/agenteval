package dev.agentscope.starter;
import java.time.Instant;
import java.util.Map;
public record SpanData(String traceId, String spanId, String parentSpanId, String service, String name,
 String kind, Instant startedAt, double durationMs, String status, String error, String input, String output,
 String model, long inputTokens, long outputTokens, double cost, Map<String,String> attributes) {}
