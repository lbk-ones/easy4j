/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.base.plugin.dbaccess.dialect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.StatementUtil;
import cn.hutool.db.sql.Wrapper;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.CommonDBAccess;
import easy4j.module.base.plugin.dbaccess.Page;
import easy4j.module.base.plugin.dbaccess.condition.SqlBuild;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.utils.ListTs;
import jodd.util.StringPool;

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
public class AbstractDialect extends CommonDBAccess implements Dialect {

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
    public PreparedStatement psForBatchInsert(String tableName, String[] columns, List<Map<String, Object>> recordList, Connection connection) {
        return preparedBatchInsertAnsi(tableName, columns, recordList, connection);
    }

    private PreparedStatement preparedBatchInsertAnsi(String tableName, String[] columns, List<Map<String, Object>> recordList, Connection connection) {

        List<String> zwfList = ListTs.newArrayList();
        List<String> columnFieldNames = ListTs.newArrayList();
        for (String column : columns) {
            columnFieldNames.add(getWrapper().wrap(StrUtil.toUnderlineCase(column)));
            zwfList.add("?");
        }
        List<String> subSql = ListTs.newArrayList();
        subSql.add(VALUES);
        List<Object> objects = ListTs.newLinkedList();
        int size = recordList.size();
        for (int i = 0; i < size; i++) {
            Map<String, Object> objectMap = recordList.get(i);
            subSql.add("(" + String.join(", ", zwfList) + ")");
            if (i != size - 1) {
                subSql.add(",");
            }
            for (String column : columns) {
                Object o = objectMap.get(column);
                objects.add(o);
            }
        }
        String subSqlsStr = String.join(StringPool.SPACE, subSql);
        String finalSql = DDlLine(INSERT, tableName, subSqlsStr, columnFieldNames.toArray(new String[]{}));
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(finalSql);
            StatementUtil.fillParams(preparedStatement, objects);
            return preparedStatement;
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("preparedBatchInsertAnsi", finalSql, e);
        }

    }

    // 单个添加
    @Override
    public PreparedStatement psForInsert(String tableName, String[] columns, Map<String, Object> record, Connection connection) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(record);
        return preparedBatchInsertAnsi(tableName, columns, list, connection);
    }

    @Override
    public PreparedStatement psForUpdateById(String tableName, Object recordList, Class<?> aClass, boolean ignoreNull, Connection connection) {
        return null;
    }

    // 单个跟新
    @Override
    public PreparedStatement psForUpdateBy(String tableName, Map<String, Object> record, Class<?> aClass, Map<String, Object> updateCondition, boolean ignoreNull, Connection connection) {

        return psForUpdateByCondition(tableName, record, updateCondition, ignoreNull, connection);
    }

    @Override
    public PreparedStatement psForUpdateBySqlBuild(String tableName, Map<String, Object> record, SqlBuild sqlBuild, boolean ignoreNull, Connection connection) {

        List<Object> objects = ListTs.newCopyOnWriteArrayList();
        String buildSql = sqlBuild.build(objects);

        return psForUpdateBySqlBuildWith(tableName, record, ignoreNull, connection, objects, buildSql);

    }

    private PreparedStatement psForUpdateBySqlBuildWith(String tableName, Map<String, Object> record, boolean ignoreNull, Connection connection, List<Object> objects, String buildSql) {
        List<String> nameList = ListTs.newLinkedList();
        Set<String> strings = record.keySet();
        Wrapper wrapper = getWrapper();
        for (String column : strings) {
            Object o = record.get(column);
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
        String s = DDlLine(UPDATE, tableName, buildSql, nameList.toArray(new String[]{}));
        PreparedStatement preparedStatement = null;
        try {
            logSql(s, objects);
            preparedStatement = StatementUtil.prepareStatement(connection, s, objects.toArray(new Object[]{}));
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("psForUpdateBySqlBuider", s, e);
        } finally {
            JdbcHelper.close(preparedStatement);
        }
        return preparedStatement;
    }

    @Override
    public PreparedStatement psForUpdateBySqlBuildStr(String tableName, Map<String, Object> record, String sqlBuilder, List<Object> args, boolean ignoreNull, Connection connection) {
        return psForUpdateBySqlBuildWith(tableName, record, ignoreNull, connection, args, sqlBuilder);
    }

    private PreparedStatement psForUpdateByCondition(String tableName, Map<String, Object> record, Map<String, Object> updateCondition, boolean ignoreNull, Connection connection) {
        List<Object> objects = ListTs.newCopyOnWriteArrayList();
        List<String> nameList = ListTs.newLinkedList();
        Set<String> strings = record.keySet();
        Wrapper wrapper = getWrapper();
        for (String column : strings) {
            Object o = record.get(column);
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
        String s = DDlLine(UPDATE, tableName, null);
        return preparedStatement(updateCondition, connection, new StringBuilder(s).append(StringPool.SPACE), objects, nameList);
    }

    private void unionWhere(Map<String, Object> updateCondition, StringBuilder stringBuilder, List<Object> objects) {
        if (updateCondition.isEmpty() || updateCondition.values().stream().allMatch(ObjectUtil::isEmpty)) {
            throw new EasyException("update condition is empty , refuse update!");
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
    public PreparedStatement psForBatchUpdate(String tableName, String[] columns, List<Map<String, Object>> recordList, Map<String, Object> updateCondition, boolean ignoreNull, Connection connection) {
        if (recordList.isEmpty()) {
            throw new EasyException("No recordList need to be updated!");
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
            throw new EasyException("No columns need to be updated!");
        }
        return preparedStatement(updateCondition, connection, stringBuilder, objects, segmentList);
    }

    private PreparedStatement preparedStatement(Map<String, Object> updateCondition, Connection connection, StringBuilder stringBuilder, List<Object> objects, List<String> segmentList) {
        stringBuilder.append(String.join(",", segmentList));
        unionWhere(updateCondition, stringBuilder, objects);
        String string = StrUtil.trim(stringBuilder.toString());
        try {
            logSql(string, objects);
            PreparedStatement preparedStatement = connection.prepareStatement(string);
            StatementUtil.fillParams(preparedStatement, objects);
            return preparedStatement;
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("prepareStatement", string, e);
        }

    }
}
