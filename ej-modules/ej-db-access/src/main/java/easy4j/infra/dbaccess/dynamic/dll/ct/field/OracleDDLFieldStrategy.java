package easy4j.infra.dbaccess.dynamic.dll.ct.field;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLConfig;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.OracleFieldType;
import easy4j.infra.dbaccess.dynamic.dll.OracleFieldType;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * PgDDLFieldStrategy
 * postgresql 相关
 *
 * @author bokun.li
 * @date 2025/8/19
 */
public class OracleDDLFieldStrategy extends AbstractIDDLFieldStrategy {

    @Override
    public boolean match(DDLFieldInfo ddlFieldInfo) {
        String dbType = ddlFieldInfo.getDbType();
        return StrUtil.equals("oracle", dbType);
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
        List<String> objects = ListTs.newList();
        // 解析字段名称
        parseFieldName(objects, ddlFieldInfo, ddlConfig);
        // 解析字段类型，递增特殊处理
        OracleFieldType oracleFieldType = parseDataType(objects, ddlFieldInfo, ddlConfig);
        // 解析非空和默认值
        parseNotNullDefault(oracleFieldType, objects, ddlFieldInfo, ddlConfig);

        // 生成额外约束
        // unique check
        if (ddlFieldInfo.isGenConstraint()) {

            if (ddlFieldInfo.isUnique()) {
                objects.add("unique");
            }
            String check = ddlFieldInfo.getCheck();
            if (StrUtil.isNotBlank(check)) {
                objects.add("check (" + check + ")");
            }
            String[] constraint = ddlFieldInfo.getConstraint();
            if (constraint != null) {
                Collections.addAll(objects, constraint);
            }
        }
        return String.join(SP.SPACE, objects);
    }

    protected void parseNotNullDefault(OracleFieldType oracleFieldType, List<String> objects, DDLFieldInfo ddlFieldInfo, DDLConfig ddlConfig) {

        String def = ddlFieldInfo.getDef();
        int defInt = ddlFieldInfo.getDefNum();
        boolean defTime = ddlFieldInfo.isDefTime();
        if (StrUtil.isNotBlank(def)) {
            objects.add("default " + ddlConfig.wrapSingleQuote(ddlConfig.getTxt(def)));
        } else if (defInt != -1) {
            objects.add("default " + defInt);
        } else if (defTime) {
            objects.add("default " + getCurrentTimeFunc(oracleFieldType));
        }

        if (ddlFieldInfo.isNotNull()) {
            objects.add(ddlConfig.getTxt("NOT NULL"));
        }
    }

    protected String getCurrentTimeFunc(OracleFieldType oracleFieldType) {
        String currentTime = null;
        if (oracleFieldType == OracleFieldType.TIMESTAMP || oracleFieldType == OracleFieldType.TIMESTAMP_TZ || oracleFieldType == OracleFieldType.TIMESTAMP_LTZ) {
            currentTime = "current_timestamp";
        } else if (oracleFieldType == OracleFieldType.DATE) {
            currentTime = "current_date";
        }
        return currentTime;
    }


    private OracleFieldType parseDataType(List<String> objects, DDLFieldInfo ddlFieldInfo, DDLConfig ddlConfig) {
        String fieldType = ddlFieldInfo.getDataType();
        OracleFieldType fromDataType = OracleFieldType.getFromDataType(fieldType);
        if (null == fromDataType) {
            if (ddlFieldInfo.isJson()) {
                fromDataType = OracleFieldType.CLOB;
            } else if (ddlFieldInfo.isLob()) {
                fromDataType = OracleFieldType.CLOB;
            } else {
                Class<?> fieldClass = ddlFieldInfo.getFieldClass();
                if (null != fieldClass) {
                    fromDataType = OracleFieldType.getByClass(fieldClass);
                    // auto increment
                    if (ddlFieldInfo.isAutoIncrement()) {
                        if (ddlConfig.checkSupportVersion(ddlFieldInfo.getDbVersion(), 12)) {
                            if (fieldClass == int.class || fieldClass == Integer.class) {
                                fromDataType = OracleFieldType.IDENTITY_INT;
                            } else if (fieldClass == long.class || fieldClass == Long.class) {
                                fromDataType = OracleFieldType.IDENTITY_LONG;
                            }
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

    public String getDataTypeByPGFieldType(OracleFieldType oracleFieldType, DDLFieldInfo ddlFieldInfo) {
        String fieldTypeTemplate = StrUtil.blankToDefault(oracleFieldType.getFieldTypeTemplate(), oracleFieldType.getFieldType());
        int dataLength = ddlFieldInfo.getDataLength();
        int dataDecimal = ddlFieldInfo.getDataDecimal();
        String[] dataTypeAttr = ddlFieldInfo.getDataTypeAttr();
        String dataTypeFormat;
        boolean isArray = false;
        if (null != dataTypeAttr && dataTypeAttr.length > 0 && false) {
            if (isArray) {
                String collect = Arrays.stream(dataTypeAttr).map(e -> StrUtil.wrap(e, "'", "'")).collect(Collectors.joining(SP.COMMA));
                CheckUtils.checkTrue(StrUtil.isBlank(collect), "the type " + oracleFieldType.getFieldType() + " need set dataTypeAttr，please check!");
                dataTypeFormat = MessageFormat.format(fieldTypeTemplate, collect);
            } else {
                dataTypeFormat = MessageFormat.format(fieldTypeTemplate, ListTs.objectToListObject(dataTypeAttr, Function.identity()).toArray(new Object[]{}));
            }
        } else {
            // string
            if ((oracleFieldType == OracleFieldType.VARCHAR2 || oracleFieldType == OracleFieldType.CHAR || oracleFieldType == OracleFieldType.CLOB || oracleFieldType == OracleFieldType.LONG) && dataLength <= 0) {
                dataLength = getStrDefaultLength();
            }
            if (oracleFieldType == OracleFieldType.DECIMAL || oracleFieldType == OracleFieldType.NUMERIC || oracleFieldType == OracleFieldType.NUMBER_DECIMAL) {
                dataLength = dataLength <= 0 ? getNumLengthDefaultLength() : dataLength;
                dataDecimal = dataDecimal <= 0 ? getNumDecimalDefaultLength() : dataDecimal;
            }
            CheckUtils.checkTrue(
                    StrUtil.isNotBlank(oracleFieldType.getFieldTypeTemplate()) && dataLength <= 0,
                    "the type " + oracleFieldType.getFieldType() + " need set dataLength，please check!"
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
