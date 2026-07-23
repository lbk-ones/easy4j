package easy4j.infra.dbaccess.orm;

import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * join查询构造器适用方法
 * <hr/>
 * <pre>
 * new SqlWrapper(
 *     SqlItem.of(OperationLogs::getOperatorId, OperationLogs.class,
 *             FWhereBuild.get(OperationLogs.class)
 *                     .eq(OperationLogs::getAction, "323")
 *                     .inArray(OperationLogs::getId, 1L, 2L),
 *             OperationLogs::getOperatorId, OperationLogs::getOperatorName
 *     ),
 *     SqlItem.leftJoin(),
 *     SqlItem.of("id", SysLogRecord.class, "tag", "tagDesc"),
 *     SqlItem.rightJoin(),
 *     SqlItem.of("var1", "", OperationLogs.class, "var1", "var2"),
 *     SqlItem.join(),
 *     SqlItem.of("operateCode", SysLogRecord.class, "var3", "var4"),
 *     SqlItem.join("hash join"),
 *     SqlItem.of("operateCode2", SysLogRecord.class, "var5  varxx", "wq.var6  as  wqx")
 * ).where(FWhereBuild.get(SysLogRecord.class).sql(true, "a.operateId = ? and b.tag = ?", "23", "25"))
 * </pre>
 *
 * @author bokun.li
 * @since 2.1.4
 */
public class SqlWrapper {

    @Getter
    private final List<SqlItem> sqlItemList = ListTs.newLinkedList();

    @Getter
    private WhereBuild whereBuild;

    public SqlWrapper(SqlItem... sqlItem) {
        sqlItemList.addAll(Arrays.asList(sqlItem));
    }

    public SqlWrapper where(WhereBuild whereBuild){
        this.whereBuild = whereBuild;
        return this;
    }

}
