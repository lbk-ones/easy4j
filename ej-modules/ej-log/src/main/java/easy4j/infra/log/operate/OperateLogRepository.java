package easy4j.infra.log.operate;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.infra.dbaccess.domain.OperationLogs;

import java.util.List;

public interface OperateLogRepository {

    void save(OperationLogs operationLogs);

    void init();

    List<OperationLogs> queryBy(WhereBuild whereBuilder);
    List<OperationLogs> page(WhereBuild whereBuilder);
}
