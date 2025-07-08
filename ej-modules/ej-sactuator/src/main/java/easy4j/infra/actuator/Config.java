package easy4j.infra.actuator;

import easy4j.infra.common.module.ModuleBoolean;
import easy4j.infra.common.utils.SysConstant;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public CacheEndpoint cacheEndpoint() {
        return new CacheEndpoint();
    }


    @Configuration(proxyBeanMethods = false)
    @ModuleBoolean(SysConstant.EASY4J_METRICS_ENABLE + ":true")
    public static class MeterConfig {
        
        @Bean
        public DefaultMetricsProvider metricsProvider(MeterRegistry meterRegistry) {
            return new DefaultMetricsProvider(meterRegistry);
        }

        @Bean
        public TimedAspect timedAspect(MeterRegistry registry) {
            return new TimedAspect(registry);
        }

        @Bean
        public CountedAspect countedAspect(MeterRegistry registry) {
            return new CountedAspect(registry);
        }

    }


}
