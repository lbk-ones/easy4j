package easy4j.infra.rpc.integrated.spring;

import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.heart.NodeHeartbeatManager;
import easy4j.infra.rpc.integrated.spring.annotations.EnableEasy4jRpc;
import easy4j.infra.rpc.integrated.spring.annotations.RpcService;
import easy4j.infra.rpc.server.DefaultServerNode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.Set;

/**
 * handler RpcService annotation
 * 扫描标注了RpcService注解的实现类
 *
 * @author bokun
 * @since 2.0.1
 */
public class SpringRpcServiceProcessor implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableEasy4jRpc.class.getName());
        if (null != annotationAttributes) {
            Object orDefault = annotationAttributes.getOrDefault("basePackage", new String[]{});
            String[] orDefault1 = (String[]) orDefault;
            if (orDefault1 == null || orDefault1.length == 0) {
                orDefault1 = new String[]{ClassUtils.getPackageName(importingClassMetadata.getClassName())};
            }
            for (String basePackage : orDefault1) {
                ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
                scanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
                Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
                for (BeanDefinition candidateComponent : candidateComponents) {
                    String beanClassName = candidateComponent.getBeanClassName();
                    try {
                        Class<?> aClass = Class.forName(beanClassName);
                        if (aClass.isInterface()) {
                            continue;
                        }
                        RpcService annotation = aClass.getAnnotation(RpcService.class);
                        if (annotation.disabled()) continue;
                        String serviceName = annotation.serviceName();
                        if (serviceName == null) {
                            throw new RpcException("RpcService annotation serviceName is not be null");
                        }
                        boolean b = aClass.isAnnotationPresent(Service.class) || aClass.isAnnotationPresent(Component.class) || aClass.isAnnotationPresent(Configuration.class);
                        boolean isUsed = false;
                        if (!b) {
                            if (!registry.isBeanNameInUse(serviceName)) {
                                BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(aClass)
                                        .getBeanDefinition();
                                // dynamic register
                                registry.registerBeanDefinition(serviceName, beanDefinition);
                            } else {
                                isUsed = true;
                            }
                        }
                        if (!isUsed) {
                            DefaultServerNode.INSTANCE.registry(new NodeHeartbeatManager(), serviceName);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }

            }


        }
    }
}
