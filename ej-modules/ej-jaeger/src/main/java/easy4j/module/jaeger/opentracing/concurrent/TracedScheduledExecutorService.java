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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Jose Montoya
 *
 * Executor which propagates span from parent thread to scheduled.
 * Optionally it creates parent span if traceWithActiveSpanOnly = false.
 */
public class TracedScheduledExecutorService extends TracedExecutorService implements ScheduledExecutorService {

  private final ScheduledExecutorService delegate;

  public TracedScheduledExecutorService(ScheduledExecutorService delegate, Tracer tracer) {
    this(delegate, tracer, true);
  }

  public TracedScheduledExecutorService(ScheduledExecutorService delegate, Tracer tracer,
      boolean traceWithActiveSpanOnly) {
    super(delegate, tracer, traceWithActiveSpanOnly);
    this.delegate = delegate;
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
    Span span = createSpan("schedule");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.schedule(tracer.activeSpan() == null ? runnable :
          new TracedRunnable(runnable, tracer, toActivate), delay, timeUnit);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit timeUnit) {
    Span span = createSpan("schedule");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.schedule(tracer.activeSpan() == null ? callable :
          new TracedCallable<T>(callable, tracer, toActivate), delay, timeUnit);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period,
      TimeUnit timeUnit) {
    Span span = createSpan("scheduleAtFixedRate");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.scheduleAtFixedRate(tracer.activeSpan() == null ? runnable :
          new TracedRunnable(runnable, tracer, toActivate), initialDelay, period, timeUnit);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay,
      TimeUnit timeUnit) {
    Span span = createSpan("scheduleWithFixedDelay");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.scheduleWithFixedDelay(tracer.activeSpan() == null ? runnable :
          new TracedRunnable(runnable, tracer, toActivate), initialDelay, delay, timeUnit);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }
}
