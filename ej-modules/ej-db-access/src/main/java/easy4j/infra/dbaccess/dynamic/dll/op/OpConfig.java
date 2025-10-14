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
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import com.google.common.collect.Maps;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.RegexEscapeUtils;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.*;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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

    // 不用怀疑这些保留字符的完整与否
    // private static final List<String> ORACLE_ESCAPE = ListTs.asList("or", "decimal", "create", "from", "public", "union", "nowait", "raw", "to", "pctfree", "values", "default", "grant", "with", "table", "alter", "<", "select", "varchar", "any", "|", "-", "group", "identified", "/", "^", "null", "connect", "view", "distinct", "set", "by", "order", "minus", "prior", "asc", "varchar2", "all", "+", "drop", "and", "lock", "intersect", "having", "on", "update", "between", "exists", ":", "integer", "insert", "for", "char", "smallint", "=", "mode", "revoke", "else", ">", "in", "rename", "trigger", "number", "synonym", ".", "cluster", "start", "share", "of", "option", "into", "compress", "where", "*", "check", "then", "as", "[", "unique", "]", "@", ",", "long", "size", "(", "delete", "not", ")", "desc", "date", "resource", "float", "is", "like", "exclusive", "&", "!", "nocompress", "index", "null");
    // private static final List<String> PG_ESCAPE = ListTs.asList("all", "analyse", "analyze", "and", "any", "array", "as", "asc", "asymmetric", "both", "case", "cast", "check", "collate", "column", "constraint", "create", "current_catalog", "current_date", "current_role", "current_time", "current_timestamp", "current_user", "default", "deferrable", "desc", "distinct", "do", "else", "end", "except", "false", "fetch", "for", "foreign", "from", "grant", "group", "having", "in", "initially", "intersect", "into", "lateral", "leading", "limit", "localtime", "localtimestamp", "not", "null", "offset", "on", "only", "or", "order", "placing", "primary", "references", "returning", "select", "session_user", "some", "symmetric", "table", "then", "to", "trailing", "true", "union", "unique", "user", "using", "variadic", "when", "where", "window", "with");
    // private static final List<String> MYSQL_ESCAPE = ListTs.asList("accessible", "add", "all", "alter", "analyze", "and", "as", "asc", "asensitive", "before", "between", "bigint", "binary", "blob", "both", "by", "call", "cascade", "case", "change", "char", "character", "check", "collate", "column", "condition", "constraint", "continue", "convert", "create", "cross", "cube", "cume_dist", "current_date", "current_time", "current_timestamp", "current_user", "cursor", "database", "databases", "day_hour", "day_microsecond", "day_minute", "day_second", "dec", "decimal", "declare", "default", "delayed", "delete", "dense_rank", "desc", "describe", "deterministic", "distinct", "distinctrow", "div", "double", "drop", "dual", "each", "else", "elseif", "empty", "enclosed", "escaped", "except", "exists", "exit", "explain", "false", "fetch", "first_value", "float", "float4", "float8", "for", "force", "foreign", "from", "fulltext", "function", "generated", "get", "grant", "group", "grouping", "groups", "having", "high_priority", "hour_microsecond", "hour_minute", "hour_second", "if", "ignore", "in", "index", "infile", "inner", "inout", "insensitive", "insert", "int", "int1", "int2", "int3", "int4", "int8", "integer", "intersect", "interval", "into", "io_after_gtids", "io_before_gtids", "is", "iterate", "join", "json_table", "key", "keys", "kill", "lag", "last_value", "lateral", "lead", "leading", "leave", "left", "like", "limit", "linear", "lines", "load", "localtime", "localtimestamp", "lock", "long", "longblob", "longtext", "loop", "low_priority", "master_bind", "master_ssl_verify_server_cert", "match", "maxvalue", "mediumblob", "mediumint", "mediumtext", "middleint", "minute_microsecond", "minute_second", "mod", "modifies", "natural", "not", "no_write_to_binlog", "nth_value", "ntile", "null", "numeric", "of", "on", "optimize", "optimizer_costs", "option", "optionally", "or", "order", "out", "outer", "outfile", "over", "partition", "percent_rank", "precision", "primary", "procedure", "purge", "range", "rank", "read", "reads", "read_write", "real", "recursive", "references", "regexp", "release", "rename", "repeat", "replace", "require", "resignal", "restrict", "return", "revoke", "right", "rlike", "row", "rows", "row_number", "schema", "schemas", "second_microsecond", "select", "sensitive", "separator", "set", "show", "signal", "smallint", "spatial", "specific", "sql", "sqlexception", "sqlstate", "sqlwarning", "sql_big_result", "sql_calc_found_rows", "sql_small_result", "ssl", "starting", "stored", "straight_join", "system", "table", "terminated", "then", "tinyblob", "tinyint", "tinytext", "to", "trailing", "trigger", "true", "undo", "union", "unique", "unlock", "unsigned", "update", "usage", "use", "using", "utc_date", "utc_time", "utc_timestamp", "values", "varbinary", "varchar", "varcharacter", "varying", "virtual", "when", "where", "while", "window", "with", "write", "xor", "year_month", "zerofill");
    // private static final List<String> H2_ESCAPE = ListTs.asList("all", "and", "any", "array", "as", "asymmetric", "authorization", "between", "both", "case", "cast", "check", "constraint", "cross", "current_catalog", "current_date", "current_path", "current_role", "current_schema", "current_time", "current_timestamp", "current_user", "day", "default", "distinct", "else", "end", "except", "exists", "false", "fetch", "for", "foreign", "from", "full", "group", "groups", "having", "hour", "if", "ilike", "in", "inner", "intersect", "interval", "is", "join", "key", "leading", "left", "like", "limit", "localtime", "localtimestamp", "minus", "minute", "month", "natural", "not", "null", "offset", "on", "or", "order", "over", "partition", "primary", "qualify", "range", "regexp", "right", "row", "rownum", "rows", "second", "select", "session_user", "set", "some", "symmetric", "system_user", "table", "to", "top", "ms", "cs", "trailing", "true", "uescape", "union", "unique", "unknown", "user", "using", "value", "values", "when", "where", "window", "with", "year", "_rowid_");
    // private static final List<String> MSSQL_ESCAPE = ListTs.asList("add","external","procedure","all","fetch","public","alter","file","raiserror","and","fillfactor","read","any","for","readtext","as","foreign","reconfigure","asc","freetext","references","authorization","freetexttable","replication","backup","from","restore","begin","full","restrict","between","function","return","break","goto","revert","browse","grant","revoke","bulk","group","right","by","having","rollback","cascade","holdlock","rowcount","case","identity","rowguidcol","check","identity_insert","rule","checkpoint","identitycol","save","close","if","schema","clustered","in","securityaudit","coalesce","index","select","collate","inner","semantickeyphrasetable","column","insert","semanticsimilaritydetailstable","commit","intersect","semanticsimilaritytable","compute","into","session_user","constraint","is","set","contains","join","setuser","containstable","key","shutdown","continue","kill","some","convert","left","statistics","create","like","system_user","cross","lineno","table","current","load","tablesample","current_date","merge","textsize","current_time","national","then","current_timestamp","nocheck","to","current_user","nonclustered","top","cursor","not","tran","database","null","transaction","dbcc","nullif","trigger","deallocate","of","truncate","declare","off","try_convert","default","offsets","tsequal","delete","on","union","deny","open","unique","desc","opendatasource","unpivot","disk","openquery","update","distinct","openrowset","updatetext","distributed","openxml","use","double","option","user","drop","or","values","dump","order","varying","else","outer","view","end","over","waitfor","errlvl","percent","when","escape","pivot","where","except","plan","while","exec","precision","with","execute","primary","within group","exists","print","writetext","exit","proc");

    private boolean toUnderLine = true;

    private boolean toLowCase = true;

    private boolean toUpperCase;


    // 是否自动执行ddl语句 默认不执行 只返回语句
    private boolean autoExeDDL;


    public static Map<String,Wrapper> dbVsWrapper = Maps.newHashMap();

    static {
        dbVsWrapper.put(DbType.MYSQL.getDb(), new Wrapper('`','`'));
        dbVsWrapper.put(DbType.ORACLE.getDb(), new Wrapper('"','"'));
        dbVsWrapper.put(DbType.H2.getDb(), new Wrapper('"','"'));
        dbVsWrapper.put(DbType.POSTGRE_SQL.getDb(), new Wrapper('"','"'));
        dbVsWrapper.put(DbType.SQL_SERVER.getDb(), new Wrapper('[',']'));
        dbVsWrapper.put(DbType.DB2.getDb(), new Wrapper('"','"'));
        dbVsWrapper = Collections.unmodifiableMap(dbVsWrapper);
    }

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

    public String getColumnNameAndEscape(String columnName, Connection connection, boolean forceEscape) {
        if (toUnderLine) {
            columnName = StrUtil.toUnderlineCase(columnName);
        }
        if (toLowCase || toUpperCase) {
            columnName = toLowCase ? columnName.toLowerCase() : columnName.toUpperCase();
        }

        return escapeCn(columnName, connection, forceEscape);
    }

