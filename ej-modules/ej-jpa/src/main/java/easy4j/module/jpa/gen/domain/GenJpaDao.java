package easy4j.module.jpa.gen.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * GenJpaDao
 *
 * @author bokun.li
 * @date 2025-05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GenJpaDao extends BaseGen{
    private String daoClassName;
    private String domainName;
}