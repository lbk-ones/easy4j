package easy4j.module.jpa.gen.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenJpaDao extends BaseGen{
    private String daoClassName;
    private String domainName;
}
