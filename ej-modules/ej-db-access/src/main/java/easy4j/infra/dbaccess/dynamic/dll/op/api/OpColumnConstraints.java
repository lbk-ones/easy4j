package easy4j.infra.dbaccess.dynamic.dll.op.api;

import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

/**
 * OpColumnConstraints
 * 列约束
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public interface OpColumnConstraints extends IOpContext {

    boolean match(OpContext opContext);

    /**
     * 获取列约束
     *
     * @param ddlFieldInfo
     * @return
     */
    String getColumnConstraints(DDLFieldInfo ddlFieldInfo);

    /**
     * 获取字段名称
     *
     * @param ddlFieldInfo
     * @return
     */
    String getFieldName(DDLFieldInfo ddlFieldInfo);

    /**
     * 获取字段类型
     *
     * @param ddlFieldInfo
     * @return
     */
    String getDataType(DDLFieldInfo ddlFieldInfo);

    /**
     * 获取字段类型 后面的额外属性 在列约束前面
     *
     * @param ddlFieldInfo
     * @return
     */
    String getDataTypeExtra(DDLFieldInfo ddlFieldInfo);

    /**
     * 获取创建表或者修改表时候的完全型sql
     *
     * @param ddlFieldInfo
     * @return
     */
    String getCreateColumnSql(DDLFieldInfo ddlFieldInfo);


}
