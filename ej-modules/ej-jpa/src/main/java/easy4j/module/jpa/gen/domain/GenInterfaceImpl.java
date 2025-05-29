package easy4j.module.jpa.gen.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * GenInterfaceImpl
 *
 * @author bokun.li
 * @date 2025-05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GenInterfaceImpl extends BaseGen {
    private String interfaceName;
    private String interfaceImplName;
    private String firstLowDomainName;
    private String domainName;
}