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
package easy4j.infra.dbaccess.dynamic.dll.op.impl.cc;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.SqlServerFieldType;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SqlServerOpColumnConstraints
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public class SqlServerOpColumnConstraints extends AbstractOpColumnConstraints {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.SQL_SERVER.getDb().equals(dbType);
    }

    /**
     * 重载 模板参数 赋值
     *
     * @param ddlFieldInfo
     * @return
     */
    @Override
    public Map<String, String> getTemplateParams(DDLFieldInfo ddlFieldInfo) {
        OpConfig opConfig = this.getOpContext().getOpConfig();
        Map<String, String> templateParams = super.getTemplateParams(ddlFieldInfo);
        Class<?> fieldClass = ddlFieldInfo.getFieldClass();
        CheckUtils.notNull(fieldClass, "fieldClass");

        if (ddlFieldInfo.isAutoIncrement() && opConfig.isNumberDefaultType(fieldClass) && ddlFieldInfo.isPrimary()) {
            templateParams.put(AUTO_INCREMENT, "IDENTITY");
        }
        //  BLOB, TEXT, GEOMETRY or JSON  can't have a default value
        //  POINT、LINESTRING、POLYGON
        //  PRIMARY KEY always can't have a default value
        SqlServerFieldType oracleFieldType = getMsSqlFieldType(fieldClass, ddlFieldInfo);
        if (ListTs.asList(SqlServerFieldType.ROWVERSION, SqlServerFieldType.TIMESTAMP, SqlServerFieldType.GEOGRAPHY, SqlServerFieldType.GEOMETRY).contains(oracleFieldType) || ddlFieldInfo.isPrimary()) {
            templateParams.remove(DEFAULT);
        } else if (opConfig.isDateDefaultType(fieldClass)) {
            if (ddlFieldInfo.isDefTime()) {
                String currentTimeFunc = getCurrentTimeFunctionName(oracleFieldType);
                templateParams.put(DEFAULT, "default " + currentTimeFunc);
            } else {
                templateParams.remove(DEFAULT);
            }
        }

        return templateParams;
    }

    public String getCurrentTimeFunctionName(SqlServerFieldType sqlServerFieldType) {

        // 初始化映射关系，根据枚举类型匹配最佳默认函数
        Map<SqlServerFieldType, String> DEFAULT_SQL_MAP = new HashMap<>();
        DEFAULT_SQL_MAP.put(SqlServerFieldType.DATETIME, "GETDATE()");
        DEFAULT_SQL_MAP.put(SqlServerFieldType.DATETIME2, "SYSDATETIME()");
        DEFAULT_SQL_MAP.put(SqlServerFieldType.SMALLDATETIME, "GETDATE()");
        DEFAULT_SQL_MAP.put(SqlServerFieldType.DATE, "CAST(SYSDATETIME() AS DATE)");
        DEFAULT_SQL_MAP.put(SqlServerFieldType.TIME, "CAST(SYSDATETIME() AS TIME)");
        DEFAULT_SQL_MAP.put(SqlServerFieldType.DATETIMEOFFSET, "SYSDATETIMEOFFSET()");
        List<SqlServerFieldType> dateTimeList = ListTs.asList(
                SqlServerFieldType.DATETIME,
                SqlServerFieldType.DATETIME2,
                SqlServerFieldType.SMALLDATETIME,
                SqlServerFieldType.DATE,
                SqlServerFieldType.TIME,
                SqlServerFieldType.DATETIMEOFFSET
        );
        if (dateTimeList.contains(sqlServerFieldType)) {
            return DEFAULT_SQL_MAP.get(sqlServerFieldType);
        }
        return null;
    }


    @Override
    public String getDataType(DDLFieldInfo ddlFieldInfo) {
        Class<?> fieldClass = ddlFieldInfo.getFieldClass();
        if (null == fieldClass) {
            String s = ddlFieldInfo.getSchema() + SP.DOT + ddlFieldInfo.getTableName() + SP.DOT + ddlFieldInfo.getName() + SP.DOT + ddlFieldInfo.getDataType();
            throw new IllegalArgumentException(s);
        }
        SqlServerFieldType sqlServerFieldType = getMsSqlFieldType(fieldClass, ddlFieldInfo);
        CheckUtils.notNullMsg(sqlServerFieldType, fieldClass.getName() + "【" + ddlFieldInfo.getDataType() + "】" + ":can not select db field type!");
        return getDataTypeBySqlServerFieldType(sqlServerFieldType, ddlFieldInfo);
    }

    public String getDataTypeBySqlServerFieldType(SqlServerFieldType sqlServerFieldType, DDLFieldInfo ddlFieldInfo) {
        OpConfig opConfig = this.getOpContext().getOpConfig();
        String fieldTypeTemplate = StrUtil.blankToDefault(sqlServerFieldType.getFieldTypeTemplate(), sqlServerFieldType.getFieldType());
        int dataLength = ddlFieldInfo.getDataLength();
        int dataDecimal = ddlFieldInfo.getDataDecimal();
        //String[] dataTypeAttr = ddlFieldInfo.getDataTypeAttr();
        String dataTypeFormat;
        {
            List<SqlServerFieldType> list = ListTs.asList(SqlServerFieldType.NVARCHAR, SqlServerFieldType.VARCHAR, SqlServerFieldType.VARBINARY_MAX, SqlServerFieldType.CHAR, SqlServerFieldType.NVARCHAR_MAX, SqlServerFieldType.NCHAR, SqlServerFieldType.TEXT, SqlServerFieldType.NTEXT, SqlServerFieldType.SYSNAME);
            boolean isStr = list.contains(sqlServerFieldType);
            if (isStr && dataLength <= 0) {
                dataLength = opConfig.getStrDefaultLength();
            }
            if (sqlServerFieldType == SqlServerFieldType.DECIMAL || sqlServerFieldType == SqlServerFieldType.NUMERIC) {
                dataLength = dataLength <= 0 ? opConfig.getNumLengthDefaultLength() : dataLength;
                dataDecimal = dataDecimal <= 0 ? opConfig.getNumDecimalDefaultLength() : dataDecimal;
                if (dataLength < dataDecimal) {
                    dataDecimal = 0;
                }
            }
            if (sqlServerFieldType == SqlServerFieldType.BIT) {
                dataLength = dataLength <= 0 ? 1 : dataLength;
            }
            if (sqlServerFieldType == SqlServerFieldType.DATETIME2) {
                dataLength = dataLength <= 0 ? 3 : dataLength;
                if (dataLength != 3 && dataLength != 7) {
                    dataLength = 3;
                }
            }
            if (sqlServerFieldType == SqlServerFieldType.TIME) {
                if (dataLength != 3 && dataLength != 7) {
                    dataLength = 0;
                }
            }
            if (sqlServerFieldType == SqlServerFieldType.FLOAT) {
                /**
                 当精度 n 为 1~24 时（单精度）：范围约为 ±1.175494351×10⁻³⁸ 至 ±3.402823466×10³⁸    ----> java float
                 当精度 n 为 25~53 时（双精度）：范围约为 ±2.2250738585072014×10⁻³⁰⁸ 至             ----> java double
                 */
                dataLength = Math.min(dataLength <= 0 ? 53 : dataLength, 53);
            }
            /**
             TIME的表格
             n（小数秒位数）	时间精度	存储大小（字节）	示例值
             0	秒	3	14:30:00
             3	毫秒（1/1000 秒）	4	14:30:00.123
             7	100 纳秒	5	14:30:00.1234567
             */
            CheckUtils.checkTrue(
                    StrUtil.isNotBlank(sqlServerFieldType.getFieldTypeTemplate()) && sqlServerFieldType != SqlServerFieldType.TIME && dataLength <= 0,
                    "the type " + sqlServerFieldType.getFieldType() + " need set dataLength，please check!"
            );
            if (sqlServerFieldType == SqlServerFieldType.VARCHAR) {
                dataLength = Math.min(dataLength, 8000);
            } else if (sqlServerFieldType == SqlServerFieldType.NVARCHAR) {
                dataLength = Math.min(dataLength, 4000);
            }
            if (isStr && dataLength == Integer.MAX_VALUE) {
                dataTypeFormat = SqlServerFieldType.NVARCHAR_MAX.getFieldType();
                ddlFieldInfo.setDataType(dataTypeFormat);
            } else if (dataLength > 8000 && (sqlServerFieldType == SqlServerFieldType.BINARY || sqlServerFieldType == SqlServerFieldType.VARBINARY)) {
                String fieldTypeTemplate1 = SqlServerFieldType.VARBINARY.getFieldTypeTemplate();
                return MessageFormat.format(fieldTypeTemplate1, String.valueOf(8000));
            } else {
                dataTypeFormat = MessageFormat.format(fieldTypeTemplate, String.valueOf(dataLength), String.valueOf(dataDecimal));
            }

        }
        return dataTypeFormat;
    }


    public SqlServerFieldType getMsSqlFieldType(Class<?> fieldClass, DDLFieldInfo ddlFieldInfo) {
        String dataType = ddlFieldInfo.getDataType();
        if (ddlFieldInfo.isJson()) {
            return SqlServerFieldType.NVARCHAR_MAX;
        }
        if (ddlFieldInfo.isLob()) {
            return SqlServerFieldType.NVARCHAR_MAX;
        }
        SqlServerFieldType sqlServerFieldType;
        if (dataType != null) {
            sqlServerFieldType = SqlServerFieldType.getFromDataType(dataType);
            if (null == sqlServerFieldType) {
                sqlServerFieldType = SqlServerFieldType.getByClass(fieldClass);
            }
        } else {
            sqlServerFieldType = SqlServerFieldType.getByClass(fieldClass);
        }
        return sqlServerFieldType;
    }
}
