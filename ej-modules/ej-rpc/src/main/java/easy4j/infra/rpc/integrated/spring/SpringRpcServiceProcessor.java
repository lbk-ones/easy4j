package easy4j.infra.rpc.integrated.spring;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.enums.RegisterType;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.heart.NodeHeartbeatManager;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.integrated.spring.annotations.EnableEasy4jRpc;
import easy4j.infra.rpc.integrated.spring.annotations.RpcService;
import easy4j.infra.rpc.server.DefaultServerNode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
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
        // 无注册中心则不执行任务注册动作
        E4jRpcConfig config = IntegratedFactory.getConfig();
        if (config.getServer().isDisabled() ||  config.getRegisterType() == RegisterType.NONE) {
            return;
        }
        if (null != annotationAttributes) {
            Object orDefault = annotationAttributes.getOrDefault("basePackage", new String[]{});
            String[] orDefault1 = (String[]) orDefault;
            if (orDefault1 == null || orDefault1.length == 0) {
                orDefault1 = new String[]{ClassUtils.getPackageName(importingClassMetadata.getClassName())};
            }
            for (String basePackage : orDefault1) {
                ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
                scanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class,true,true));
                Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
                for (BeanDefinition candidateComponent : candidateComponents) {
                    String beanClassName = candidateComponent.getBeanClassName();
                    try {
                        Class<?> aClass = Class.forName(beanClassName);
                        // 应该不可能是接口 但是严谨起见 还是判断一下
                        if (aClass.isInterface()) {
                            continue;
                        }
                        // dynamic registry bean
                        if (!isBeanRegistry(aClass,registry)) {
                            String s = StrUtil.lowerFirst(aClass.getSimpleName());
                            BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(aClass)
                                    .getBeanDefinition();
                            registry.registerBeanDefinition(s,beanDefinition);
                        }
                        // 先取实现类 如果没有找到服务名称再找接口
                        RpcService annotation = aClass.getAnnotation(RpcService.class);
                        if(annotation == null){
                            // 如果实现的接口上有RpcService注解且兼容取值
                            Class<?>[] interfaces = aClass.getInterfaces();
                            if (interfaces != null) {
                                for (Class<?> anInterface : interfaces) {
                                    if(anInterface.isAnnotationPresent(RpcService.class)){
                                        annotation = anInterface.getAnnotation(RpcService.class);
                                        break;
                                    }
                                }
                            }
                        }
                        if (annotation == null || annotation.disabled()) continue;
                        String serviceName = annotation.serviceName();
                        if(StrUtil.isBlank(serviceName)){
                            serviceName = config.getServer().getServerName();
                        }
                        if(StrUtil.isNotBlank(serviceName)){
                            DefaultServerNode.INSTANCE.registry(new NodeHeartbeatManager(), serviceName);
                        }else{
                            throw new RpcException("RpcService annotation serviceName is not be null");
                        }
//                        boolean b = aClass.isAnnotationPresent(Service.class) || aClass.isAnnotationPresent(Component.class) || aClass.isAnnotationPresent(Configuration.class);
//                        boolean isUsed = false;
//                        if (!b) {
//                            if (!registry.isBeanNameInUse(serviceName)) {
//                                BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(aClass)
//                                        .getBeanDefinition();
//                                // dynamic register
//                                registry.registerBeanDefinition(serviceName, beanDefinition);
//                            } else {
//                                isUsed = true;
//                            }
//                        }
//                        if (!isUsed) {
//                            DefaultServerNode.INSTANCE.registry(new NodeHeartbeatManager(), serviceName);
//                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }

            }


        }
    }

    public boolean isBeanRegistry(Class<?> aclass,BeanDefinitionRegistry registry){
        if (registry.containsBeanDefinition(StrUtil.lowerFirst(aclass.getSimpleName())) || registry.containsBeanDefinition(aclass.getName())) {
            return true;
        }
        return aclass.isAnnotationPresent(Service.class) || aclass.isAnnotationPresent(Component.class) || aclass.isAnnotationPresent(Repository.class);
    }
}
