package easy4j.modules.ltl.transactional;


import easy4j.modules.ltl.transactional.component.LtTransactionalAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@ComponentScan(value={"easy4j.modules.ltl.transactional.component"})
@EnableJdbcRepositories(basePackageClasses = {LtlTransactionMapper.class})
public class Config {

    @Bean
    public LtTransactionalAspect ltTransactionalAspect(){

        return new LtTransactionalAspect();
    }

}
