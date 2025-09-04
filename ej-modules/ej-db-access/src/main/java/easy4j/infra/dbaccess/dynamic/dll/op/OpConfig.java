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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.RegexEscapeUtils;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dynamic.dll.*;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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


    // 是否自动执行ddl语句 默认不执行 只返回语句
    private boolean autoExeDDL;


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

    public String getColumnNameAndEscape(String columnName, Connection connection) {
        if (toUnderLine) {
            columnName = StrUtil.toUnderlineCase(columnName);
        }
        if (toLowCase || toUpperCase) {
            columnName = toLowCase ? columnName.toLowerCase() : columnName.toUpperCase();
        }

        return escapeCn(columnName, connection);
    }

    /**
     * 转义
     *
     * @author bokun.li
     * @date 2025/9/4
     */
    public String escapeCn(String name, Connection connection) {

        Dialect dialect = JdbcHelper.getDialect(connection);
        Wrapper wrapper = dialect.getWrapper();

        return wrapper.wrap(name);
    }

    /**
     * 拆分并转义
     *
     * @author bokun.li
     * @date 2025/9/4
     */
    public String splitStrAndEscape(String str, String comma, Connection connection) {
        if(StrUtil.isBlank(str) || StrUtil.isBlank(comma)) return str;
        String s = RegexEscapeUtils.escapeRegex(comma);
        String[] split = str.split(s);
        List<String> list = ListTs.asList(split);
        return list.stream().map(e -> escapeCn(e, connection)).collect(Collectors.joining(comma));
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
        if (StrUtil.equalsIgnoreCase(DbType.MYSQL.getDb(), dbType)) {
            return Optional.ofNullable(MySQLFieldType.getFromDataType(typeName)).map(MySQLFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
        } else if (StrUtil.equalsIgnoreCase(DbType.POSTGRE_SQL.getDb(), dbType)) {
            return Optional.ofNullable(PgSQLFieldType.getFromDataType(typeName)).map(PgSQLFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
        } else if (StrUtil.equalsIgnoreCase(DbType.ORACLE.getDb(), dbType)) {
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
        if (StrUtil.equalsIgnoreCase(DbType.MYSQL.getDb(), dbType)) {
            return MySQLFieldType.getFromDataType(typeName) == MySQLFieldType.JSON;
        } else if (StrUtil.equalsIgnoreCase(DbType.POSTGRE_SQL.getDb(), dbType)) {
            PgSQLFieldType fromDataType = PgSQLFieldType.getFromDataType(typeName);
            return fromDataType == PgSQLFieldType.JSON || fromDataType == PgSQLFieldType.JSONB;
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
        if (StrUtil.equalsIgnoreCase(DbType.MYSQL.getDb(), dbType)) {
            return MySQLFieldType.getFromDataType(typeName) == MySQLFieldType.LONGTEXT;

        } else if (StrUtil.equalsIgnoreCase(DbType.POSTGRE_SQL.getDb(), dbType)) {
            PgSQLFieldType fromDataType = PgSQLFieldType.getFromDataType(typeName);
            return fromDataType == PgSQLFieldType.TEXT;
        } else if (StrUtil.equalsIgnoreCase(DbType.ORACLE.getDb(), dbType)) {
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
                fieldClass == float.class ||
                fieldClass == Float.class ||
                fieldClass == double.class ||
                fieldClass == Double.class ||
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

    public String getTableName(DDLTableInfo ddlTableInfo) {
        String tableName = ddlTableInfo.getTableName();
        String tableInfoSchema = ddlTableInfo.getSchema();
        return StrUtil.isNotBlank(tableInfoSchema) ? tableInfoSchema + SP.DOT + tableName : tableName;
    }


    /**
     * 模板填值 并清除未填值的
     *
     * @param ddlTableInfo 表格建模
     * @param tempStr      模板字符串
     * @param FIELD_MAP    字符串参数map
     * @param extParamMap  额外的字符串额外参数map 和 FIELD_MAP 不一样 FIELD_MAP 只存参数名称 extParamMap是参数名称对应值
     * @param function     获取参数值的函数
     * @return
     * @author bokun.li
     */
    public <T> String patchStrWithTemplate(T ddlTableInfo, String tempStr, Map<String, String> FIELD_MAP, Map<String, String> extParamMap, Function<T, Map<String, String>> function) {
        Map<String, String> templateParams = function.apply(ddlTableInfo);
        if (CollUtil.isNotEmpty(extParamMap)) {
            templateParams.putAll(extParamMap);
        }
        // replace templateParams keys
        for (String key : templateParams.keySet()) {
            String s = templateParams.get(key);
            String wrap = StrUtil.wrap(key, SP.LEFT_SQ_BRACKET, SP.RIGHT_SQ_BRACKET);
            tempStr = tempStr.replace(wrap, s);
        }
        return clearTemplate(FIELD_MAP, tempStr);
    }

    /**
     * 清除模板字符串中的空格 和最后一个空格
     *
     * @param FIELD_MAP 参数map
     * @param template  替换之后的模板字符
     * @return String
     * @author bokun.li
     */
    public String clearTemplate(Map<String, String> FIELD_MAP, String template) {
        // replace field map
        for (String s : FIELD_MAP.keySet()) {
            String wrap = StrUtil.wrap(s, SP.LEFT_SQ_BRACKET, SP.RIGHT_SQ_BRACKET);
            template = template.replace(wrap, "");
        }
        template = template.replaceAll(" +", " ");
        template = StrUtil.trim(template);
        if (template.endsWith("\n")) {
            template = StrUtil.replaceLast(template, "\n", "");
        }
        return StrUtil.trim(template);
    }


    /**
     * 过滤特殊符号，然后转下划线
     *
     * @param wt
     * @return
     */
    public String replaceSpecialSymbol(String wt) {
        if (StrUtil.isBlank(wt)) return "";
        return wt.replaceAll("[^a-zA-Z0-9_\u4e00-\u9fa5]", "_").replaceAll("_+", "_");
    }

    /**
     * 往字符串拼接前缀
     *
     * @param prefix
     * @param text
     * @return
     */
    public String concatPrefix(String prefix, String text) {
        if (StrUtil.isNotBlank(text)) {
            return StrUtil.blankToDefault(prefix, "") + text;
        }
        return text;
    }

    /**
     * 往字符串拼接后缀
     *
     * @param suffix
     * @param text
     * @return
     */
    public String concatSuffix(String suffix, String text) {
        if (StrUtil.isNotBlank(text)) {
            return text + StrUtil.blankToDefault(suffix, "");
        }
        return text;
    }

    /**
     * 简写下划线字符串
     *
     * @param underLineName
     * @return
     */
    public String get63UnderLineName(String underLineName) {

        if (StrUtil.isBlank(underLineName)) return underLineName;
        String[] split = underLineName.split("_");
        String var1 = ListTs.get(split, 0);
        String var2 = ListTs.get(split, 1);
        List<String> objects = ListTs.asList(var1, var2);
        int length = split.length;
        List<String> objects2 = ListTs.asList();
        if (length > 2) {
            for (int i = 2; i < length; i++) {
                String s = split[i];
                char c = s.charAt(0);
                objects2.add(String.valueOf(c));
            }
        }
        String s = String.join(SP.UNDERSCORE, objects) + concatPrefix(SP.UNDERSCORE, String.join("", objects2));
        if (s.length() > 63) {
            s = s.substring(0, 63);
        }
        return s;
    }


    public String compatibleGetIdxName(DDLIndexInfo ddlIndexInfo, String[] keys, String indexTypeName, OpConfig opConfig, String name) {
        if (keys != null && keys.length > 0) {
            String lowerCase = StrUtil.blankToDefault(StrUtil.blankToDefault(indexTypeName, "").toLowerCase(), ddlIndexInfo.getIndexNamePrefix());
            String collect = ListTs.asList(keys)
                    .stream()
                    .map(StrUtil::toUnderlineCase)
                    .collect(Collectors.joining("_"));

            collect = opConfig.replaceSpecialSymbol(collect);
            name = (StrUtil.isBlank(lowerCase) ? "" : (lowerCase + "_")) + "idx_" + ddlIndexInfo.getTableName().toLowerCase() + "_" + collect;
            name = opConfig.replaceSpecialSymbol(name);
        }
        return name;
    }


}
