//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package easy4j.module.jaeger;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.opentracing.Span;

/**
 * ThreadLocalTools
 *
 * @author bokun.li
 * @date 2025-05
 */
public class ThreadLocalTools {
    public static ThreadLocal<Span> httpWebSpanThreadLocal = new TransmittableThreadLocal<>();
    public static ThreadLocal<Span> spanThreadLocal = new TransmittableThreadLocal<>();
    public static ThreadLocal<String> stringThreadLocal = new TransmittableThreadLocal<>();
    public static ThreadLocal<Span> providerSpanThreadLocal = new TransmittableThreadLocal<>();

    public ThreadLocalTools() {
    }
}