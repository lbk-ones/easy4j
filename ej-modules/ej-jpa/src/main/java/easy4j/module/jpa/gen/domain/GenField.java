package easy4j.module.jpa.gen.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenField extends BaseGen {
    private String type;
    private String name;


    private List<String> fieldLine = new ArrayList<>();

}
