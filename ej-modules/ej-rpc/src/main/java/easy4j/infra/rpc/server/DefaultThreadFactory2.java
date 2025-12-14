package easy4j.infra.rpc.server;

import io.netty.util.concurrent.DefaultThreadFactory;

public class DefaultThreadFactory2 extends DefaultThreadFactory {
    Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public DefaultThreadFactory2(Class<?> poolType) {
        super(poolType);
    }

    public DefaultThreadFactory2(String poolName) {
        super(poolName);
    }

    public DefaultThreadFactory2(Class<?> poolType, boolean daemon) {
        super(poolType, daemon);
    }

    public DefaultThreadFactory2(String poolName, boolean daemon) {
        super(poolName, daemon);
    }

    public DefaultThreadFactory2(Class<?> poolType, int priority) {
        super(poolType, priority);
    }

    public DefaultThreadFactory2(String poolName, int priority) {
        super(poolName, priority);
    }

    public DefaultThreadFactory2(Class<?> poolType, boolean daemon, int priority) {
        super(poolType, daemon, priority);
    }

    public DefaultThreadFactory2(String poolName, boolean daemon, int priority, ThreadGroup threadGroup) {
        super(poolName, daemon, priority, threadGroup);
    }

    public DefaultThreadFactory2(String poolName, boolean daemon, int priority, ThreadGroup threadGroup, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        super(poolName, daemon, priority, threadGroup);
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    public DefaultThreadFactory2(String poolName, boolean daemon, int priority) {
        super(poolName, daemon, priority);
    }

    @Override
    protected Thread newThread(Runnable r, String name) {
        Thread thread = super.newThread(r, name);
        if (this.uncaughtExceptionHandler != null) {
            thread.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
        }
        return thread;
    }
}
