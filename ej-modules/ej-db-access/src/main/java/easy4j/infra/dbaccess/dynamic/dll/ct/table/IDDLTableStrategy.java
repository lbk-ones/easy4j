package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;

import java.util.List;

public interface IDDLTableStrategy {

    void setToUnderLine(boolean toUnderLine);

    /**
     * 数据库类型匹配
     *
     * @param ddlTableInfo
     * @return
     */
    boolean match(DDLTableInfo ddlTableInfo);


    /**
     * 获取建表最外层模板
     *
     * @param ddlFieldInfo
     * @return
     */
    String getTableTemplate(DDLTableInfo ddlFieldInfo);

    /**
     * 获取单独的注释语句 如果是类似与mysql那样的注释就返回null或者空
     *
     * @author bokun.li
     * @date 2025/8/19
     */
    List<String> getComments(DDLTableInfo ddlFieldInfo);

    /**
     * 返回索引建立语句
     *
     * @param ddlFieldInfo
     * @return
     */
    List<String> getIndexes(DDLTableInfo ddlFieldInfo);

}
