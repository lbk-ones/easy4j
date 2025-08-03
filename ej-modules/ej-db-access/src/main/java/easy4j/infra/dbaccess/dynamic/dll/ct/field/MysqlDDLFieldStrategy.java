package easy4j.infra.dbaccess.dynamic.dll.ct.field;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLConfig;
import easy4j.infra.dbaccess.dynamic.dll.MySQLFieldType;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MysqlDDLFieldStrategy extends AbstractIDDLFieldStrategy {
    @Override
    public boolean match(DDLFieldInfo ddlFieldInfo) {
        String dbType = ddlFieldInfo.getDbType();
        return StrUtil.equals("mysql", dbType);
    }

    @Override
    public String getResColumn(DDLFieldInfo ddlFieldInfo) {
        DDLConfig dllConfig = ddlFieldInfo.getDllConfig();
        List<String> objects = ListTs.newLinkedList();
        // field name
        String name = ddlFieldInfo.getName();
        objects.add(dllConfig.getColumnName(name));

        // data type
        Class<?> fieldClass = ddlFieldInfo.getFieldClass();
        CheckUtils.notNull(fieldClass, "fieldClass");
        MySQLFieldType mysqlFieldType = getMysqlFieldType(fieldClass, ddlFieldInfo);
        CheckUtils.notNullMsg(mysqlFieldType, fieldClass.getName() + ":can not select db field type!");
        String dataType = getDataTypeByMySQLFieldType(mysqlFieldType, ddlFieldInfo);
        objects.add(dllConfig.getTxt(dataType));

        // not null
        if (ddlFieldInfo.isNotNull()) {
            objects.add(dllConfig.getTxt("NOT NULL"));
        }

        // default
        String currentTimeFunction = getCurrentTimeFunctionName(mysqlFieldType);
        if (ddlFieldInfo.isDefTime() && null != currentTimeFunction) {
            objects.add(dllConfig.getTxt(currentTimeFunction));
        } else if (StrUtil.isNotBlank(ddlFieldInfo.getDef())) {
            objects.add(dllConfig.getTxt(ddlFieldInfo.getDef()));
        }

        // primary key or unique
        /*if (ddlFieldInfo.isPrimary()) {
            objects.add(getTxt("PRIMARY KEY"));
        } else {
            objects.add(getTxt("UNIQUE"));
        }*/

        // comment
        if (StrUtil.isNotBlank(ddlFieldInfo.getComment())) {
            objects.add(dllConfig.getTxt("comment " + dllConfig.wrapQuote(ddlFieldInfo.getComment())));
        }
        return String.join(SP.SPACE, objects);
    }


    public String getCurrentTimeFunctionName(MySQLFieldType mysqlFieldType) {
        MySQLFieldType datetime = MySQLFieldType.DATETIME;
        MySQLFieldType timestamp = MySQLFieldType.TIMESTAMP;
        if (datetime == mysqlFieldType || timestamp == mysqlFieldType) return "current_timestamp";
        MySQLFieldType date = MySQLFieldType.DATE;
        if (date == mysqlFieldType) return "current_date";
        MySQLFieldType time = MySQLFieldType.TIME;
        if (time == mysqlFieldType) return "current_time";
        return null;
    }

    public String getDataTypeByMySQLFieldType(MySQLFieldType mysqlFieldType, DDLFieldInfo ddlFieldInfo) {
        String fieldTypeTemplate = StrUtil.blankToDefault(mysqlFieldType.getFieldTypeTemplate(), mysqlFieldType.getFieldType());
        int dataLength = ddlFieldInfo.getDataLength();
        int dataDecimal = ddlFieldInfo.getDataDecimal();
        String[] dataTypeAttr = ddlFieldInfo.getDataTypeAttr();
        String dataTypeFormat;
        boolean isArray = MySQLFieldType.ENUM == mysqlFieldType || MySQLFieldType.SET == mysqlFieldType;
        if (null != dataTypeAttr && dataTypeAttr.length > 0) {
            if (isArray) {
                String collect = Arrays.stream(dataTypeAttr).map(e -> StrUtil.wrap(e, "'", "'")).collect(Collectors.joining(SP.COMMA));
                CheckUtils.checkTrue(StrUtil.isBlank(collect), "the type " + mysqlFieldType.getFieldType() + " need set dataTypeAttr，please check!");
                dataTypeFormat = MessageFormat.format(fieldTypeTemplate, collect);
            } else {
                dataTypeFormat = MessageFormat.format(fieldTypeTemplate, ListTs.objectToListObject(dataTypeAttr, Function.identity()).toArray(new Object[]{}));
            }
        } else {
            if ((mysqlFieldType == MySQLFieldType.VARCHAR || mysqlFieldType == MySQLFieldType.CHAR) && dataLength <= 0) {
                dataLength = getStrDefaultLength();
            }
            if (mysqlFieldType == MySQLFieldType.DECIMAL) {
                dataLength = dataLength <= 0 ? getNumLengthDefaultLength() : dataLength;
                dataDecimal = dataDecimal <= 0 ? getNumDecimalDefaultLength() : dataDecimal;
            }
            if (mysqlFieldType == MySQLFieldType.BIT) {
                dataLength = dataLength <= 0 ? 1 : dataLength;
            }
            CheckUtils.checkTrue(
                    StrUtil.isNotBlank(mysqlFieldType.getFieldTypeTemplate()) && dataLength <= 0,
                    "the type " + mysqlFieldType.getFieldType() + " need set dataLength，please check!"
            );
            dataTypeFormat = MessageFormat.format(fieldTypeTemplate, dataLength, dataDecimal);
        }
        return dataTypeFormat;
    }


    public MySQLFieldType getMysqlFieldType(Class<?> fieldClass, DDLFieldInfo ddlFieldInfo) {
        String dataType = ddlFieldInfo.getDataType();
        MySQLFieldType mySQLFieldType;
        if (dataType != null) {
            mySQLFieldType = MySQLFieldType.getFromDataType(dataType);
            if (null == mySQLFieldType) {
                mySQLFieldType = MySQLFieldType.getByClass(fieldClass);
            }
        } else {
            mySQLFieldType = MySQLFieldType.getByClass(fieldClass);
        }
        return mySQLFieldType;
    }
}
