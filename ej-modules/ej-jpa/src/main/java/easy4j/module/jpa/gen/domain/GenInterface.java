package easy4j.module.jpa.gen.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * GenInterface
 *
 * @author bokun.li
 * @date 2025-05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GenInterface extends BaseGen {
    private String interfaceName;


    private String firstLowDomainName;
    private String domainName;
}