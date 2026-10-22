package dev.agentscope.starter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.*;
@ConfigurationProperties("agentscope")
public class AgentScopeProperties {
 private boolean enabled=true; private String endpoint="http://localhost:8081",serviceName="spring-ai-agent",apiKey="";
 private int queueCapacity=2048; private Tracing tracing=new Tracing(); private Redaction redaction=new Redaction(); private Map<String,Price> pricing=new HashMap<>();
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public String getEndpoint(){return endpoint;} public void setEndpoint(String v){endpoint=v;}
 public String getServiceName(){return serviceName;} public void setServiceName(String v){serviceName=v;}
 public String getApiKey(){return apiKey;} public void setApiKey(String v){apiKey=v;}
 public int getQueueCapacity(){return queueCapacity;} public void setQueueCapacity(int v){queueCapacity=v;}
 public Tracing getTracing(){return tracing;} public Redaction getRedaction(){return redaction;}
 public Map<String,Price> getPricing(){return pricing;} public void setPricing(Map<String,Price> v){pricing=v;}
 public static class Tracing {private boolean captureInputs,captureOutputs; public boolean isCaptureInputs(){return captureInputs;} public void setCaptureInputs(boolean v){captureInputs=v;} public boolean isCaptureOutputs(){return captureOutputs;} public void setCaptureOutputs(boolean v){captureOutputs=v;}}
 public static class Redaction {private boolean enabled=true; private List<String> rules=new ArrayList<>(); public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public List<String> getRules(){return rules;} public void setRules(List<String> v){rules=v;}}
 public record Price(double inputPerMillion,double outputPerMillion) {}
}