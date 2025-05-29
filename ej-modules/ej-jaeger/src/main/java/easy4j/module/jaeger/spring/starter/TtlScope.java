package easy4j.module.jaeger.spring.starter;

import io.opentracing.Scope;
import io.opentracing.Span;

/**
 * TtlScope
 *
 * @author bokun.li
 * @date 2025-05
 */
public class TtlScope implements Scope {
    private final TtlScopeManager scopeManager;
    private final Span wrapped;
    private final TtlScope toRestore;

    TtlScope(TtlScopeManager scopeManager, Span wrapped) {
        this.scopeManager = scopeManager;
        this.wrapped = wrapped;
        this.toRestore = scopeManager.tlsScope.get();
        scopeManager.tlsScope.set(this);
    }

    @Override
    public void close() {
        if (this.scopeManager.tlsScope.get() == this) {
            //this.scopeManager.tlsScope.set(this.toRestore);
            this.scopeManager.tlsScope.remove();
        }
    }

    Span span() {
        return this.wrapped;
    }

}