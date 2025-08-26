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
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.OracleFieldType;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

import java.text.MessageFormat;
import java.util.Map;

/**
 * 重写 oracle 相关约束
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public class OracleOpColumnConstraints extends AbstractOpColumnConstraints {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return "oracle".equals(dbType);
    }

    /**
     * oracle default 必须在 not null 前面
     * GENERATED ALWAYS AS 也必须在字段类型后面
     * 就很不灵活
     *
     * @return String
     */
    @Override
    public String getTemplate() {
        return "[" + GENERATED_ALWAYS_AS + "] [" + BEFORE + "]  [" + DEFAULT + "]  [" + NOT_NULL + "] [" + CHECK + "] [" + UNIQUE + "] [" + PRIMARY_KEY + "] [" + AUTO_INCREMENT + "] [" + REFERENCES + "] [" + COMMENTS + "] [" + AFTER + "]";
    }

    @Override
    public Map<String, String> getTemplateParams(DDLFieldInfo ddlFieldInfo) {
        OpConfig opConfig = this.getOpContext().getOpConfig();
        Map<String, String> templateParams = super.getTemplateParams(ddlFieldInfo);
        CheckUtils.notNull(ddlFieldInfo, "fieldClass");
        Class<?> fieldClass = ddlFieldInfo.getFieldClass();
        if (opConfig.isDateDefaultType(fieldClass)) {
            if (ddlFieldInfo.isDefTime()) {
                OracleFieldType oracleFieldType = getOracleFieldType(ddlFieldInfo);
                String currentTimeFunc = getCurrentTimeFunc(oracleFieldType);
                templateParams.put(DEFAULT, "default " + currentTimeFunc);
            } else {
                templateParams.remove(DEFAULT);
            }
        }
        oracleAutoIncrement(ddlFieldInfo, templateParams, opConfig);
        return templateParams;
    }

    // 递增不需要默认值oracle会报错的，非空也没必要
    private static void oracleAutoIncrement(DDLFieldInfo ddlFieldInfo, Map<String, String> templateParams, OpConfig opConfig) {
        if (ddlFieldInfo.isAutoIncrement() && ddlFieldInfo.isPrimary()) {
            if (opConfig.checkSupportVersion(ddlFieldInfo.getDbVersion(), 12)) {
                Class<?> fieldClass = ddlFieldInfo.getFieldClass();
                if (opConfig.isNumberDefaultType(fieldClass)) {
                    templateParams.remove(DEFAULT);
                    templateParams.remove(NOT_NULL);
                    String gaai = "GENERATED ALWAYS AS IDENTITY";
                    String gaaiTemp = "(START WITH {startWith} INCREMENT BY {increment})";
                    boolean hasStart = false;
                    if (ddlFieldInfo.getStartWith() != 0) {
                        hasStart = true;
                        gaaiTemp = gaaiTemp.replace("{startWith}", "" + ddlFieldInfo.getStartWith());
                    } else {
                        gaaiTemp = gaaiTemp.replace("{startWith}", "0");
                    }
                    if (ddlFieldInfo.getIncrement() != 1) {
                        hasStart = true;
                        gaaiTemp = gaaiTemp.replace("{increment}", "" + Math.max(ddlFieldInfo.getIncrement(), 1));
                    } else {
                        gaaiTemp = gaaiTemp.replace("{increment}", "1");
                    }
                    if (hasStart) gaai += gaaiTemp;
                    templateParams.put(GENERATED_ALWAYS_AS, gaai);
                }
            }
        } else {
            // generated always as (expr)
            String generatedAlwaysAs = ddlFieldInfo.getGeneratedAlwaysAs();
            if (StrUtil.isNotBlank(generatedAlwaysAs)) {
                String generatedAlwaysAsModel = StrUtil.blankToDefault(ddlFieldInfo.getGeneratedAlwaysAsModel(), "");
                generatedAlwaysAsModel = StrUtil.isNotBlank(generatedAlwaysAsModel) ? SP.SPACE + generatedAlwaysAsModel : generatedAlwaysAsModel;
                if (ddlFieldInfo.isGeneratedAlwaysAsNotNull()) {
                    generatedAlwaysAsModel += " not null";
                }
                templateParams.put(GENERATED_ALWAYS_AS, "generated always as (" + generatedAlwaysAs + ")" + generatedAlwaysAsModel);
            }
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

    @Override
    public String getDataType(DDLFieldInfo ddlFieldInfo) {
        return parseDataType(ddlFieldInfo);
    }

    private String parseDataType(DDLFieldInfo ddlFieldInfo) {
        OracleFieldType fromDataType = getOracleFieldType(ddlFieldInfo);
        if (null != fromDataType) {
            return getDataTypeByPGFieldType(fromDataType, ddlFieldInfo);
        } else {
            throw new EasyException(ddlFieldInfo.getName() + " not select pgsql datatype please check!");
        }

    }

    private OracleFieldType getOracleFieldType(DDLFieldInfo ddlFieldInfo) {
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
                }
            }
        }
        return fromDataType;
    }

    public String getDataTypeByPGFieldType(OracleFieldType oracleFieldType, DDLFieldInfo ddlFieldInfo) {
        String fieldTypeTemplate = StrUtil.blankToDefault(oracleFieldType.getFieldTypeTemplate(), oracleFieldType.getFieldType());
        int dataLength = ddlFieldInfo.getDataLength();
        int dataDecimal = ddlFieldInfo.getDataDecimal();
        OpConfig opConfig = this.getOpContext().getOpConfig();
        String dataTypeFormat;
        {
            // string
            if ((oracleFieldType == OracleFieldType.VARCHAR2 || oracleFieldType == OracleFieldType.CHAR || oracleFieldType == OracleFieldType.CLOB || oracleFieldType == OracleFieldType.LONG) && dataLength <= 0) {
                dataLength = opConfig.getStrDefaultLength();
            }
            if (oracleFieldType == OracleFieldType.DECIMAL || oracleFieldType == OracleFieldType.NUMERIC || oracleFieldType == OracleFieldType.NUMBER_DECIMAL) {
                dataLength = dataLength <= 0 ? opConfig.getNumLengthDefaultLength() : dataLength;
                dataDecimal = dataDecimal <= 0 ? opConfig.getNumDecimalDefaultLength() : dataDecimal;
            }
            CheckUtils.checkTrue(
                    StrUtil.isNotBlank(oracleFieldType.getFieldTypeTemplate()) && dataLength <= 0,
                    "the type " + oracleFieldType.getFieldType() + " need set dataLength，please check!"
            );
            dataTypeFormat = MessageFormat.format(fieldTypeTemplate, dataLength, dataDecimal);
        }
        return dataTypeFormat;
    }

}
