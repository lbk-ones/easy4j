package easy4j.infra.dbaccess.dynamic.dll.op;

import easy4j.infra.dbaccess.CommonDBAccess;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OpConfig {

    private boolean toUnderLine = true;

    private boolean toLowCase = true;

    private boolean toUpperCase;

    private CommonDBAccess commonDBAccess = new CommonDBAccess();

}
