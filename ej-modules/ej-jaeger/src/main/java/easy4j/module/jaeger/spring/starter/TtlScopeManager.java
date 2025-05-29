package easy4j.module.jaeger.spring.starter;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * TtlScopeManager
 *
 * @author bokun.li
 * @date 2025-05
 */
public class TtlScopeManager implements ScopeManager {
    final ThreadLocal<TtlScope> tlsScope = new TransmittableThreadLocal<>();


    public Scope activate(Span span) {
        return new TtlScope(this, span);
    }

    public Span activeSpan() {
        TtlScope scope = this.tlsScope.get();
        return scope == null ? null : scope.span();
    }
}