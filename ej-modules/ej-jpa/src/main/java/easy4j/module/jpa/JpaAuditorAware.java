package easy4j.module.jpa;

import easy4j.module.jpa.aware.EasyJpaAuditorAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Spring Data JPA 自动审计
 * @author bokun
 * @date 2023/5/4
 */
//@Component
@Slf4j
public class JpaAuditorAware implements AuditorAware<String> {

    @Autowired
    EasyJpaAuditorAware easyJpaAuditorAware;

    @Override
    public Optional<String> getCurrentAuditor() {
        return easyJpaAuditorAware.getCurrentAuditor();
    }
}