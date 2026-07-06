package io.github.lbkones.cloud.openfeign;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 默认的降级实现 使用方法：@FeignClient(name='xxx',fallbackFactory=Easy4jOpenFeignFallbackFactory.class)
 * @param <T>
 */
@Slf4j
public final class Easy4jOpenFeignFallbackFactory<T> implements FallbackFactory<T> {


    private final Class<T> targetInterface;

    public Easy4jOpenFeignFallbackFactory(Class<T> targetInterface) {
        this.targetInterface = targetInterface;
    }

    @Override
    public T create(Throwable cause) {
        // 使用动态代理创建降级对象
        return (T) Proxy.newProxyInstance(
                targetInterface.getClassLoader(),
                new Class[]{targetInterface},
                new FallbackInvocationHandler(cause)
        );
    }
    /**
     * 降级调用处理器
     */
    private static class FallbackInvocationHandler implements InvocationHandler {

        private final Throwable cause;

        public FallbackInvocationHandler(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            log.info("执行降级方法 {}#{}",method.getDeclaringClass().getSimpleName(),method.getName());

            // 获取方法返回类型
            Class<?> returnType = method.getReturnType();

            // 根据返回类型返回默认值
            return getDefaultValue(returnType);
        }

        /**
         * 获取指定类型的默认值
         */
        private Object getDefaultValue(Class<?> returnType) {
            if (returnType == void.class) {
                return null;
            }

            if (returnType == boolean.class) {
                return false;
            }

            if (returnType == byte.class) {
                return (byte) 0;
            }

            if (returnType == short.class) {
                return (short) 0;
            }

            if (returnType == int.class) {
                return 0;
            }

            if (returnType == long.class) {
                return 0L;
            }

            if (returnType == float.class) {
                return 0.0f;
            }

            if (returnType == double.class) {
                return 0.0d;
            }

            if (returnType == char.class) {
                return '\u0000';
            }
            if (FallbackRes.class.isAssignableFrom(returnType)) {
                Object o = ReflectUtil.newInstance(returnType);
                FallbackRes o1 = (FallbackRes) o;
                return o1.fallbackRes(this.cause);
            }
            // 对象类型返回 null
            return ReflectUtil.newInstance(returnType);
        }
    }
}
