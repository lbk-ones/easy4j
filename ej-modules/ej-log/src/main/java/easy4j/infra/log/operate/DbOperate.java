package easy4j.infra.log.operate;

import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.DBAccessFactory;
import easy4j.infra.dbaccess.SqlFileEnums;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import easy4j.infra.dbaccess.domain.OperationLogs;

import java.util.ArrayList;
import java.util.List;

public class DbOperate implements OperateLogRepository {

    private static DBAccess dbAccess = null;
    @Override
    public void save(OperationLogs operationLogs) {
        dbAccess.saveOne(operationLogs,OperationLogs.class);
    }

    @Override
    public void init() {
        DBAccessFactory.INIT_DB_FILE_PATH.add(SqlFileEnums.DB_OPERATE_LOG);
        dbAccess = DBAccessFactory.getDBAccess(JdbcHelper.getDataSource(), false, true);
    }

    @Override
    public List<OperationLogs> queryBy(WhereBuild whereBuilder) {
        return dbAccess.selectByCondition(whereBuilder,OperationLogs.class);
    }

    @Override
    public List<OperationLogs> page(WhereBuild whereBuilder) {
        return new ArrayList<>();

    }
}
