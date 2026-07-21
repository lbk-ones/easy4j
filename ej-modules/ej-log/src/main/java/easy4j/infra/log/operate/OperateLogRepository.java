package easy4j.infra.log.operate;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.domain.OperationLogs;
import easy4j.infra.dbaccess.domain.PageRes;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;

import java.util.List;

public interface OperateLogRepository {

    void save(OperationLogs operationLogs);

    void init();

    List<OperationLogs> queryBy(WhereBuild whereBuilder);
    PageRes page(WhereBuild whereBuilder, Page<OperationLogs> page);
}
