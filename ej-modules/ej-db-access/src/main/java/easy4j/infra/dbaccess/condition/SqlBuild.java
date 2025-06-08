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
package easy4j.infra.dbaccess.condition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.common.exception.EasyException;
import jodd.util.StringPool;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 构建可执行sql
 *
 * @author bokun.li
 * @date 2025/6/3
 */
public class SqlBuild extends CommonDBAccess {


    private SqlBuild() {

    }

    private static class SqlBuildHolder {
        private static final SqlBuild INSTANCE = new SqlBuild();
    }

    public static SqlBuild get() {
        return SqlBuildHolder.INSTANCE;
    }


    /**
     * 单表简单sql构建
     * SELECT,INSERT,UPDATE 需要传入值
     * INSERT 可以不用传条件构造器
     *
     * @param sqlType    构建的sql类型
     * @param whereBuild
     * @param aclass     实体bean的class
     * @param obj        如果是要更新则需要携带这个
     * @param returnZwf  返回字符串是否携带占位符 ?  直接返回sql有风险 返回的sql并不一定能百分百执行
     * @param argList    参数集合
     * @param <T>
     * @return
     */
    public <T> String build(
            String sqlType,
            WhereBuild whereBuild,
            Class<T> aclass,
            T obj,
            boolean returnZwf,
            List<Object> argList,
            Connection connection,
            Dialect dialect
    ) {
        if (!ListTs.asList(SELECT, DELETE, UPDATE, INSERT).contains(sqlType)) {
            throw new EasyException("SqlType is inValid");
        }
        if (INSERT.equals(sqlType) && Objects.nonNull(whereBuild)) {
            throw new EasyException("insert sql no where segment!");
        }
        String tableName = getTableName(aclass, dialect);
        List<Object> objects = ListTs.newArrayList();
        String build = Optional.ofNullable(whereBuild).map(e -> e.build(objects)).orElse("");
        List<String> selectFields = Optional.ofNullable(whereBuild).map(WhereBuild::getSelectFields).orElse(new ArrayList<>());
        String sql = null;
        List<Object> frgment1SqlValue = ListTs.newArrayList();
        switch (sqlType) {
            case SELECT:
                sql = DDlLine(sqlType, tableName, where(build), selectFields.toArray(new String[]{}));
                sql = distinctSql(whereBuild, selectFields, sql);
                break;
            case DELETE:
                sql = DDlLine(sqlType, tableName, where(build));
                break;
            case UPDATE:
                if (obj == null) {
                    throw new EasyException("please input update obj");
                }
                Map<String, Object> stringObjectMap = castBeanMap(obj, true, true);
                String[] array = (String[]) stringObjectMap.keySet().stream().map(e -> {
                    Object o = stringObjectMap.get(e);
                    String re = e + StringPool.SPACE + "=" + StringPool.SPACE;
                    if (returnZwf) {
                        re += "?";
                    } else {
                        re += Convert.toStr(o);
                    }
                    frgment1SqlValue.add(re);
                    return re;
                }).toArray();
                sql = DDlLine(sqlType, tableName, where(build), array);
                break;
            case INSERT:
                Map<String, Object> keyMap = castBeanMap(obj, true, true);
                List<String> name = ListTs.newArrayList();
                for (String s : keyMap.keySet()) {
                    name.add(s);
                    Object o = keyMap.get(s);
                    frgment1SqlValue.add(o);
                }
                if (CollUtil.isEmpty(frgment1SqlValue)) {
                    throw new EasyException("insert segment should have values fields!");
                }
                String subSql = VALUES +
                        StringPool.SPACE +
                        StringPool.LEFT_BRACKET +
                        frgment1SqlValue.stream().map(e -> "?").collect(Collectors.joining(StringPool.COMMA + StringPool.SPACE)) +
                        StringPool.RIGHT_BRACKET;

                sql = DDlLine(sqlType, tableName, subSql, name.toArray(new String[]{}));

                break;
        }
        if (CollUtil.isNotEmpty(objects)) {
            frgment1SqlValue.addAll(objects);
        }
        if (argList != null && CollUtil.isNotEmpty(frgment1SqlValue)) {
            argList.addAll(frgment1SqlValue);
        }
        if (returnZwf) {
            return sql;
        } else {
            return getSql(sql, connection, frgment1SqlValue.toArray(new Object[]{}));
        }
    }

    private static String distinctSql(WhereBuild whereBuild, List<String> selectFields, String sql) {
        if (whereBuild != null && whereBuild.isDistinct() && CollUtil.isNotEmpty(selectFields)) {
            sql = sql.replaceAll(SELECT, SELECT + " DISTINCT");
        }
        return sql;
    }


}
