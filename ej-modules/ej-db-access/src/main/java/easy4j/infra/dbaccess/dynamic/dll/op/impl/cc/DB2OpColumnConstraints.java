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
import easy4j.infra.dbaccess.dynamic.dll.DB2FieldType;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.VersionChecker;

import java.text.MessageFormat;
import java.util.Map;

/**
 * DB2 相关约束处理
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public class DB2OpColumnConstraints extends AbstractOpColumnConstraints {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.DB2.getDb().equals(dbType);
    }

    /**
     * DB2 约束顺序：
     * DEFAULT 和 NOT NULL 可以同时存在
     * GENERATED ALWAYS AS IDENTITY 不需要 DEFAULT 和 NOT NULL
     * GENERATED ALWAYS AS (expr) 通常用于计算列
     *
     * @return String
     */
    @Override
    public String getTemplate() {
        return "[" + DEFAULT + "] [" + NOT_NULL + "] [" + CHECK + "] [" + UNIQUE + "] [" + PRIMARY_KEY + "] [" + AUTO_INCREMENT + "] [" + REFERENCES + "] [" + COMMENTS + "] [" + BEFORE + "] [" + AFTER + "]";
    }

    @Override
    public Map<String, String> getTemplateParams(DDLFieldInfo ddlFieldInfo) {
        OpConfig opConfig = this.getOpContext().getOpConfig();
        Map<String, String> templateParams = super.getTemplateParams(ddlFieldInfo);
        CheckUtils.notNull(ddlFieldInfo, "fieldClass");
        Class<?> fieldClass = ddlFieldInfo.getFieldClass();

        if (opConfig.isDateDefaultType(fieldClass)) {
            if (ddlFieldInfo.isDefTime()) {
                DB2FieldType db2FieldType = getDB2FieldType(ddlFieldInfo);
                String currentTimeFunc = getCurrentTimeFunc(db2FieldType);
                if (StrUtil.isNotBlank(currentTimeFunc)) {
                    templateParams.put(DEFAULT, "default " + currentTimeFunc);
                }
            } else {
                templateParams.remove(DEFAULT);
            }
        }

        db2AutoIncrement(ddlFieldInfo, templateParams, opConfig);

        // 主键不能为 CLOB 或 BLOB
        if (ddlFieldInfo.isPrimary() && ddlFieldInfo.isLob()) {
            templateParams.remove(PRIMARY_KEY);
        }

        return templateParams;
    }

    /**
     * DB2 自增处理
     * GENERATED ALWAYS AS IDENTITY 从 DB2 9.1 开始支持
     * 使用 GENERATED ALWAYS AS IDENTITY 时，不需要 DEFAULT 和 NOT NULL
     */
    private static void db2AutoIncrement(DDLFieldInfo ddlFieldInfo, Map<String, String> templateParams, OpConfig opConfig) {
        if (ddlFieldInfo.isAutoIncrement() && ddlFieldInfo.isPrimary()) {
            if (VersionChecker.isGreaterOrEqual(ddlFieldInfo.getDbVersion(), "9.1")) {
                Class<?> fieldClass = ddlFieldInfo.getFieldClass();
                if (opConfig.isNumberDefaultType(fieldClass)) {
                    templateParams.remove(DEFAULT);
                    templateParams.remove(NOT_NULL);

                    String gaai = "GENERATED ALWAYS AS IDENTITY";
                    StringBuilder gaaiBuilder = new StringBuilder(gaai);
                    boolean hasOptions = false;

                    long startWith = ddlFieldInfo.getStartWith();
                    long increment = ddlFieldInfo.getIncrement();

                    // DB2 IDENTITY 选项: START WITH, INCREMENT BY, MINVALUE, MAXVALUE, CYCLE, CACHE, NO CACHE
                    if (startWith != 0 || increment != 1) {
                        gaaiBuilder.append(" (");
                        if (startWith != 0) {
                            gaaiBuilder.append("START WITH ").append(startWith);
                            hasOptions = true;
                        }
                        if (increment != 1) {
                            if (hasOptions) {
                                gaaiBuilder.append(", ");
                            }
                            gaaiBuilder.append("INCREMENT BY ").append(Math.max(increment, 1));
                        }
                        gaaiBuilder.append(")");
                    }

                    templateParams.put(AUTO_INCREMENT, gaaiBuilder.toString());
                }
            }
        } else {
            // GENERATED ALWAYS AS (expr) - 计算列
            String generatedAlwaysAs = ddlFieldInfo.getGeneratedAlwaysAs();
            if (StrUtil.isNotBlank(generatedAlwaysAs)) {
                String generatedAlwaysAsModel = StrUtil.blankToDefault(ddlFieldInfo.getGeneratedAlwaysAsModel(), "");
                generatedAlwaysAsModel = StrUtil.isNotBlank(generatedAlwaysAsModel) ? SP.SPACE + generatedAlwaysAsModel : generatedAlwaysAsModel;
                templateParams.put(AUTO_INCREMENT, "generated always as (" + generatedAlwaysAs + ")" + generatedAlwaysAsModel);
            }
        }
    }

    /**
     * 获取 DB2 当前时间函数
     *
     * @param db2FieldType DB2 字段类型
     * @return 时间函数字符串
     */
    protected String getCurrentTimeFunc(DB2FieldType db2FieldType) {
        String currentTime = null;
        if (db2FieldType == DB2FieldType.TIMESTAMP) {
            // DB2 TIMESTAMP 包含日期和时间，精度到微秒
            currentTime = "CURRENT TIMESTAMP";
        } else if (db2FieldType == DB2FieldType.DATE) {
            currentTime = "CURRENT DATE";
        } else if (db2FieldType == DB2FieldType.TIME) {
            currentTime = "CURRENT TIME";
        }
        return currentTime;
    }

    @Override
    public String getDataType(DDLFieldInfo ddlFieldInfo) {
        return parseDataType(ddlFieldInfo);
    }

    /**
     * 解析数据类型
     *
     * @param ddlFieldInfo 字段信息
     * @return DB2 数据类型字符串
     */
    private String parseDataType(DDLFieldInfo ddlFieldInfo) {
        DB2FieldType db2FieldType = getDB2FieldType(ddlFieldInfo);
        if (null != db2FieldType) {
            return getDataTypeByDB2FieldType(db2FieldType, ddlFieldInfo);
        } else {
            throw new EasyException(ddlFieldInfo.getName() + "【" + ddlFieldInfo.getDataType() + "】" + " not select db2 datatype please check!");
        }
    }

    /**
     * 获取 DB2 字段类型
     *
     * @param ddlFieldInfo 字段信息
     * @return DB2 字段类型
     */
    private DB2FieldType getDB2FieldType(DDLFieldInfo ddlFieldInfo) {
        String fieldType = ddlFieldInfo.getDataType();
        DB2FieldType db2FieldType = DB2FieldType.getFromDataType(fieldType);

        if (null == db2FieldType) {
            if (ddlFieldInfo.isJson()) {
                db2FieldType = DB2FieldType.VARCHAR;
            } else if (ddlFieldInfo.isLob()) {
                // CLOB 用于大文本，BLOB 用于二进制数据
                if (ddlFieldInfo.getFieldClass() == byte[].class) {
                    db2FieldType = DB2FieldType.BLOB;
                } else {
                    db2FieldType = DB2FieldType.CLOB;
                }
            } else {
                Class<?> fieldClass = ddlFieldInfo.getFieldClass();
                if (null != fieldClass) {
                    db2FieldType = DB2FieldType.getByClass(fieldClass);
                }
            }
        }

        return db2FieldType;
    }

    /**
     * 根据 DB2 字段类型生成数据类型字符串
     *
     * @param db2FieldType DB2 字段类型
     * @param ddlFieldInfo 字段信息
     * @return 数据类型字符串
     */
    public String getDataTypeByDB2FieldType(DB2FieldType db2FieldType, DDLFieldInfo ddlFieldInfo) {
        String fieldTypeTemplate = StrUtil.blankToDefault(db2FieldType.getFieldTypeTemplate(), db2FieldType.getFieldType());
        int dataLength = ddlFieldInfo.getDataLength();
        int dataDecimal = ddlFieldInfo.getDataDecimal();
        OpConfig opConfig = this.getOpContext().getOpConfig();
        String dataTypeFormat;

        {
            // 字符类型长度处理
            if ((db2FieldType == DB2FieldType.VARCHAR ||
                    db2FieldType == DB2FieldType.CHAR || db2FieldType == DB2FieldType.CLOB ||
                    db2FieldType == DB2FieldType.VARGRAPHIC || db2FieldType == DB2FieldType.GRAPHIC) && dataLength <= 0) {
                dataLength = opConfig.getStrDefaultLength();
            }

            // 二进制类型长度处理
            if ((db2FieldType == DB2FieldType.VARBINARY || db2FieldType == DB2FieldType.BINARY) && dataLength <= 0) {
                dataLength = opConfig.getStrDefaultLength();
            }

            // 数字类型精度处理
            if (db2FieldType == DB2FieldType.DECIMAL || db2FieldType == DB2FieldType.NUMERIC ||
                    db2FieldType == DB2FieldType.DEC) {
                dataLength = dataLength <= 0 ? opConfig.getNumLengthDefaultLength() : dataLength;
                dataDecimal = dataDecimal <= 0 ? opConfig.getNumDecimalDefaultLength() : dataDecimal;
                // 小数位不能大于总位数
                if (dataLength < dataDecimal) {
                    dataDecimal = 0;
                }
            }

            // 需要长度但未设置时进行校验
            CheckUtils.checkTrue(
                    StrUtil.isNotBlank(db2FieldType.getFieldTypeTemplate()) && dataLength <= 0,
                    "the type " + db2FieldType.getFieldType() + " need set dataLength，please check!"
            );

            // VARCHAR 长度处理：DB2 VARCHAR 最大 32672（不包括 LONG VARCHAR 最大 32KB）
            if ((db2FieldType == DB2FieldType.VARCHAR ) &&
                    dataLength == Integer.MAX_VALUE) {
                dataTypeFormat = DB2FieldType.CLOB.getFieldType();
                // 回写
                ddlFieldInfo.setDataType(dataTypeFormat);
            } else {
                // DB2 VARCHAR 最多 32672 字节
                if (db2FieldType == DB2FieldType.VARCHAR) {
                    dataLength = Math.min(dataLength, 32672);
                }

                // DB2 VARBINARY 最多 32672 字节
                if (db2FieldType == DB2FieldType.VARBINARY) {
                    dataLength = Math.min(dataLength, 32672);
                }

                // DB2 GRAPHIC 最多 127 个字符（双字节），VARGRAPHIC 最多 16336 个字符
                if (db2FieldType == DB2FieldType.GRAPHIC) {
                    dataLength = Math.min(dataLength, 127);
                } else if (db2FieldType == DB2FieldType.VARGRAPHIC) {
                    dataLength = Math.min(dataLength, 16336);
                }

                dataTypeFormat = MessageFormat.format(fieldTypeTemplate, String.valueOf(dataLength), String.valueOf(dataDecimal));
            }
        }

        return dataTypeFormat;
    }
}