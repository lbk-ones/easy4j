package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractIDDLTableStrategy implements IDDLTableStrategy {
    boolean toUnderLine;
    boolean toLowCase = true;
    boolean toUpperCase;
}
