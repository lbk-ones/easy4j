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
import easy4j.infra.dbaccess.dynamic.dll.H2SqlFieldType;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Map;
import java.util.function.Function;

/**
 * 解析H2特有的列约束，以及字段类型和字段属性
 *
 * @author bokun.li
 * @date 2025/9/10
 */
@Slf4j
public class H2OpColumnConstraints extends AbstractOpColumnConstraints {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.H2.getDb().equals(dbType);
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
        // h2 也支持 comment 语法 但是同时它也支持 pg和 oracle那种形式 所以不用comment这种 这种无法加表注释
//        String comment = ddlFieldInfo.getComment();
//        if (StrUtil.isNotBlank(comment)) {
//            if (!StrUtil.isWrap(comment, SP.SINGLE_QUOTE)) {
//                templateParams.put(COMMENTS, "comment " + StrUtil.wrap(comment, SP.SINGLE_QUOTE, SP.SINGLE_QUOTE));
//            } else {
//                templateParams.put(COMMENTS, "comment " + comment);
//            }
//        }

        //  BLOB, TEXT, GEOMETRY or JSON  can't have a default value
        //  POINT、LINESTRING、POLYGON
        //  PRIMARY KEY always can't have a default value
        H2SqlFieldType oracleFieldType = getH2FieldType(fieldClass, ddlFieldInfo);
        if (ListTs.asList(H2SqlFieldType.BLOB).contains(oracleFieldType) || ddlFieldInfo.isPrimary()) {
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

    public String getCurrentTimeFunctionName(H2SqlFieldType h2SqlFieldType) {
        H2SqlFieldType datetime = H2SqlFieldType.DATETIME;
        H2SqlFieldType timestamp = H2SqlFieldType.TIMESTAMP;
        H2SqlFieldType timestampWithTimeZone = H2SqlFieldType.TIMESTAMP_WITH_TIME_ZONE;
        if (datetime == h2SqlFieldType || timestamp == h2SqlFieldType || timestampWithTimeZone == h2SqlFieldType)
            return "current_timestamp";
        H2SqlFieldType date = H2SqlFieldType.DATE;
        if (date == h2SqlFieldType) return "current_date";
        H2SqlFieldType time = H2SqlFieldType.TIME;
        if (time == h2SqlFieldType) return "current_time";
        return null;
    }


    @Override
    public String getDataType(DDLFieldInfo ddlFieldInfo) {
        Class<?> fieldClass = ddlFieldInfo.getFieldClass();
        if (null == fieldClass) {
            String s = ddlFieldInfo.getSchema() + SP.DOT + ddlFieldInfo.getTableName() + SP.DOT + ddlFieldInfo.getName() + SP.DOT + ddlFieldInfo.getDataType();
            throw new IllegalArgumentException(s);
        }
        H2SqlFieldType h2SqlFieldType = getH2FieldType(fieldClass, ddlFieldInfo);
        CheckUtils.notNullMsg(h2SqlFieldType, fieldClass.getName() + "【" + ddlFieldInfo.getDataType() + "】" + ":can not select db field type!");
        return getDataTypeByH2SqlFieldType(h2SqlFieldType, ddlFieldInfo);
    }

    public String getDataTypeByH2SqlFieldType(H2SqlFieldType h2SqlFieldType, DDLFieldInfo ddlFieldInfo) {
        OpConfig opConfig = this.getOpContext().getOpConfig();
        String fieldTypeTemplate = StrUtil.blankToDefault(h2SqlFieldType.getFieldTypeTemplate(), h2SqlFieldType.getFieldType());
        int dataLength = ddlFieldInfo.getDataLength();
        int dataDecimal = ddlFieldInfo.getDataDecimal();
        //String[] dataTypeAttr = ddlFieldInfo.getDataTypeAttr();
        String dataTypeFormat;
        {
            boolean isStr = h2SqlFieldType == H2SqlFieldType.VARCHAR || h2SqlFieldType == H2SqlFieldType.CHAR || h2SqlFieldType == H2SqlFieldType.CHARACTER_VARYING || h2SqlFieldType == H2SqlFieldType.CHARACTER;
            if (isStr && dataLength <= 0) {
                dataLength = opConfig.getStrDefaultLength();
            }
            if (h2SqlFieldType == H2SqlFieldType.DECIMAL || h2SqlFieldType == H2SqlFieldType.NUMERIC) {
                dataLength = dataLength <= 0 ? opConfig.getNumLengthDefaultLength() : dataLength;
                dataDecimal = dataDecimal <= 0 ? opConfig.getNumDecimalDefaultLength() : dataDecimal;
                if (dataLength < dataDecimal) {
                    dataDecimal = 0;
                }
            }
            if (h2SqlFieldType == H2SqlFieldType.BIT) {
                dataLength = dataLength <= 0 ? 1 : dataLength;
            }
            CheckUtils.checkTrue(
                    StrUtil.isNotBlank(h2SqlFieldType.getFieldTypeTemplate()) && dataLength <= 0,
                    "the type " + h2SqlFieldType.getFieldType() + " need set dataLength，please check!"
            );
            if (isStr && dataLength == Integer.MAX_VALUE) {
                dataTypeFormat = H2SqlFieldType.CLOB.getFieldType();
                ddlFieldInfo.setDataType(dataTypeFormat);
            }else if(dataLength == Integer.MAX_VALUE && (h2SqlFieldType== H2SqlFieldType.BINARY || h2SqlFieldType== H2SqlFieldType.VARBINARY )){
                String fieldTypeTemplate1 = H2SqlFieldType.VARBINARY.getFieldTypeTemplate();
                return MessageFormat.format(fieldTypeTemplate1, String.valueOf(1000000000));
            } else {
                dataTypeFormat = MessageFormat.format(fieldTypeTemplate, String.valueOf(dataLength), String.valueOf(dataDecimal));
            }

        }
        return dataTypeFormat;
    }


    public H2SqlFieldType getH2FieldType(Class<?> fieldClass, DDLFieldInfo ddlFieldInfo) {
        String dataType = ddlFieldInfo.getDataType();
        if (ddlFieldInfo.isJson()) {
            return H2SqlFieldType.JSON;
        }
        if (ddlFieldInfo.isLob()) {
            return H2SqlFieldType.CLOB;
        }
        H2SqlFieldType h2SqlFieldType;
        if (dataType != null) {
            h2SqlFieldType = H2SqlFieldType.getFromDataType(dataType);
            if (null == h2SqlFieldType) {
                h2SqlFieldType = H2SqlFieldType.getByClass(fieldClass);
            }
        } else {
            h2SqlFieldType = H2SqlFieldType.getByClass(fieldClass);
        }
        return h2SqlFieldType;
    }
}
