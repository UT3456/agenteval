package dev.agentscope.starter;
import java.util.*;
import java.util.regex.Pattern;
/** Best-effort masking. Never a guarantee that arbitrary private content is safe. */
public final class Redactor {
 private final List<Pattern> patterns=new ArrayList<>();
 public Redactor(List<String> custom) {
  for(String s:List.of("(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}","(?i)Bearer\\s+[a-z0-9._~+/=-]+","(?i)(?:api[_-]?key|password|secret|token)[\\\"']?\\s*[:=]\\s*[\\\"']?[^\\s\\\"',}]+","(?<![\\d.])\\+?\\d[\\d ()-]{8,}\\d(?![\\d.])","sk-[A-Za-z0-9_-]{12,}")) patterns.add(Pattern.compile(s));
  for(String s:custom) patterns.add(Pattern.compile(s));
 }
 public String clean(String value){if(value==null)return null; String s=value.length()>32768?value.substring(0,32768):value;for(Pattern p:patterns)s=p.matcher(s).replaceAll("[REDACTED]");return s;}
 public SpanData clean(SpanData s){var a=new HashMap<String,String>();if(s.attributes()!=null)s.attributes().forEach((k,v)->a.put(clean(k),clean(v)));return new SpanData(s.traceId(),s.spanId(),s.parentSpanId(),clean(s.service()),clean(s.name()),s.kind(),s.startedAt(),s.durationMs(),s.status(),clean(s.error()),clean(s.input()),clean(s.output()),clean(s.model()),s.inputTokens(),s.outputTokens(),s.cost(),a);}
}