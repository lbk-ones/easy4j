package easy4j.module.jpa.gen.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenInterface extends BaseGen {
    private String interfaceName;


    private String firstLowDomainName;
    private String domainName;
}
