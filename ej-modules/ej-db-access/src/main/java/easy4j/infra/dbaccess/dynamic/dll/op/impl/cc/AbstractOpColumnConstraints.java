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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpColumnConstraints;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * AbstractOpColumnConstraints
 * 如果要重写模板内容 子类重写 getTemplate 即刻
 * 如果要重写模板参数获取逻辑 子类重写 getTemplateParams 即刻
 * getColumnConstraints 一般不用重写
 * getDataType（字段类型，比如 varchar(2333)） 一定要重写 因为每个数据库的数据类型都有所区别 没有一个大概的标准类型
 * getDataTypeExtra 字段类型额外属性 一般来说跟在 数据类型后面 有需要就重写 一般用不上那些属性，这里只是留个口子
 *
 * @author bokun.li
 * @date 2025/8/23
 * @see easy4j.infra.dbaccess.dynamic.dll.op.impl.cc.AbstractOpColumnConstraints#put(java.lang.String, java.lang.String) 如果模板参数不是默认的那些参数名称，那么就要调用这个方法给模板参数传值
 */
@Getter
public abstract class AbstractOpColumnConstraints implements OpColumnConstraints {

    // 上下文对象
    private OpContext opContext;
    // 保存参数名称的map
    private static final Map<String, String> FIELD_MAP = Maps.newHashMap();
    // 额外的参数 这个会覆盖 getTemplateParams 方法获取的参数
    private final Map<String, String> extParamMap = Maps.newHashMap();
    // 生成列约束
    public static final String GENERATED_ALWAYS_AS = "GENERATED_ALWAYS_AS";
    // null约束
    public static final String NOT_NULL = "NOT_NULL";
    // 默认值约束
    public static final String DEFAULT = "DEFAULT";
    // CHECK 约束
    public static final String CHECK = "CHECK";
    // 唯一约束
    public static final String UNIQUE = "UNIQUE";
    // 主键约束
    public static final String PRIMARY_KEY = "PRIMARY_KEY";
    // 注释 有的数据库没有
    public static final String COMMENTS = "COMMENTS";
    // 外键约束
    public static final String REFERENCES = "REFERENCES";
    // 自动递增的约束 支持的数据库有限
    public static final String AUTO_INCREMENT = "AUTO_INCREMENT";
    // 自定义后面的约束
    public static final String AFTER = "AFTER";
    // 自定义前面的约束
    public static final String BEFORE = "BEFORE";

    static {
        FIELD_MAP.put(GENERATED_ALWAYS_AS, GENERATED_ALWAYS_AS);
        FIELD_MAP.put(NOT_NULL, NOT_NULL);
        FIELD_MAP.put(DEFAULT, DEFAULT);
        FIELD_MAP.put(CHECK, CHECK);
        FIELD_MAP.put(UNIQUE, UNIQUE);
        FIELD_MAP.put(PRIMARY_KEY, PRIMARY_KEY);
        FIELD_MAP.put(COMMENTS, COMMENTS);
        FIELD_MAP.put(REFERENCES, REFERENCES);
        FIELD_MAP.put(AUTO_INCREMENT, AUTO_INCREMENT);
        FIELD_MAP.put(AFTER, AFTER);
        FIELD_MAP.put(BEFORE, BEFORE);
    }

    /**
     * 如果模板参数不是默认的那些参数名称，那么子类就要调用这个方法给模板参数传值
     *
     * @author bokun.li
     * @date 2025-08-24
     */
    public void put(String field, String value) {
        FIELD_MAP.putIfAbsent(field, field);
        extParamMap.putIfAbsent(field, value);
    }

    // [GENERATED_ALWAYS_AS] [NOT_NULL] [DEFAULT] [CHECK] [UNIQUE] [PRIMARY_KEY] [COMMENTS]
    public String getTemplate() {
        return "[" + BEFORE + "] [" + NOT_NULL + "] [" + DEFAULT + "] [" + CHECK + "] [" + UNIQUE + "] [" + PRIMARY_KEY + "] [" + AUTO_INCREMENT + "] [" + REFERENCES + "] [" + GENERATED_ALWAYS_AS + "] [" + COMMENTS + "] [" + AFTER + "]";
    }

