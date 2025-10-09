package easy4j.infra.quartz;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 通用代理Job，不依赖具体业务类，通过反射执行目标方法
 * 简单一点直接兼容获取实例
 * 传入class对象和方法对象 一个方法创建一个代理
 *
 * @author bokun.li
 * @date 2025/10/8
 */
@Slf4j
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
        Object targetClass_ = context.getMergedJobDataMap().get(QzConstant.QUARTZ_JOB_CLASS);
        Object method_ = context.getMergedJobDataMap().get(QzConstant.QUARTZ_JOB_CLASS_METHOD);
        if (null != targetClass_ && method_ != null) {
            String targetClassName = targetClass_.toString();
            String methodName = method_.toString();
            Class<?> targetClass;
            try {
                targetClass = Class.forName(targetClassName);
            } catch (ClassNotFoundException e) {
                log.error("the targetClass is not found :" + targetClassName);
                return;
            }
            Method method = ReflectUtil.getMethod(targetClass, methodName,JobExecutionContext.class);
            if (method == null) return;
            Object targetObject = getInstance(targetClass);
            if (null == targetObject) return;
            try {
                method.invoke(targetObject, context);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("the targetClass method invoke error :" + e.getMessage());

            }
        }
    }
}
