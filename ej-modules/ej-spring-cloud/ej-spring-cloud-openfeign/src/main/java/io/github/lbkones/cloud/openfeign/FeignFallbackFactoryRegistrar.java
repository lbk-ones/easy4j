package io.github.lbkones.cloud.openfeign;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 自动注册FeignClient对应的FallbackFactory Bean
 * 扫描所有@FeignClient注解的接口，为其创建Easy4jDefaultFallbackFactory实例
 * @author bokun.li
 */
@Slf4j
public class FeignFallbackFactoryRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

    Environment environment;
    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        // 获取扫描包路径
        String basePackage = getBasePackage(importingClassMetadata);
        if (basePackage.isEmpty()) {
            basePackage = "io.github.lbkones";
        }

        // 扫描所有@FeignClient接口
        Set<Class<?>> feignClientClasses = scanFeignClientInterfaces(basePackage);

        for (Class<?> feignClientClass : feignClientClasses) {
            FeignClient annotation = feignClientClass.getAnnotation(FeignClient.class);
            if (annotation == null) {
                continue;
            }
            // 检查是否已指定其他fallbackFactory
            Class<?> fallbackFactoryClass = annotation.fallbackFactory();
            if (fallbackFactoryClass != void.class && !fallbackFactoryClass.getSimpleName().equals(Easy4jOpenFeignFallbackFactory.class.getSimpleName())) {
                continue;
            }
            String clientName = getClientName(annotation);
            String beanName = resolve(clientName, registry);
            if(StrUtil.isBlank(beanName)) return;
            if (!registry.containsBeanDefinition(beanName)) {
                RootBeanDefinition beanDefinition = createBeanDefinition(feignClientClass);
                registry.registerBeanDefinition(beanName, beanDefinition);
            }else{
                log.info("skip {} bean auto gen",beanName);
            }
        }
    }

    private String resolve( String value,BeanDefinitionRegistry registry) {
        ConfigurableBeanFactory beanFactory = registry instanceof ConfigurableBeanFactory
                ? (ConfigurableBeanFactory) registry : null;
        if (StringUtils.hasText(value)) {
            if (beanFactory == null) {
                return this.environment.resolvePlaceholders(value);
            }
            BeanExpressionResolver resolver = beanFactory.getBeanExpressionResolver();
            String resolved = beanFactory.resolveEmbeddedValue(value);
            if (resolver == null) {
                return resolved;
            }
            Object evaluateValue = resolver.evaluate(resolved, new BeanExpressionContext(beanFactory, null));
            if (evaluateValue != null) {
                return String.valueOf(evaluateValue);
            }
            return null;
        }
        return value;
    }

    // contextId > name > value
    public String getClientName(FeignClient annotation){
        String contextId = annotation.contextId();
        if(StrUtil.isNotBlank(contextId)){
            return contextId;
        }
        String name = annotation.name();
        if(StrUtil.isNotBlank(name)) return name;

        String value = annotation.value();
        if(StrUtil.isNotBlank(value)) return value;
        return null;
    }

    /**
     * 扫描所有@FeignClient接口
     */
    private Set<Class<?>> scanFeignClientInterfaces(String basePackage) {
        Set<Class<?>> result = new HashSet<>();
        try {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(basePackage) + "/**/*.class";
            Resource[] resources = resourcePatternResolver.getResources(pattern);

            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }

                try {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();

                    if (metadata.hasAnnotation(FeignClient.class.getName())) {
                        String className = metadata.getClassName();
                        try {
                            Class<?> clazz = Class.forName(className);
                            if (clazz.isInterface()) {
                                result.add(clazz);
                            }
                        } catch (ClassNotFoundException e) {
                            // 忽略无法加载的类
                        }
                    }
                } catch (Exception e) {
                    // 忽略无法读取的资源
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan FeignClient interfaces", e);
        }

        return result;
    }

    /**
     * 创建Easy4jDefaultFallbackFactory的Bean定义
     */
    private RootBeanDefinition createBeanDefinition(Class<?> feignClientClass) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(Easy4jOpenFeignFallbackFactory.class);
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(feignClientClass);
        return beanDefinition;
    }

    /**
     * 从ImportingClassMetadata获取基础包路径
     */
    private String getBasePackage(AnnotationMetadata importingClassMetadata) {
        String className = importingClassMetadata.getClassName();
        return className.substring(0, className.lastIndexOf('.'));
    }
}
