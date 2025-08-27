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
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.PgSQLFieldType;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.VersionChecker;

import java.text.MessageFormat;
import java.util.Map;

/**
 * @author bokun.li
 * @date 2025/8/23
 */
public class PgOpColumnConstraints extends AbstractOpColumnConstraints {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.POSTGRE_SQL.getDb().equals(dbType);
    }

    @Override
    public Map<String, String> getTemplateParams(DDLFieldInfo ddlFieldInfo) {
        Map<String, String> templateParams = super.getTemplateParams(ddlFieldInfo);
        if (ddlFieldInfo.isUniqueNotNullDistinct()) {
            templateParams.put(UNIQUE, "unique nulls not distinct");
        }
        OpConfig opConfig = this.getOpContext().getOpConfig();
        Class<?> fieldClass = ddlFieldInfo.getFieldClass();
        CheckUtils.notNull(fieldClass, "fieldClass");
        if (opConfig.isDateDefaultType(fieldClass)) {
            if (ddlFieldInfo.isDefTime()) {
                PgSQLFieldType oracleFieldType = getPgSQLFieldType(ddlFieldInfo);
                String currentTimeFunc = getCurrentTimeFunc(oracleFieldType);
                templateParams.put(DEFAULT, "default " + currentTimeFunc);
            } else {
                templateParams.remove(DEFAULT);
            }
        }
        oracleAutoIncrement(ddlFieldInfo, templateParams, opConfig);
        return templateParams;
    }

    private void oracleAutoIncrement(DDLFieldInfo ddlFieldInfo, Map<String, String> templateParams, OpConfig opConfig) {

        // GENERATED ALWAYS AS      only support pg version great than 9.5
        if (!VersionChecker.isGreaterOrEqual(this.getOpContext().getDbVersion(), "9.5")) {
            return;
        }
        if (ddlFieldInfo.isAutoIncrement() && ddlFieldInfo.isPrimary()) {
            Class<?> fieldClass = ddlFieldInfo.getFieldClass();
            int startWith = ddlFieldInfo.getStartWith();
            int increment = ddlFieldInfo.getIncrement();
            if (opConfig.isNumberDefaultType(fieldClass) && !(startWith == 0 && increment == 1)) {
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
        } else {
            // generated always as (expr)
            String generatedAlwaysAs = ddlFieldInfo.getGeneratedAlwaysAs();
            if (StrUtil.isNotBlank(generatedAlwaysAs)) {
                String generatedAlwaysAsModel = StrUtil.blankToDefault(ddlFieldInfo.getGeneratedAlwaysAsModel(), "stored");
                generatedAlwaysAsModel = StrUtil.isNotBlank(generatedAlwaysAsModel) ? SP.SPACE + generatedAlwaysAsModel : generatedAlwaysAsModel;
                if (ddlFieldInfo.isGeneratedAlwaysAsNotNull()) {
                    generatedAlwaysAsModel += " not null";
                }
                templateParams.put(GENERATED_ALWAYS_AS, "generated always as (" + generatedAlwaysAs + ")" + generatedAlwaysAsModel);
            }
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


    @Override
    public String getDataType(DDLFieldInfo ddlFieldInfo) {
        return parseDataType(ddlFieldInfo);
    }

    private String parseDataType(DDLFieldInfo ddlFieldInfo) {
        PgSQLFieldType fromDataType = getPgSQLFieldType(ddlFieldInfo);
        if (null != fromDataType) {
            return getDataTypeByPGFieldType(fromDataType, ddlFieldInfo);
        } else {
            throw new EasyException(ddlFieldInfo.getName() + " not select pgsql datatype please check!");
        }

    }

    private PgSQLFieldType getPgSQLFieldType(DDLFieldInfo ddlFieldInfo) {
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
                    int startWith = ddlFieldInfo.getStartWith();
                    int increment = ddlFieldInfo.getIncrement();
                    boolean less95 = !VersionChecker.isGreaterOrEqual(this.getOpContext().getDbVersion(), "9.5");
                    // auto increment
                    if (ddlFieldInfo.isAutoIncrement() && (less95 || (startWith == 0 && increment == 1))) {
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
        return fromDataType;
    }

    public String getDataTypeByPGFieldType(PgSQLFieldType pgsqlFieldType, DDLFieldInfo ddlFieldInfo) {
        String fieldTypeTemplate = StrUtil.blankToDefault(pgsqlFieldType.getFieldTypeTemplate(), pgsqlFieldType.getFieldType());
        int dataLength = ddlFieldInfo.getDataLength();
        int dataDecimal = ddlFieldInfo.getDataDecimal();
        String dataTypeFormat;
        OpConfig opConfig = this.getOpContext().getOpConfig();
        {
            // string
            if ((pgsqlFieldType == PgSQLFieldType.VARCHAR || pgsqlFieldType == PgSQLFieldType.CHAR || pgsqlFieldType == PgSQLFieldType.BPCHAR) && dataLength <= 0) {
                dataLength = opConfig.getStrDefaultLength();
            }
            if (pgsqlFieldType == PgSQLFieldType.DECIMAL || pgsqlFieldType == PgSQLFieldType.NUMERIC) {
                dataLength = dataLength <= 0 ? opConfig.getNumLengthDefaultLength() : dataLength;
                dataDecimal = dataDecimal <= 0 ? opConfig.getNumDecimalDefaultLength() : dataDecimal;
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
}
