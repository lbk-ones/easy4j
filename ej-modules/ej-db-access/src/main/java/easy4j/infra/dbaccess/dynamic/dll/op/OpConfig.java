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
package easy4j.infra.dbaccess.dynamic.dll.op;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.dynamic.dll.MySQLFieldType;
import easy4j.infra.dbaccess.dynamic.dll.OracleFieldType;
import easy4j.infra.dbaccess.dynamic.dll.PgSQLFieldType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpConfig
 * op的全局配置
 *
 * @author bokun.li
 * @date 2025/8/23
 */
@Data
@Accessors(chain = true)
public class OpConfig {

    private boolean toUnderLine = true;

    private boolean toLowCase = true;

    private boolean toUpperCase;

    private CommonDBAccess commonDBAccess = new CommonDBAccess();

    public String getColumnName(String columnName) {
        if (toUnderLine) {
            columnName = StrUtil.toUnderlineCase(columnName);
        }
        if (toLowCase || toUpperCase) {
            columnName = toLowCase ? columnName.toLowerCase() : columnName.toUpperCase();
        }

        return columnName;
    }

    public String getTxt(String txt) {
        if (toLowCase || toUpperCase) {
            txt = toLowCase ? txt.toLowerCase() : txt.toUpperCase();
        }
        return txt;
    }


    public String wrapQuote(String txt) {
        return StrUtil.wrap(txt, SP.SINGLE_QUOTE + SP.SINGLE_QUOTE, SP.SINGLE_QUOTE + SP.SINGLE_QUOTE);
    }

    public String wrapSingleQuote(String txt) {
        return StrUtil.wrap(txt, SP.SINGLE_QUOTE, SP.SINGLE_QUOTE);
    }


    /**
     * 根据typeName确定javaclass的类型
     *
     * @param typeName
     * @param dbType
     * @return
     */
    public Class<?> getJavaClassByTypeNameAndDbType(String typeName, String dbType) {

        switch (dbType) {
            case "mysql":
                return Optional.ofNullable(MySQLFieldType.getFromDataType(typeName)).map(MySQLFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
            case "postgres":
                return Optional.ofNullable(PgSQLFieldType.getFromDataType(typeName)).map(PgSQLFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
            case "oracle":
                return Optional.ofNullable(OracleFieldType.getFromDataType(typeName)).map(OracleFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
        }
        return null;
    }


    public <R> boolean isMatchMapIgnoreCase(Map<String, R> map, String key) {
        R matchMapIgnoreCase = getMatchMapIgnoreCase(map, key);
        return matchMapIgnoreCase != null;
    }

    public <R> R getMatchMapIgnoreCase(Map<String, R> map, String key) {
        if (ObjectUtil.isNotEmpty(map) && ObjectUtil.isNotEmpty(key)) {
            R res = null;
            R r = map.get(key);
            if (r == null) {
                R r2 = map.get(key.toLowerCase());
                if (r2 == null) {
                    R r3 = map.get(key.toUpperCase());
                    if (r3 != null) {
                        res = r3;
                    }
                } else {
                    res = r2;
                }
            } else {
                res = r;
            }
            return res;
        }
        return null;
    }

    /**
     * 通过不同数据库来判断当前字段类型是否是 json类型
     *
     * @param typeName
     * @param dbType
     * @return
     */
    public boolean isJson(String typeName, String dbType) {
        switch (dbType) {
            case "mysql":
                return MySQLFieldType.getFromDataType(typeName) == MySQLFieldType.JSON;
            case "postgres":
                PgSQLFieldType fromDataType = PgSQLFieldType.getFromDataType(typeName);
                return fromDataType == PgSQLFieldType.JSON || fromDataType == PgSQLFieldType.JSONB;
            case "oracle":
                break;
        }
        return false;
    }

    /**
     * 通过不同数据库来判断当前字段类型是否是 lob(大文本)类型
     *
     * @param typeName
     * @param dbType
     * @return
     */
    public boolean isLob(String typeName, String dbType) {
        switch (dbType) {
            case "mysql":
                return MySQLFieldType.getFromDataType(typeName) == MySQLFieldType.LONGTEXT;
            case "postgres":
                PgSQLFieldType fromDataType = PgSQLFieldType.getFromDataType(typeName);
                return fromDataType == PgSQLFieldType.TEXT;
            case "oracle":
                OracleFieldType fromDataType1 = OracleFieldType.getFromDataType(typeName);
                return fromDataType1 == OracleFieldType.CLOB;
        }
        return false;
    }


    public int getStrDefaultLength() {
        return 255;
    }

    public int getNumLengthDefaultLength() {
        return 6;
    }

    public int getNumDecimalDefaultLength() {
        return 4;
    }


    /**
     * 用正则提取字符串前面的数字
     *
     * @param str 待处理字符串
     * @return 开头的数字（无则返回空字符串）
     */
    public String extractPrefixNumberByRegex(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        // 正则：^匹配开头，\\d+匹配1个及以上数字
        Pattern pattern = Pattern.compile("^\\d+");
        Matcher matcher = pattern.matcher(str);
        // 找到匹配结果则返回，否则返回空
        return matcher.find() ? matcher.group() : "";
    }

    public boolean checkSupportVersion(String dbVersion, int version) {
        try {
            if (StrUtil.isNotBlank(dbVersion)) {
                String s = extractPrefixNumberByRegex(dbVersion);
                if (StrUtil.isNotBlank(s)) {
                    int version1 = Integer.parseInt(s);
                    return version >= version1;
                }
            }
        } catch (Exception e) {
        }
        return true;
    }

    /**
     * 是否是数字类型的默认值
     *
     * @author bokun.li
     * @date 2025-08-21
     */
    public boolean isNumberDefaultType(Class<?> fieldClass) {
        return fieldClass == byte.class ||
                fieldClass == Byte.class ||
                fieldClass == int.class ||
                fieldClass == Integer.class ||
                fieldClass == long.class ||
                fieldClass == Long.class ||
                fieldClass == short.class ||
                fieldClass == Short.class;
    }

    /**
     * 判断是否是时间类型
     *
     * @param fieldClass
     * @return
     */
    public boolean isDateDefaultType(Class<?> fieldClass) {

        return fieldClass == Date.class ||
                fieldClass == java.sql.Date.class ||
                fieldClass == java.sql.Timestamp.class ||
                fieldClass == java.sql.Time.class ||
                fieldClass == LocalTime.class ||
                fieldClass == LocalDate.class ||
                fieldClass == LocalDateTime.class;
    }


}
