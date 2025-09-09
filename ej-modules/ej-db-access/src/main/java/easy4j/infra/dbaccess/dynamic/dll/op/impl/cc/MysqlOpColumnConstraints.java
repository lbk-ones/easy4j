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
import easy4j.infra.dbaccess.dynamic.dll.MySQLFieldType;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 解析mysql特有的列约束，以及字段类型和字段属性
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public class MysqlOpColumnConstraints extends AbstractOpColumnConstraints {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.MYSQL.getDb().equals(dbType);
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
            templateParams.put(AUTO_INCREMENT, "auto_increment");
        }

        String comment = ddlFieldInfo.getComment();
        if (StrUtil.isNotBlank(comment)) {
            if (!StrUtil.isWrap(comment, SP.SINGLE_QUOTE)) {
                templateParams.put(COMMENTS, "comment " + StrUtil.wrap(comment, SP.SINGLE_QUOTE, SP.SINGLE_QUOTE));
            } else {
                templateParams.put(COMMENTS, "comment " + comment);
            }
        }

        //  BLOB, TEXT, GEOMETRY or JSON  can't have a default value
        //  POINT、LINESTRING、POLYGON
        //  PRIMARY KEY always can't have a default value
        MySQLFieldType oracleFieldType = getMysqlFieldType(fieldClass, ddlFieldInfo);
        if (ListTs.asList(MySQLFieldType.BLOB, MySQLFieldType.TEXT, MySQLFieldType.LONGTEXT, MySQLFieldType.JSON).contains(oracleFieldType) || ddlFieldInfo.isPrimary()) {
            templateParams.remove(DEFAULT);
        } else if (opConfig.isDateDefaultType(fieldClass)) {
            if (ddlFieldInfo.isDefTime()) {
                String currentTimeFunc = getCurrentTimeFunctionName(oracleFieldType);
                templateParams.put(DEFAULT, "default " + currentTimeFunc);
            } else {
                templateParams.remove(DEFAULT);
            }
        }

        // generated always as (expr)
        // mysql default virtual model
        String generatedAlwaysAs = ddlFieldInfo.getGeneratedAlwaysAs();
        if (StrUtil.isNotBlank(generatedAlwaysAs)) {
            String generatedAlwaysAsModel = StrUtil.blankToDefault(ddlFieldInfo.getGeneratedAlwaysAsModel(), "");
            generatedAlwaysAsModel = StrUtil.isNotBlank(generatedAlwaysAsModel) ? SP.SPACE + generatedAlwaysAsModel : generatedAlwaysAsModel;
            if (ddlFieldInfo.isGeneratedAlwaysAsNotNull()) {
                generatedAlwaysAsModel += " not null";
            }
            templateParams.put(BEFORE, "generated always as (" + generatedAlwaysAs + ")" + generatedAlwaysAsModel);
        }


        return templateParams;
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


    @Override
    public String getDataType(DDLFieldInfo ddlFieldInfo) {
        Class<?> fieldClass = ddlFieldInfo.getFieldClass();
        MySQLFieldType mysqlFieldType = getMysqlFieldType(fieldClass, ddlFieldInfo);
        CheckUtils.notNullMsg(mysqlFieldType, fieldClass.getName() + ":can not select db field type!");
        return getDataTypeByMySQLFieldType(mysqlFieldType, ddlFieldInfo);
    }

    public String getDataTypeByMySQLFieldType(MySQLFieldType mysqlFieldType, DDLFieldInfo ddlFieldInfo) {
        OpConfig opConfig = this.getOpContext().getOpConfig();
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
                dataLength = opConfig.getStrDefaultLength();
            }
            if (mysqlFieldType == MySQLFieldType.DECIMAL) {
                dataLength = dataLength <= 0 ? opConfig.getNumLengthDefaultLength() : dataLength;
                dataDecimal = dataDecimal <= 0 ? opConfig.getNumDecimalDefaultLength() : dataDecimal;
                if (dataLength < dataDecimal) {
                    dataDecimal = 0;
                }
            }
            if (mysqlFieldType == MySQLFieldType.BIT) {
                dataLength = dataLength <= 0 ? 1 : dataLength;
            }
            CheckUtils.checkTrue(
                    StrUtil.isNotBlank(mysqlFieldType.getFieldTypeTemplate()) && dataLength <= 0,
                    "the type " + mysqlFieldType.getFieldType() + " need set dataLength，please check!"
            );
            if (dataLength == Integer.MAX_VALUE) {
                dataTypeFormat = MySQLFieldType.LONGTEXT.getFieldType();
            } else {
                dataTypeFormat = MessageFormat.format(fieldTypeTemplate, String.valueOf(dataLength), String.valueOf(dataDecimal));
            }

        }
        return dataTypeFormat;
    }


    public MySQLFieldType getMysqlFieldType(Class<?> fieldClass, DDLFieldInfo ddlFieldInfo) {
        String dataType = ddlFieldInfo.getDataType();
        if (ddlFieldInfo.isJson()) {
            return MySQLFieldType.JSON;
        }
        if (ddlFieldInfo.isLob()) {
            return MySQLFieldType.LONGTEXT;
        }
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
