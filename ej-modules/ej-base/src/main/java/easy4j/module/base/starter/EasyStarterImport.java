package easy4j.module.base.starter;

import easy4j.module.base.exception.GlobalExceptionHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
/**
 * 引入 bean
 * @author bokun.li
 * @date 2023/11/19
 */
public class EasyStarterImport implements InitializingBean, ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {

        return new String[]{
                ApplicationStarterAfterForEj.class.getName(),
                EnvironmentHolder.class.getName(),
                GlobalExceptionHandler.class.getName()
        };
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
