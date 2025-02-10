package easy4j.module.jpa.gen.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenDto extends BaseGen{

    private String dtoClassName;

    private List<GenField> fieldList;

}
