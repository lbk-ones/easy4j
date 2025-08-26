package easy4j.infra.dbaccess.dynamic.dll.ct.field;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLConfig;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.PgSQLFieldType;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PgDDLFieldStrategy
 * postgresql 相关
 *
 * @author bokun.li
 * @date 2025/8/19
 */
public class PgDDLFieldStrategy extends AbstractIDDLFieldStrategy {

    @Override
    public boolean match(DDLFieldInfo ddlFieldInfo) {
        String dbType = ddlFieldInfo.getDbType();
        return StrUtil.equals(DbType.POSTGRE_SQL.getDb(), dbType);
    }

    /**
     * 除了字段信息外，不解析索引相关信息，只解析约束，比如主键，唯一，check校验等
     *
     * @author bokun.li
     * @date 2025/8/19
     */
    @Override
    public String getResColumn(DDLFieldInfo ddlFieldInfo) {
        DDLConfig ddlConfig = ddlFieldInfo.getDllConfig();
        CheckUtils.checkByLambda(ddlFieldInfo, DDLFieldInfo::getDllConfig);
        CheckUtils.checkByLambda(ddlFieldInfo, DDLFieldInfo::getFieldClass);
        List<String> objects = ListTs.newList();
        // 解析字段名称
        parseFieldName(objects, ddlFieldInfo, ddlConfig);
        // 解析字段类型，递增特殊处理
        PgSQLFieldType pgSQLFieldType = parseDataType(objects, ddlFieldInfo, ddlConfig);
        // 解析非空和默认值
        parseNotNullDefault(pgSQLFieldType, objects, ddlFieldInfo, ddlConfig);

        // 生成额外约束
        // unique check
        genConstraint(ddlFieldInfo, objects);
        return String.join(SP.SPACE, objects);
    }

    protected void parseNotNullDefault(PgSQLFieldType pgSQLFieldType, List<String> objects, DDLFieldInfo ddlFieldInfo, DDLConfig ddlConfig) {
        if (ddlFieldInfo.isNotNull()) {
            objects.add(ddlConfig.getTxt("NOT NULL"));
        }

        String def = ddlFieldInfo.getDef();
        int defInt = ddlFieldInfo.getDefNum();
        boolean defTime = ddlFieldInfo.isDefTime();
        if (StrUtil.isNotBlank(def)) {
            Class<?> fieldClass = ddlFieldInfo.getFieldClass();
            if (isNumberDefaultType(fieldClass)) {
                objects.add("default " + def);
            } else {
                objects.add("default " + ddlConfig.wrapSingleQuote(def));
            }
        } else if (defInt != -1) {
            objects.add("default " + defInt);
        } else if (defTime) {
            objects.add("default " + getCurrentTimeFunc(pgSQLFieldType));
        }
    }

    protected String getCurrentTimeFunc(PgSQLFieldType pgSQLFieldType) {
        String currentTime = null;
        if (pgSQLFieldType == PgSQLFieldType.TIMESTAMP || pgSQLFieldType == PgSQLFieldType.TIMESTAMPTZ) {
            currentTime = "current_timestamp";
        } else if (pgSQLFieldType == PgSQLFieldType.DATE) {
            currentTime = "current_date";
        } else if (pgSQLFieldType == PgSQLFieldType.TIME || pgSQLFieldType == PgSQLFieldType.TIMETZ) {
            currentTime = "current_time";
        }
        return currentTime;
    }

