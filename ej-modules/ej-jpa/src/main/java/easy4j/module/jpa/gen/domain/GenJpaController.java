package easy4j.module.jpa.gen.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * GenJpaController
 *
 * @author bokun.li
 * @date 2025-05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GenJpaController extends BaseGen{
    private String firstLowDomainName;
    private String domainName;
    private String controllerName;
}