package easy4j.modules.ltl.transactional;


import easy4j.module.base.module.Module;
import easy4j.modules.ltl.transactional.component.LtTransactionalAspect;
import easy4j.modules.ltl.transactional.component.LtlTransactionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import javax.sql.DataSource;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
@Configuration
@ConditionalOnBean(DataSource.class)
public class Config {
    public static final String EASY4J_LTL_ENABLE = "ltl.enable";

    @Bean
    @Module(EASY4J_LTL_ENABLE)
    public LtTransactionalAspect ltTransactionalAspect() {

        return new LtTransactionalAspect();
    }

    @Bean
    @Module(EASY4J_LTL_ENABLE)
    public LtlTransactionService ltlTransactionService() {

        return new LtlTransactionService();
    }

}