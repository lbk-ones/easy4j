package easy4j.infra.rpc.integrated.spring;

import easy4j.infra.rpc.integrated.spring.annotations.EnableEasy4jRpc;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author bokun
 * @since 2.0.1
 */
public class BeanImport implements ImportSelector {

    private static final List<String> basePackages = new ArrayList<>();

    public static List<String> getBasePackages() {
        return Collections.unmodifiableList(basePackages);
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableEasy4jRpc.class.getName());
        if (annotationAttributes != null) {
            Object o = annotationAttributes.getOrDefault("basePackage", new String[]{});
            String[] o1 = (String[]) o;
            if (o1 != null && o1.length > 0) {
                Collections.addAll(basePackages, o1);
            } else {
                String basePackage = ClassUtils.getPackageName(importingClassMetadata.getClassName());
                basePackages.add(basePackage);
            }
        }

        return new String[]{SpringIntegrated.class.getName()};
    }
}
