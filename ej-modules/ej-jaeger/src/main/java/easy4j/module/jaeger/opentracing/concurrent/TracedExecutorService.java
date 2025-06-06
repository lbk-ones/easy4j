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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;


/**
 * TracedExecutorService
 *
 * @author bokun.li
 * @date 2025-05
 */
public class TracedExecutorService extends TracedExecutor implements ExecutorService {

  private final ExecutorService delegate;

  public TracedExecutorService(ExecutorService delegate, Tracer tracer) {
    this(delegate, tracer, true);
  }

  public TracedExecutorService(ExecutorService delegate, Tracer tracer,
      boolean traceWithActiveSpanOnly) {
    super(delegate, tracer, traceWithActiveSpanOnly);
    this.delegate = delegate;
  }

  @Override
  public void shutdown() {
    delegate.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return delegate.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return delegate.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return delegate.isTerminated();
  }

  @Override
  public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
    return delegate.awaitTermination(l, timeUnit);
  }

  @Override
  public <T> Future<T> submit(Callable<T> callable) {
    Span span = createSpan("submit");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.submit(toActivate == null ? callable :
          new TracedCallable<T>(callable, tracer, toActivate));
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public <T> Future<T> submit(Runnable runnable, T t) {
    Span span = createSpan("submit");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.submit(toActivate == null ? runnable :
          new TracedRunnable(runnable, tracer, toActivate), t);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public Future<?> submit(Runnable runnable) {
    Span span = createSpan("submit");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.submit(toActivate == null ? runnable :
          new TracedRunnable(runnable, tracer, toActivate));
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection)
      throws InterruptedException {
    Span span = createSpan("invokeAll");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.invokeAll(toTraced(collection, toActivate));
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l,
      TimeUnit timeUnit) throws InterruptedException {
    Span span = createSpan("invokeAll");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.invokeAll(toTraced(collection, toActivate), l, timeUnit);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> collection)
      throws InterruptedException, ExecutionException {
    Span span = createSpan("invokeAny");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.invokeAny(toTraced(collection, toActivate));
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit)
      throws InterruptedException, ExecutionException, TimeoutException {
    Span span = createSpan("invokeAny");
    try {
      Span toActivate = span != null ? span : tracer.activeSpan();
      return delegate.invokeAny(toTraced(collection, toActivate), l, timeUnit);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  private <T> Collection<? extends Callable<T>> toTraced(Collection<? extends Callable<T>> delegate, Span toActivate) {
    List<Callable<T>> tracedCallables = new ArrayList<Callable<T>>(delegate.size());

    for (Callable<T> callable: delegate) {
      tracedCallables.add(toActivate == null ? callable :
          new TracedCallable<T>(callable, tracer, toActivate));
    }

    return tracedCallables;
  }
}
