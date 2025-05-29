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

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

import java.util.concurrent.Callable;


/**
 * TracedCallable
 *
 * @author bokun.li
 * @date 2025-05
 */
public class TracedCallable<V> implements Callable<V> {

  private final Callable<V> delegate;
  private final Span span;
  private final Tracer tracer;

  public TracedCallable(Callable<V> delegate, Tracer tracer) {
    this(delegate, tracer, tracer.activeSpan());
  }

  public TracedCallable(Callable<V> delegate, Tracer tracer, Span span) {
    this.delegate = delegate;
    this.tracer = tracer;
    this.span = span;
  }

  @Override
  public V call() throws Exception {
    Scope scope = span == null ? null : tracer.scopeManager().activate(span);
    try {
      return delegate.call();
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }
}
