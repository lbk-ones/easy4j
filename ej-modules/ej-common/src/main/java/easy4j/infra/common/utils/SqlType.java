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
package easy4j.infra.common.utils;


import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.enums.DbType;

import java.util.*;

/**
 * 判断数据库类型 公共类 包括驱动名称和sql校验语句
 *
 * @author bokun.li
 * @date 2023/11/18
 */
public final class SqlType {
    public static String datatype1 = "mysql";
    public static String datatype2 = "oracle";
    public static String datatype3 = "sqlserver";
    public static String datatype4 = "h2";
    public static String datatype5 = "postgresql";
    public static String datatype6 = "db2";
    private static final Map<String, String> DATASOURCECLASS = new HashMap<>();
    private static final Map<String, String> VALIDATIONQUERY = new HashMap<>();

    static {
        DATASOURCECLASS.put(datatype1, "com.mysql.cj.jdbc.Driver");
        DATASOURCECLASS.put(datatype2, "oracle.jdbc.driver.OracleDriver");
        DATASOURCECLASS.put(datatype3, "com.microsoft.jdbc.sqlserver.SQLServerDriver");
        DATASOURCECLASS.put(datatype4, "org.h2.Driver");
        DATASOURCECLASS.put(datatype5, "org.postgresql.Driver");
        DATASOURCECLASS.put(datatype6, "com.ibm.db2.jcc.DB2Driver");

        VALIDATIONQUERY.put(datatype1, "select 'x'");
        VALIDATIONQUERY.put(datatype2, "select 'x' from dual");
        VALIDATIONQUERY.put(datatype3, "select 1");
        VALIDATIONQUERY.put(datatype4, "select 1");
        VALIDATIONQUERY.put(datatype5, "select 1");
        VALIDATIONQUERY.put(datatype6, "SELECT 1 FROM SYSIBM.SYSDUMMY1");

    }

    /**
     * 根据数据库连接来判断到底是哪个数据库 目前只能判断mysql oracle sqlServer
     *
     * @author bokun.li
     * @date 2022/2/23
     */
    public static String getDataTypeByUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        String s = url.toLowerCase();
        if (StrUtil.containsAnyIgnoreCase(s, datatype1)) {
            return datatype1;
        }
        if (StrUtil.containsAnyIgnoreCase(s, datatype2)) {
            return datatype2;
        }
        if (StrUtil.containsAnyIgnoreCase(s, datatype3)) {
            return datatype3;
        }
        if (StrUtil.containsAnyIgnoreCase(s, datatype4)) {
            return datatype4;
        }
        if (StrUtil.containsAnyIgnoreCase(s, datatype5)) {
            return datatype5;
        }
        if (StrUtil.containsAnyIgnoreCase(s, datatype6)) {
            return datatype6;
        }
        return null;
    }

    /**
     * 根据链接获取sql校验的名字
     *
     * @param url
     * @return
     */
    public static String getValidateSqlByUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        String s = url.toLowerCase();
        String dataType = getDataTypeByUrl(s);
        return VALIDATIONQUERY.get(dataType);
    }

    /**
     * 根据链接获取sql校验的名字
     *
     * @param url
     * @return
     */
    public static String getDriverClassNameByUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        String s = url.toLowerCase();
        String dataType = getDataTypeByUrl(s);
        return DATASOURCECLASS.get(dataType);
    }

    /**
     * 获取sql校验的名字
     *
     * @param name
     * @return
     */
    public static String getValidateSql(String name) {
        String s = name.toLowerCase();
        if (s.contains(datatype1)) {
            return VALIDATIONQUERY.get(datatype1);
        }
        if (s.contains(datatype2)) {
            return VALIDATIONQUERY.get(datatype2);
        }
        if (s.contains(datatype3)) {
            return VALIDATIONQUERY.get(datatype3);
        }
        if (s.contains(datatype4)) {
            return VALIDATIONQUERY.get(datatype4);
        }
        if (s.contains(datatype5)) {
            return VALIDATIONQUERY.get(datatype5);
        }
        return null;
    }

    /**
     * 获取驱动名称
     *
     * @author bokun.li
     * @date 2022/2/23
     */
    public static String getDriverClassName(String name) {
        String s = name.toLowerCase();
        if (s.contains(datatype1)) {
            return DATASOURCECLASS.get(datatype1);
        }
        if (s.contains(datatype2)) {
            return DATASOURCECLASS.get(datatype2);
        }
        if (s.contains(datatype3)) {
            return DATASOURCECLASS.get(datatype3);
        }
        if (s.contains(datatype4)) {
            return DATASOURCECLASS.get(datatype4);
        }
        if (s.contains(datatype5)) {
            return DATASOURCECLASS.get(datatype5);
        }
        return null;
    }

    public static Set<String> getIgnoreStateMent() {
        Collection<String> values = VALIDATIONQUERY.values();
        return new HashSet<>(ListTs.newArrayList(values.iterator()));
    }


}
