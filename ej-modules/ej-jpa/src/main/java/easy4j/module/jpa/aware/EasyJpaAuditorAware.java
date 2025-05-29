package easy4j.module.jpa.aware;

import easy4j.module.base.annotations.Desc;

import java.util.Optional;

/**
 * EasyJpaAuditorAware
 *
 * @author bokun.li
 * @date 2025-05
 */
@Desc("jpa自动审计 实现接口 并注入 bean EasyJpaAuditorAware 不能再去实现  AuditorAware 接口 不然会有问题")
public interface EasyJpaAuditorAware {

    Optional<String> getCurrentAuditor();

}