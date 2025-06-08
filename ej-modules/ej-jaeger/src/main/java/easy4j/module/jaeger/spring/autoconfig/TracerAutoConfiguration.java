/**
 * Copyright 2018-2019 The OpenTracing Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package easy4j.module.jaeger.spring.autoconfig;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SqlType;
import easy4j.module.jaeger.opentracing.jdbc.TracingDataSource;
import easy4j.module.jaeger.opentracing.web.ServletFilterSpanDecorator;
import easy4j.module.jaeger.opentracing.web.TracingFilter;
import easy4j.module.jaeger.opentracing.web.decorator.NotStandardSpanDecorator;
import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Pavol Loffay
 */
@Configuration
@Slf4j
@AutoConfigureAfter({DruidDataSourceAutoConfigure.class})
public class TracerAutoConfiguration {

    /**
     * This method provides tracer if user did not specify any tracer bean.
     * <p>
     * The order of getting the tracer is:
     * <ol>
     *     <li>Tracer registered in {@link GlobalTracer#registerIfAbsent(Tracer)}</li>
     *     <li>Tracer resolved from {@link TracerResolver#resolveTracer()}</li>
     *     <li>Default tracer, which is {@link io.opentracing.noop.NoopTracer}</li>
     * </ol>
     *
     * @return tracer
     */
    @Bean
    @ConditionalOnMissingBean(Tracer.class)
    public Tracer getTracer() {
        Tracer tracer;
        if (GlobalTracer.isRegistered()) {
            log.warn("GlobalTracer is already registered. For consistency it is best practice to provide " +
                    "a Tracer bean instead of manually registering it with the GlobalTracer");
            tracer = GlobalTracer.get();
        } else {
            tracer = TracerResolver.resolveTracer();
            if (tracer == null) {
                // WARNING: Don't return GlobalTracer.get() as this will result in a
                // stack overflow if the returned tracer is subsequently wrapped by a
                // BeanPostProcessor. The post processed tracer would then be registered
                // with the {@link GlobalTracer) (via the {@link TracerRegisterAutoConfiguration})
                // resulting in the wrapper both wrapping the GlobalTracer, as well as being
                // the tracer used by the GlobalTracer.
                tracer = NoopTracerFactory.create();
            }
        }
        log.warn("Tracer bean is not configured! Switching to " + tracer);
        return tracer;
    }

    // web filter
    @Bean
    @ConditionalOnProperty(value = "opentracing.jaeger.enableWebFilter", havingValue = "true", matchIfMissing = true)
    public TracingFilter jaegerWebFilter(Tracer trace) {
        List<ServletFilterSpanDecorator> list = ListTs.asList(ServletFilterSpanDecorator.STANDARD_TAGS, NotStandardSpanDecorator.NOT_STANDARD_TAGS);
        Pattern compile = Pattern.compile("/actuator");
        return new TracingFilter(trace, list, compile);
    }


    // datasource filter
    @Bean
    @Primary
    @Qualifier("dataSource")
    @ConditionalOnProperty(value = "opentracing.jaeger.enableDatasourceFilter", havingValue = "true", matchIfMissing = true)
    public DataSource dataSourceWrapper(DataSource dataSource, Tracer tracer) {
        log.info("begin replace dataSource");
        Set<String> ignoreStateMent = SqlType.getIgnoreStateMent();
        return new TracingDataSource(tracer, dataSource, null, true, ignoreStateMent);
    }
}
