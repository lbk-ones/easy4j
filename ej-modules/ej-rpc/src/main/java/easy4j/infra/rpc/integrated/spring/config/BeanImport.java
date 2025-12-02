package easy4j.infra.rpc.integrated.spring.config;

import easy4j.infra.rpc.config.SpringConfig;
import easy4j.infra.rpc.integrated.SpringConnectionManager;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class BeanImport implements ImportSelector {


    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{SpringConfig.class.getName(), SpringConnectionManager.class.getName()};
    }
}
