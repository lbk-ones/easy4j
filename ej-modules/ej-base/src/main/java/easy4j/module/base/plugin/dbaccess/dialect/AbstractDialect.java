package easy4j.module.base.plugin.dbaccess.dialect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.StatementUtil;
import cn.hutool.db.sql.Wrapper;
import easy4j.module.base.plugin.dbaccess.Page;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcTable;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.utils.ListTs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static easy4j.module.base.plugin.dbaccess.AbstractDBAccess.getSelectByMap;

/**
 * 标准sql方言实现
 * Ansi Sql Dialect
 */
public class AbstractDialect implements Dialect {

    @Override
    public String getPageSql(String sql, Page<?> page) {
        return null;
    }

    @Override
    public Wrapper getWrapper() {
        return new Wrapper();
    }

    /**
     * 标准批量插入语句
     * insert into user (name,age) values(?,?),(?,?)
     *
     * @param tableName
     * @param columns
     * @param recordList
     * @param connection
     * @return
     * @throws SQLException
     */
    @Override
    public PreparedStatement psForBatchInsert(String tableName, String[] columns, List<Map<String, Object>> recordList, Connection connection) throws SQLException {
        return preparedBatchInsertAnsi(tableName, columns, recordList, connection);
    }

    private PreparedStatement preparedBatchInsertAnsi(String tableName, String[] columns, List<Map<String, Object>> recordList, Connection connection) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder("insert into");
        stringBuilder.append(" ");
        stringBuilder.append(tableName);
        stringBuilder.append("(");
        List<String> zwfList = ListTs.newArrayList();
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            stringBuilder.append(getWrapper().wrap(StrUtil.toUnderlineCase(column)));
            if (i != columns.length - 1) {
                stringBuilder.append(",");
            }
            zwfList.add("?");
        }
        stringBuilder.append(")");
        stringBuilder.append(" values ");

        List<Object> objects = ListTs.newLinkedList();
        for (Map<String, Object> objectMap : recordList) {
            stringBuilder.append("(");
            stringBuilder.append(String.join(" , ", zwfList));
            stringBuilder.append(")");
            stringBuilder.append(",");
            for (String column : columns) {
                Object o = objectMap.get(column);
                objects.add(o);
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        String string = stringBuilder.toString();
        String trim = StrUtil.trim(string);
        PreparedStatement preparedStatement = connection.prepareStatement(trim);
        StatementUtil.fillParams(preparedStatement, objects);
        return preparedStatement;
    }

    // 单个添加
    @Override
    public PreparedStatement psForInsert(String tableName, String[] columns, Map<String, Object> record, Connection connection) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(record);
        return preparedBatchInsertAnsi(tableName, columns, list, connection);
    }

    @Override
    public PreparedStatement psForUpdateById(String tableName, Object recordList, Class<?> aClass, boolean ignoreNull, Connection connection) throws SQLException {
        return null;
    }

    // 单个跟新
    @Override
    public PreparedStatement psForUpdateBy(String tableName, Map<String, Object> recordList, Class<?> aClass, Map<String, Object> updateCondition, boolean ignoreNull, Connection connection) throws SQLException {

        return psForUpdateByCondition(tableName, recordList, updateCondition, ignoreNull, connection);
    }

    private PreparedStatement psForUpdateByCondition(String tableName, Map<String, Object> recordList, Map<String, Object> updateCondition, boolean ignoreNull, Connection connection) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder("update");
        stringBuilder.append(" ");
        stringBuilder.append(tableName);
        stringBuilder.append(" set ");
        List<Object> objects = ListTs.newCopyOnWriteArrayList();
        List<String> nameList = ListTs.newLinkedList();
        List<String> columns = ListTs.newArrayList();
        Set<String> strings = recordList.keySet();
        Wrapper wrapper = getWrapper();
        for (String column : strings) {
            Object o = recordList.get(column);
            if (ignoreNull) {
                if (!ObjectUtil.isEmpty(o)) {
                    objects.add(o);
                    nameList.add(wrapper.wrap(StrUtil.toUnderlineCase(column)) + " = ? ");
                }
            } else {
                objects.add(o);
                nameList.add(wrapper.wrap(StrUtil.toUnderlineCase(column)) + " = ? ");
            }
        }
        return preparedStatement(updateCondition, connection, stringBuilder, objects, nameList);
    }

    private void unionWhere(Map<String, Object> updateCondition, StringBuilder stringBuilder, List<Object> objects) throws SQLException {
        if (updateCondition.isEmpty() || updateCondition.values().stream().allMatch(ObjectUtil::isEmpty)) {
            throw new SQLException("update condition is empty , refuse update!");
        }
        stringBuilder.append(" where ");
        stringBuilder.append(getSelectByMap(updateCondition, objects, getWrapper(), true));
    }

    /**
     * 批量更新
     * UPDATE employees
     * SET
     * department = CASE
     * WHEN id = ? AND department = ? THEN ?
     * WHEN id = ? AND department = ? THEN ?
     * ELSE department
     * END,
     * salary = CASE
     * WHEN id = ? AND department = ? THEN ?
     * WHEN id = ? AND department = ? THEN ?
     * ELSE salary
     * END
     * WHERE id IN (101, 102) AND department = '销售部'; -- 过滤条件
     *
     * @param tableName       要更新的表
     * @param columns         要更新的列
     * @param recordList      将要更新的数据
     * @param updateCondition 更新的条件
     * @param ignoreNull      是否忽略空值
     * @param connection
     * @return
     * @throws SQLException
     */
    @Override
    public PreparedStatement psForBatchUpdate(String tableName, String[] columns, List<Map<String, Object>> recordList, Map<String, Object> updateCondition, boolean ignoreNull, Connection connection) throws SQLException {
        if (recordList.isEmpty()) {
            throw new SQLException("No recordList need to be updated!");
        }
        // 单条不走批量
        if (recordList.size() == 1) {
            return psForUpdateByCondition(tableName, recordList.get(0), updateCondition, ignoreNull, connection);
        }
        StringBuilder stringBuilder = new StringBuilder("update");
        stringBuilder.append(" ");
        stringBuilder.append(tableName);
        stringBuilder.append(" set ");
        List<Object> objects = ListTs.newCopyOnWriteArrayList();
        // 遍历要修改的参数
        List<String> segmentList = ListTs.newArrayList();
        Wrapper wrapper = getWrapper();
        for (String column : columns) {
            StringBuilder var0 = new StringBuilder(wrapper.wrap(StrUtil.toUnderlineCase(column)));
            var0.append(" = ( ");
            var0.append(" case ");
            List<String> whenList = ListTs.newArrayList();
            for (Map<String, Object> objectMap : recordList) {
                Set<String> conditionKeys = updateCondition.keySet();
                List<String> var1List = ListTs.newArrayList();
                Object o = objectMap.get(column);
                if (ObjectUtil.isEmpty(o) && ignoreNull) {
                    continue;
                }
                // 大概形成下面的数据格式
                // join = id = ? AND department = ? THEN ?
                StringBuilder var2 = new StringBuilder("when ");
                // 遍历条件
                for (String column2 : conditionKeys) {
                    Object o2 = objectMap.get(column2);
                    String var1 = wrapper.wrap(StrUtil.toUnderlineCase(column2)) + "=" + "?";
                    objects.add(o2);
                    var1List.add(var1);
                }
                var2.append(String.join(" and ", var1List));
                var2.append(" then ?");
                whenList.add(var2.toString());
                objects.add(o);
            }
            // 为空这个字段就丢失 不给它赋值
            if (whenList.isEmpty()) {
                continue;
            }
            var0.append(String.join(" ", whenList));
            var0.append(" else ").append(wrapper.wrap(StrUtil.toUnderlineCase(column)));
            var0.append(" end ");
            var0.append(")");
            segmentList.add(StrUtil.trim(var0.toString()));
        }
        if (CollUtil.isEmpty(segmentList)) {
            throw new SQLException("No columns need to be updated!");
        }
        return preparedStatement(updateCondition, connection, stringBuilder, objects, segmentList);
    }

    private PreparedStatement preparedStatement(Map<String, Object> updateCondition, Connection connection, StringBuilder stringBuilder, List<Object> objects, List<String> segmentList) throws SQLException {
        stringBuilder.append(String.join(",", segmentList));
        unionWhere(updateCondition, stringBuilder, objects);
        String string = StrUtil.trim(stringBuilder.toString());
        PreparedStatement preparedStatement = connection.prepareStatement(string);
        StatementUtil.fillParams(preparedStatement, objects);
        return preparedStatement;
    }
}