//    /**
//     * 校验字符是否为英文字母（大写或小写）
//     * @param c 要校验的字符
//     * @return 如果是英文字母返回 true，否则返回 false
//     */
//    public  boolean isEnglishLetter(char c) {
//        // 检查是否是大写字母 (A-Z) 或小写字母 (a-z)
//        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
//    }
//
//    public boolean containUpper(String name){
//        char[] charArray = name.toCharArray();
//        for (char c : charArray) {
//            if (isEnglishLetter(c) && StrUtil.isUpperCase(String.valueOf(c))) {
//                return true;
//            }
//        }
//        return false;
//    }
//    public boolean containLower(String name){
//        char[] charArray = name.toCharArray();
//        for (char c : charArray) {
//            if (isEnglishLetter(c) && StrUtil.isLowerCase(String.valueOf(c))) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * 转义
     *
     * @author bokun.li
     * @date 2025/9/4
     */
    public String escapeCn(String name, Connection connection, boolean forceEscape) {
        DialectV2 dialectV2 = DialectFactory.get(connection);
        if(forceEscape){
            return dialectV2.forceEscape(name);
        }else{
            return dialectV2.escape(name);
        }
//        String databaseType = null;
//        lbk:
//        try {
//            databaseType = JdbcHelper.getDatabaseType(connection);
//            // 先大概检查一下肯定需要转义的名称 不考虑数据库保留字
//            // 如果强制转义那么也跳过
//            if (DBFieldEscapeChecker.needEscape(name) || forceEscape) {
//                break lbk;
//            }
//            // 这些数据库 只转义该转义的 其他不转义
//            if (DbType.ORACLE.getDb().equals(databaseType) && !containLower(name)) {
//                if (!ListTs.equalIgnoreCase(ORACLE_ESCAPE, name)) {
//                    return name;
//                }
//            } else if (DbType.POSTGRE_SQL.getDb().equals(databaseType) && !containUpper(name)) {
//                if (!ListTs.equalIgnoreCase(PG_ESCAPE, name)) {
//                    return name;
//                }
//            } else if (DbType.MYSQL.getDb().equals(databaseType)) {
//                if (!ListTs.equalIgnoreCase(MYSQL_ESCAPE, name)) {
//                    return name;
//                }
//            } else if (DbType.H2.getDb().equals(databaseType) && !containLower(name)) {
//                if (!ListTs.equalIgnoreCase(H2_ESCAPE, name)) {
//                    return name;
//                }
//            }else if (DbType.SQL_SERVER.getDb().equals(databaseType)) {
//                if (!ListTs.equalIgnoreCase(MSSQL_ESCAPE, name)) {
//                    return name;
//                }
//            }
//        } catch (SQLException e) {
//            throw JdbcHelper.translateSqlException("escapeCn", null, e);
//        }
//        Wrapper wrapper = dbVsWrapper.getOrDefault(databaseType, new Wrapper());
//
//        return wrapper.wrap(name);
    }

    /**
     * 拆分并转义
     *
     * @author bokun.li
     * @date 2025/9/4
     */
    public String splitStrAndEscape(String str, String comma, Connection connection, boolean forceEscape) {
        if (StrUtil.isBlank(str) || StrUtil.isBlank(comma)) return str;
        String s = RegexEscapeUtils.escapeRegex(comma);
        String[] split = str.split(s);
        List<String> list = ListTs.asList(split);
        return list.stream().map(e -> escapeCn(e, connection, forceEscape)).collect(Collectors.joining(comma));
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
     * @see easy4j.infra.dbaccess.dialect.v2.DialectV2
     */
    @Deprecated
    public Class<?> getJavaClassByTypeNameAndDbType(String typeName, String dbType) {
        if (StrUtil.equalsIgnoreCase(DbType.MYSQL.getDb(), dbType)) {
            return Optional.ofNullable(MySQLFieldType.getFromDataType(typeName)).map(MySQLFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
        } else if (StrUtil.equalsIgnoreCase(DbType.POSTGRE_SQL.getDb(), dbType)) {
            return Optional.ofNullable(PgSQLFieldType.getFromDataType(typeName)).map(PgSQLFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
        } else if (StrUtil.equalsIgnoreCase(DbType.ORACLE.getDb(), dbType)) {
            return Optional.ofNullable(OracleFieldType.getFromDataType(typeName)).map(OracleFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
        } else if (StrUtil.equalsIgnoreCase(DbType.H2.getDb(), dbType)) {
            return Optional.ofNullable(H2SqlFieldType.getFromDataType(typeName)).map(H2SqlFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
        }else if (StrUtil.equalsIgnoreCase(DbType.SQL_SERVER.getDb(), dbType)) {
            return Optional.ofNullable(SqlServerFieldType.getFromDataType(typeName)).map(SqlServerFieldType::getJavaTypes).map(e -> ListTs.get(e, 0)).orElse(null);
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
     * @see easy4j.infra.dbaccess.dialect.v2.DialectV2#isJson
     */
    @Deprecated
    public boolean isJson(String typeName, String dbType) {
        if (StrUtil.equalsIgnoreCase(DbType.MYSQL.getDb(), dbType)) {
            return MySQLFieldType.getFromDataType(typeName) == MySQLFieldType.JSON;
        } else if (StrUtil.equalsIgnoreCase(DbType.POSTGRE_SQL.getDb(), dbType)) {
            PgSQLFieldType fromDataType = PgSQLFieldType.getFromDataType(typeName);
            return fromDataType == PgSQLFieldType.JSON || fromDataType == PgSQLFieldType.JSONB;
        } else if (StrUtil.equalsIgnoreCase(DbType.H2.getDb(), dbType)) {
            H2SqlFieldType fromDataType = H2SqlFieldType.getFromDataType(typeName);
            return fromDataType == H2SqlFieldType.JSON;
        } /*else if (StrUtil.equalsIgnoreCase(DbType.SQL_SERVER.getDb(), dbType)) {
            SqlServerFieldType fromDataType = SqlServerFieldType.getFromDataType(typeName);
            return fromDataType == SqlServerFieldType.JSON;
        }*/
        return false;
    }

    /**
     * 通过不同数据库来判断当前字段类型是否是 lob(大文本)类型
     *
     * @param typeName
     * @param dbType
     * @return
     * @see easy4j.infra.dbaccess.dialect.v2.DialectV2#isLob
     */
    @Deprecated
    public boolean isLob(String typeName, String dbType) {
        if (StrUtil.equalsIgnoreCase(DbType.MYSQL.getDb(), dbType)) {
            return MySQLFieldType.getFromDataType(typeName) == MySQLFieldType.LONGTEXT;

        } else if (StrUtil.equalsIgnoreCase(DbType.POSTGRE_SQL.getDb(), dbType)) {
            PgSQLFieldType fromDataType = PgSQLFieldType.getFromDataType(typeName);
            return fromDataType == PgSQLFieldType.TEXT;
        } else if (StrUtil.equalsIgnoreCase(DbType.ORACLE.getDb(), dbType)) {
            OracleFieldType fromDataType1 = OracleFieldType.getFromDataType(typeName);
            return fromDataType1 == OracleFieldType.CLOB;
        } else if (StrUtil.equalsIgnoreCase(DbType.H2.getDb(), dbType)) {
            H2SqlFieldType fromDataType1 = H2SqlFieldType.getFromDataType(typeName);
            return fromDataType1 == H2SqlFieldType.CLOB || fromDataType1 == H2SqlFieldType.TEXT;
        }else if (StrUtil.equalsIgnoreCase(DbType.SQL_SERVER.getDb(), dbType)) {
            SqlServerFieldType fromDataType1 = SqlServerFieldType.getFromDataType(typeName);
            return fromDataType1 == SqlServerFieldType.VARCHAR_MAX || fromDataType1 == SqlServerFieldType.NVARCHAR_MAX || fromDataType1 == SqlServerFieldType.TEXT || fromDataType1 == SqlServerFieldType.NTEXT;
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
        String s1 = RandomUtil.randomString(4);
        s += s1;
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
