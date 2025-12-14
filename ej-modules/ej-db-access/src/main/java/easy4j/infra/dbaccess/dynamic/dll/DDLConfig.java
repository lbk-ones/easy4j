package easy4j.infra.dbaccess.dynamic.dll;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.sql.DataSource;

import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Accessors(chain = true)
@Deprecated
public class DDLConfig {

    private String dbType;
    private String dbVersion;
    // mysql拿catalog当schema 其他数据库不一定是这个
    private String connectionCatalog;
    // 这个不一定有值 看驱动实现
    private String connectionSchema;
    private List<DynamicColumn> dbColumns;

    @Desc("需要新增的列")
    private List<DDLFieldInfo> adColumns;

    private String schema;
    private String tableName;
    private DataSource dataSource;
    private Connection connection;
    private Dialect dialect;
    private boolean toUnderLine = true;
    private boolean toLowCase = true;
    private boolean toUpperCase;
    private DDLTableInfo ddlTableInfo;
    // parse java 才有这个字段
    private Class<?> domainClass;

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
}
