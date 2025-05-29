/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
