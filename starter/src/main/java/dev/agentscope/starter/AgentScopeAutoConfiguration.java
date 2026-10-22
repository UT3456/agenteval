package dev.agentscope.starter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
@AutoConfiguration
@EnableConfigurationProperties(AgentScopeProperties.class)
@ConditionalOnProperty(prefix="agentscope",name="enabled",havingValue="true",matchIfMissing=true)
public class AgentScopeAutoConfiguration {
 @Bean @ConditionalOnMissingBean Redactor agentScopeRedactor(AgentScopeProperties p){return new Redactor(p.getRedaction().getRules());}
 @Bean(destroyMethod="close") @ConditionalOnMissingBean TelemetryExporter agentScopeExporter(AgentScopeProperties p){return new TelemetryExporter(p,new ObjectMapper());}
 @Bean AgentScopeObservationHandler agentScopeObservationHandler(AgentScopeProperties p,TelemetryExporter e,Redactor r){return new AgentScopeObservationHandler(p,e,r);}
}