    private PgSQLFieldType parseDataType(List<String> objects, DDLFieldInfo ddlFieldInfo, DDLConfig ddlConfig) {
        String fieldType = ddlFieldInfo.getDataType();
        PgSQLFieldType fromDataType = PgSQLFieldType.getFromDataType(fieldType);
        if (null == fromDataType) {
            if (ddlFieldInfo.isJson()) {
                fromDataType = PgSQLFieldType.JSONB;
            } else if (ddlFieldInfo.isLob()) {
                fromDataType = PgSQLFieldType.TEXT;
            } else {
                Class<?> fieldClass = ddlFieldInfo.getFieldClass();
                if (null != fieldClass) {
                    fromDataType = PgSQLFieldType.getByClass(fieldClass);
                    // auto increment
                    if (ddlFieldInfo.isAutoIncrement()) {
                        if (fieldClass == int.class || fieldClass == Integer.class) {
                            fromDataType = PgSQLFieldType.SERIAL;
                        } else if (fieldClass == long.class || fieldClass == Long.class) {
                            fromDataType = PgSQLFieldType.BIGSERIAL;
                        } else if (fieldClass == short.class || fieldClass == Short.class) {
                            fromDataType = PgSQLFieldType.SMALLSERIAL;
                        }
                    }
                }
            }
        }
        if (null != fromDataType) {
            String dataTypeByPGFieldType = getDataTypeByPGFieldType(fromDataType, ddlFieldInfo);
            objects.add(dataTypeByPGFieldType);
        } else {
            throw new EasyException(ddlFieldInfo.getName() + " not select pgsql datatype please check!");
        }
        return fromDataType;

    }

    public String getDataTypeByPGFieldType(PgSQLFieldType pgsqlFieldType, DDLFieldInfo ddlFieldInfo) {
        String fieldTypeTemplate = StrUtil.blankToDefault(pgsqlFieldType.getFieldTypeTemplate(), pgsqlFieldType.getFieldType());
        int dataLength = ddlFieldInfo.getDataLength();
        int dataDecimal = ddlFieldInfo.getDataDecimal();
        String[] dataTypeAttr = ddlFieldInfo.getDataTypeAttr();
        String dataTypeFormat;
        boolean isArray = false;
        if (null != dataTypeAttr && dataTypeAttr.length > 0 && false) {
            if (isArray) {
                String collect = Arrays.stream(dataTypeAttr).map(e -> StrUtil.wrap(e, "'", "'")).collect(Collectors.joining(SP.COMMA));
                CheckUtils.checkTrue(StrUtil.isBlank(collect), "the type " + pgsqlFieldType.getFieldType() + " need set dataTypeAttr，please check!");
                dataTypeFormat = MessageFormat.format(fieldTypeTemplate, collect);
            } else {
                dataTypeFormat = MessageFormat.format(fieldTypeTemplate, ListTs.objectToListObject(dataTypeAttr, Function.identity()).toArray(new Object[]{}));
            }
        } else {
            // string
            if ((pgsqlFieldType == PgSQLFieldType.VARCHAR || pgsqlFieldType == PgSQLFieldType.CHAR || pgsqlFieldType == PgSQLFieldType.BPCHAR) && dataLength <= 0) {
                dataLength = getStrDefaultLength();
            }
            if (pgsqlFieldType == PgSQLFieldType.DECIMAL || pgsqlFieldType == PgSQLFieldType.NUMERIC) {
                dataLength = dataLength <= 0 ? getNumLengthDefaultLength() : dataLength;
                dataDecimal = dataDecimal <= 0 ? getNumDecimalDefaultLength() : dataDecimal;
            }
            if (
                    pgsqlFieldType == PgSQLFieldType.BIT ||
                            pgsqlFieldType == PgSQLFieldType.BIT_VARYING ||
                            pgsqlFieldType == PgSQLFieldType.VARBIT
            ) {
                dataLength = dataLength <= 0 ? 1 : dataLength;
            }
            CheckUtils.checkTrue(
                    StrUtil.isNotBlank(pgsqlFieldType.getFieldTypeTemplate()) && dataLength <= 0,
                    "the type " + pgsqlFieldType.getFieldType() + " need set dataLength，please check!"
            );
            dataTypeFormat = MessageFormat.format(fieldTypeTemplate, dataLength, dataDecimal);
        }
        return dataTypeFormat;
    }

    private void parseFieldName(List<String> objects, DDLFieldInfo ddlFieldInfo, DDLConfig ddlConfig) {
        String name = ddlFieldInfo.getName();
        String columnName = ddlConfig.getColumnName(name);
        objects.add(columnName);
    }

}
