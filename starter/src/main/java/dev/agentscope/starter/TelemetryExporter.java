package dev.agentscope.starter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
/** A bounded, lossy exporter: telemetry is never on the business request path. */
public class TelemetryExporter implements AutoCloseable {
 private final ArrayBlockingQueue<SpanData> queue; private final Thread worker; private volatile boolean running=true;
 private final AgentScopeProperties config; private final ObjectMapper mapper; private final HttpClient client;
 public final AtomicLong dropped=new AtomicLong(),delivered=new AtomicLong();
 public TelemetryExporter(AgentScopeProperties config,ObjectMapper mapper){this.config=config;this.mapper=mapper.copy().findAndRegisterModules();queue=new ArrayBlockingQueue<>(Math.max(1,config.getQueueCapacity()));client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();worker=Thread.ofPlatform().daemon().name("agentscope-export").start(this::drain);}
 public void offer(SpanData span){if(!running||!queue.offer(span))dropped.incrementAndGet();}
 private void drain(){while(running||!queue.isEmpty()){try{SpanData s=queue.poll(200,TimeUnit.MILLISECONDS);if(s==null)continue;boolean ok=false;for(int n=0;n<3&&!ok;n++){try{var request=HttpRequest.newBuilder(URI.create(config.getEndpoint()+"/api/v1/traces")).timeout(Duration.ofSeconds(2)).header("Content-Type","application/json").header("X-AgentScope-Key",config.getApiKey()).POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(s))).build();int status=client.send(request,HttpResponse.BodyHandlers.discarding()).statusCode();ok=status>=200&&status<300;if(!ok&&status<500&&status!=429)break;}catch(Exception ignored){}if(!ok)Thread.sleep(100L*(n+1));}if(ok)delivered.incrementAndGet();else dropped.incrementAndGet();}catch(InterruptedException e){Thread.currentThread().interrupt();break;}}}
 public void close(){running=false;try{worker.join(3000);}catch(InterruptedException e){Thread.currentThread().interrupt();}worker.interrupt();}
}