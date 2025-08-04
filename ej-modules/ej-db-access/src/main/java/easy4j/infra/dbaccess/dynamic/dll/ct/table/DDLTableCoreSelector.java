package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;

import java.util.List;

public class DDLTableCoreSelector {

    private final static List<IDDLTableStrategy> list = ListTs.newArrayList();

    static {
        list.add(new MysqlDDLTableStrategy());
    }

    public static DDLTableExecutor getDDlTableExecutor(DDLTableInfo ddlTableInfo) {
        for (IDDLTableStrategy iddlFieldCore : list) {
            if (iddlFieldCore.match(ddlTableInfo)) {
                return new DDLTableExecutor(iddlFieldCore, ddlTableInfo);
            }
        }
        throw EasyException.wrap(BusCode.A00047, ddlTableInfo.getDbType());
    }
}
