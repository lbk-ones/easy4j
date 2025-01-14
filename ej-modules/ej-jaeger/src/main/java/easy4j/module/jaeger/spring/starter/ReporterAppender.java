
package easy4j.module.jaeger.spring.starter;

import io.jaegertracing.spi.Reporter;

import java.util.Collection;

@FunctionalInterface
public interface ReporterAppender {

  /**
   * Provides the ability to add custom Reporters other than the ones that are auto-configured based
   * on the configuration Implementation should only add reporters to the collection
   */
  void append(Collection<Reporter> reporters);
}
