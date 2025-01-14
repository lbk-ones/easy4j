
package easy4j.module.jaeger.spring.starter;

import io.jaegertracing.internal.JaegerTracer;

@FunctionalInterface
public interface TracerBuilderCustomizer {

  /**
   * Provides the ability to execute arbitrary operations on the builder The customizer should NOT
   * call the build method
   */
  void customize(JaegerTracer.Builder builder);
}
