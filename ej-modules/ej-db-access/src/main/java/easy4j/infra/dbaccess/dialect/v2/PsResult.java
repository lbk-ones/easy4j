package easy4j.infra.dbaccess.dialect.v2;

import lombok.Data;

import java.sql.PreparedStatement;
import java.util.List;

@Data
public class PsResult {

    /**
     * 如果是批量的那么这个sql会有多个
     */
    private List<String> sql;

    /**
     * 耗时如果是批量执行的那么这个是所有加起来的耗时，并不能看出单个sql的耗时
     */
    private long costTime;

    /**
     * 暂时没返回回来，给未来预留的
     */
    private PreparedStatement preparedStatement;

    /**
     * 受影响的条数
     */
    private long effectRows;
}
