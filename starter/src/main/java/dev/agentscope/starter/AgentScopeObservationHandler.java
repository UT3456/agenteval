package dev.agentscope.starter;
import io.micrometer.observation.*;
import io.opentelemetry.api.trace.Span;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.tool.observation.ToolCallingObservationContext;
import java.time.Instant;
import java.util.*;
public class AgentScopeObservationHandler implements ObservationHandler<Observation.Context> {
 private static final String KEY=AgentScopeObservationHandler.class.getName();
 private record State(String trace,String span,String parent,Instant at,long nano){}
 private final AgentScopeProperties config; private final TelemetryExporter exporter; private final Redactor redactor;
 public AgentScopeObservationHandler(AgentScopeProperties p,TelemetryExporter e,Redactor r){config=p;exporter=e;redactor=r;}
 public boolean supportsContext(Observation.Context c){return c.getName()!=null&&(c.getName().startsWith("spring.ai.")||c.getName().startsWith("gen_ai.")||c.getName().startsWith("agentscope."));}
 private static String id(){return UUID.randomUUID().toString().replace("-","");}
 public void onStart(Observation.Context c){try{State parent=null;var p=c.getParentObservation();while(p!=null&&parent==null){parent=p.getContextView().get(KEY);p=p.getContextView().getParentObservation();}var otel=Span.current().getSpanContext();String trace=parent!=null?parent.trace():otel.isValid()?otel.getTraceId():id();c.put(KEY,new State(trace,id().substring(0,16),parent!=null?parent.span():otel.isValid()?otel.getSpanId():null,Instant.now(),System.nanoTime()));}catch(RuntimeException ignored){}}
 public void onStop(Observation.Context c){try{State s=c.get(KEY);if(s==null)return;var attrs=new HashMap<String,String>();c.getLowCardinalityKeyValues().forEach(k->attrs.put(k.getKey(),k.getValue()));
 String name=c.getContextualName()!=null?c.getContextualName():c.getName(),kind="AGENT",input=null,output=null,model=null;long it=0,ot=0;
 if(c instanceof ToolCallingObservationContext tool){kind="TOOL";name=tool.getToolDefinition().name();attrs.put("schema",tool.getToolDefinition().inputSchema());if(config.getTracing().isCaptureInputs())input=tool.getToolCallArguments();if(config.getTracing().isCaptureOutputs())output=tool.getToolCallResult();}
 else if(c instanceof ChatModelObservationContext chat){kind="LLM";if(config.getTracing().isCaptureInputs())input=chat.getRequest().getContents();if(chat.getResponse()!=null){var response=chat.getResponse();model=response.getMetadata().getModel();var usage=response.getMetadata().getUsage();if(usage!=null){it=usage.getPromptTokens()==null?0:usage.getPromptTokens();ot=usage.getCompletionTokens()==null?0:usage.getCompletionTokens();}if(config.getTracing().isCaptureOutputs()&&response.getResult()!=null)output=response.getResult().getOutput().getText();}}
 else if(c.getName().contains("advisor"))kind="ADVISOR";else if(c.getName().contains("vector"))kind="RETRIEVAL";
 var price=model==null?null:config.getPricing().get(model);double cost=price==null?0:(it*price.inputPerMillion()+ot*price.outputPerMillion())/1000000;
 Throwable error=c.getError();var span=new SpanData(s.trace(),s.span(),s.parent(),config.getServiceName(),name,kind,s.at(),(System.nanoTime()-s.nano())/1000000d,error==null?"SUCCESS":"ERROR",error==null?null:error.getClass().getSimpleName()+": "+error.getMessage(),input,output,model,it,ot,cost,attrs);
 exporter.offer(redactor.clean(span));}catch(RuntimeException ignored){exporter.dropped.incrementAndGet();}}
}