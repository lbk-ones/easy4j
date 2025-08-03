package easy4j.infra.dbaccess.dynamic.dll.ct.field;

import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;

import java.util.List;

/**
 * DDLFieldStrategySelector
 * 选择器
 *
 * @author bokun.li
 * @date 2025-08-03
 */
public class DDLFieldStrategySelector {

    private final static List<IDDLFieldStrategy> list = ListTs.newArrayList();

    static {
        list.add(new MysqlDDLFieldStrategy());
    }


    public static DDLFieldStrategyExecutor selectExecutor(DDLFieldInfo ddlFieldInfo) {
        for (IDDLFieldStrategy iddlFieldCore : list) {
            if (iddlFieldCore.match(ddlFieldInfo)) {
                return new DDLFieldStrategyExecutor(iddlFieldCore, ddlFieldInfo);
            }
        }
        throw EasyException.wrap(BusCode.A00047, ddlFieldInfo.getDbType());
    }


}
