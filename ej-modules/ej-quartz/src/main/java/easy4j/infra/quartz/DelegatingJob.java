package easy4j.infra.quartz;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.Method;

/**
 * 通用代理Job，不依赖具体业务类，通过反射执行目标方法
 * 简单一点直接兼容获取实例
 * 传入class对象和方法对象 一个方法创建一个代理
 *
 * @author bokun.li
 * @date 2025/10/8
 */
final class DelegatingJob extends AbstractEasyQzJob {


    public Object getInstance(Class<?> clazz) {
        Object instance = null;
        try {
            if (clazz == null) return instance;
            instance = SpringUtil.getBean(clazz);
            if (null == instance) {
                instance = ReflectUtil.newInstance(clazz);
            }
        } catch (Exception e) {
            instance = ReflectUtil.newInstance(clazz);
        }
        return instance;
    }

    @Override
    public void executeJob(JobExecutionContext context) throws JobExecutionException {
        try {
            Object targetClass_ = context.getMergedJobDataMap().get(QzConstant.QUARTZ_JOB_CLASS);
            Object method_ = context.getMergedJobDataMap().get(QzConstant.QUARTZ_JOB_CLASS_METHOD);
            if (null != targetClass_ && method_ != null) {
                Class<?> targetClass = (Class<?>) targetClass_;
                Method method = (Method) method_;
                Object targetObject = getInstance(targetClass);
                if (null == targetObject) return;
                int parameterCount = method.getParameterCount();
                if (parameterCount != 1) return;
                Class<?>[] parameterTypes = method.getParameterTypes();
                for (Class<?> parameterType : parameterTypes) {
                    if (parameterType != JobExecutionContext.class) {
                        return;
                    }
                }
                method.invoke(targetObject, context);
            }
        } catch (Exception e) {
            throw new JobExecutionException("执行任务失败", e);
        }
    }
}
