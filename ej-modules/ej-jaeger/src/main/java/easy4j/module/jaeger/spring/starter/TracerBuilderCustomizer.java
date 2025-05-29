
package easy4j.module.jaeger.spring.starter;

import io.jaegertracing.internal.JaegerTracer;

/**
 * TracerBuilderCustomizer
 *
 * @author bokun.li
 * @date 2025-05
 */
@FunctionalInterface
public interface TracerBuilderCustomizer {

  /**
   * Provides the ability to execute arbitrary operations on the builder The customizer should NOT
   * call the build method
   */
  void customize(JaegerTracer.Builder builder);
}