package easy4j.infra.rpc.integrated.spring;

import easy4j.infra.rpc.integrated.spring.annotations.EnableEasy4jRpc;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bokun
 * @since 2.0.1
 */
public class BeanImport implements ImportSelector {

    private static final List<String> basePackages = new ArrayList<>();
    private static final List<String> domainPackages = new ArrayList<>();

    public static List<String> getBasePackages() {
        return basePackages.stream().distinct().collect(Collectors.toList());
    }
    public static List<String> getDomainPackages() {
        return domainPackages.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableEasy4jRpc.class.getName());
        if (annotationAttributes != null) {
            Object o = annotationAttributes.getOrDefault("basePackage", new String[]{});
            Object o2 = annotationAttributes.getOrDefault("domainPackage", new String[]{});
            String[] o1 = (String[]) o;
            String[] o22 = (String[]) o2;
            if (o1 != null && o1.length > 0) {
                Collections.addAll(basePackages, o1);
            } else {
                String basePackage = ClassUtils.getPackageName(importingClassMetadata.getClassName());
                basePackages.add(basePackage);
            }
            if (o22 != null && o22.length > 0) {
                Collections.addAll(domainPackages, o22);
            } else {
                String basePackage = ClassUtils.getPackageName(importingClassMetadata.getClassName());
                domainPackages.add(basePackage);
            }
        }

        return new String[]{SpringIntegrated.class.getName()};
    }
}
