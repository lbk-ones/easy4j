package easy4j.module.jaeger.opentracing.concurrent;

import io.opentracing.Span;
import io.opentracing.Tracer;

import java.util.concurrent.Executor;

/**
 * 如果 traceWithActiveSpanOnly=false 那么如果当前没有span则新建span
 */
public class TracedExecutor implements Executor {

  protected final Tracer tracer;
  private final Executor delegate;
  private final boolean traceWithActiveSpanOnly;

  public TracedExecutor(Executor executor, Tracer tracer) {
    this(executor, tracer, true);
  }

  public TracedExecutor(Executor executor, Tracer tracer, boolean traceWithActiveSpanOnly) {
    this.delegate = executor;
    this.tracer = tracer;
    this.traceWithActiveSpanOnly = traceWithActiveSpanOnly;
  }

  @Override
  public void execute(Runnable runnable) {
    Span span = createSpan("execute");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      delegate.execute(toActivate == null ? runnable : new TracedRunnable(runnable, tracer, toActivate));
    } finally {
      // close the span if created
      if (span != null) {
        span.finish();
      }
    }
  }

  Span createSpan(String operationName) {
    if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
      return tracer.buildSpan(operationName).start();
    }
    return null;
  }
}
