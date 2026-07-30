package easy4j.infra.log.operate;

import easy4j.infra.dbaccess.OrmInternal;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.SqlFileEnums;
import easy4j.infra.dbaccess.domain.PageRes;
import easy4j.infra.dbaccess.domain.OperationLogs;
import easy4j.infra.dbaccess.orm.IDBAccess;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;

import java.util.List;

public class DbOperate implements OperateLogRepository {

    private static IDBAccess dbAccess = null;
    @Override
    public void save(OperationLogs operationLogs) {
        dbAccess.save(operationLogs,OperationLogs.class);
    }

    @Override
    public void init() {
        OrmInternal.INIT_DB_FILE_PATH.add(SqlFileEnums.DB_OPERATE_LOG);
        dbAccess = OrmInternal.getNoTransactionOrm();
    }

    @Override
    public List<OperationLogs> queryBy(WhereBuild whereBuilder) {
        return dbAccess.query(whereBuilder,OperationLogs.class);
    }

    @Override
    public PageRes page(WhereBuild whereBuilder, Page<OperationLogs> page) {
        return dbAccess.queryPage(whereBuilder,page, OperationLogs.class);

    }
}
