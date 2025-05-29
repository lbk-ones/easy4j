package easy4j.module.jpa;

import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;


/**
 * HibernateConfig
 *
 * @author bokun.li
 * @date 2025-05
 */
public class HibernateConfig implements HibernatePropertiesCustomizer {
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        // 生成 sql 注释 类似于这种  /* load com.example.Entity */
//        hibernateProperties.put("hibernate.use_sql_comments", true);
        // 根据注解生成注释
        hibernateProperties.put("hibernate.integrator_provider",
                (IntegratorProvider) () -> Collections.singletonList(CommentIntegrator.INSTANCE));
    }
}