    /**
     * 默认不解析 COMMENTS 支持这个语法的数据库有限
     * GENERATED_ALWAYS_AS 默认也不解析
     * REFERENCES 默认也不解析
     * AUTO_INCREMENT 默认也不解析
     *
     * @author bokun.li
     * @date 2025-08-24
     */
    public Map<String, String> getTemplateParams(DDLFieldInfo ddlFieldInfo) {
        Class<?> fieldClass = ddlFieldInfo.getFieldClass();
        OpConfig opConfig = this.getOpContext().getOpConfig();
        CheckUtils.notNull(fieldClass, "fieldClass");
        Map<@Nullable String, @Nullable String> pm = Maps.newHashMap();
        if (ddlFieldInfo.isNotNull()) {
            pm.put(NOT_NULL, "not null");
        }
        String def = ddlFieldInfo.getDef();
        int defNum = ddlFieldInfo.getDefNum();
        if(!ddlFieldInfo.isPrimary()){
            // defTime 不处理
            if (StrUtil.isNotBlank(def)) {
                if (opConfig.isNumberDefaultType(fieldClass)) {
                    boolean isNum = true;
                    try {
                        Integer.parseInt(def);
                    } catch (Exception e) {
                        isNum = false;
                    }
                    if (isNum) pm.put(DEFAULT, "default " + def);
                } else {
                    pm.put(DEFAULT, "default " + opConfig.wrapSingleQuote(def));
                }
            } else if (defNum != -1) {
                pm.put(DEFAULT, "default " + defNum);
            }
            // only valid json can be set
            if (ddlFieldInfo.isJson() && StrUtil.isNotBlank(def)) {
                if (!JacksonUtil.isValidJson(def)) {
                    pm.remove(DEFAULT);
                }
            }
        }
        if(ddlFieldInfo.isGenConstraint()){
            String check = ddlFieldInfo.getCheck();
            if (StrUtil.isNotBlank(check)) {
                pm.put(CHECK, "check (" + check + ")");
            }
            if (ddlFieldInfo.isUnique()) {
                pm.put(UNIQUE, "unique");
            }
            if (ddlFieldInfo.isPrimary()) {
                pm.put(PRIMARY_KEY, "primary key");
            }
        }
        return pm;
    }

    @Override
    public void setOpContext(OpContext opContext) {
        if(this.opContext == null){
            this.opContext = opContext;
        }
    }

    @Override
    public String getColumnConstraints(DDLFieldInfo ddlFieldInfo) {
        CheckUtils.notNull(ddlFieldInfo, "ddlFieldInfo");
        CheckUtils.notNull(this.opContext, "opContext");
        CheckUtils.checkByPath(ddlFieldInfo, "fieldClass");
        CheckUtils.checkByPath(this.opContext, "opConfig");
        OpConfig opConfig = this.opContext.getOpConfig();
        return opConfig.patchStrWithTemplate(ddlFieldInfo, getTemplate(),FIELD_MAP, extParamMap, this::getTemplateParams);
    }

    @Override
    public String getFieldName(DDLFieldInfo ddlFieldInfo) {
        String name = ddlFieldInfo.getName();
        CheckUtils.notNull(name, "name");
        return this.getOpContext().getOpConfig().getColumnName(name);
    }

    @Override
    public String getDataType(DDLFieldInfo ddlFieldInfo) {
        return null;
    }

    @Override
    public String getDataTypeExtra(DDLFieldInfo ddlFieldInfo) {
        return null;
    }

    @Override
    public String getCreateColumnSql(DDLFieldInfo ddlFieldInfo) {
        String fieldName = getFieldName(ddlFieldInfo);
        String dataType = getDataType(ddlFieldInfo);
        String dataTypeExtra = getDataTypeExtra(ddlFieldInfo);
        String columnConstraints = getColumnConstraints(ddlFieldInfo);
        return ListTs.asList(fieldName, dataType, dataTypeExtra, columnConstraints).stream().filter(StrUtil::isNotBlank).collect(Collectors.joining(SP.SPACE));
    }
}
