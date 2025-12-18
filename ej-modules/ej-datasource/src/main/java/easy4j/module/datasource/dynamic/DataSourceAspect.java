package easy4j.module.datasource.dynamic;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * 数据源切面（适配自定义标识)
 * 这种模式有缺陷，使用另外的形式来搞
 */
@Aspect
@Order(1)
@Deprecated
public class DataSourceAspect {

    @Pointcut("@annotation(easy4j.module.datasource.dynamic.DataSource) || @within(easy4j.module.datasource.dynamic.DataSource)")
    public void dataSourcePointCut() {
    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String dataSourceKey = getDataSourceKey(joinPoint);
        // 若注解值为空，使用默认数据源
        if (dataSourceKey.isEmpty()) {
            dataSourceKey = DataSourceContextHolder.getDataSourceKey();
        }
        DataSourceContextHolder.setDataSourceKey(dataSourceKey);

        try {
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceKey();
        }
    }

    private String getDataSourceKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 方法级别注解
        DataSource methodAnnotation = method.getAnnotation(DataSource.class);
        if (methodAnnotation != null) {
            return methodAnnotation.value();
        }

        // 类级别注解
        Class<?> targetClass = joinPoint.getTarget().getClass();
        DataSource classAnnotation = targetClass.getAnnotation(DataSource.class);
        if (classAnnotation != null) {
            return classAnnotation.value();
        }

        // 默认空字符串（后续用默认数据源）
        return "";
    }
}