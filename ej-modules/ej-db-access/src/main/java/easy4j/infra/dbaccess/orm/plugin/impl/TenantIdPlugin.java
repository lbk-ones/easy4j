package easy4j.infra.dbaccess.orm.plugin.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.dbaccess.orm.*;
import easy4j.infra.dbaccess.orm.conditions.UpdateBuild;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import easy4j.infra.dbaccess.orm.conditions.wd.Wd;
import easy4j.infra.dbaccess.orm.plugin.AbstractPlugin;
import easy4j.infra.dbaccess.orm.plugin.IObtainTenantId;
import easy4j.infra.dbaccess.orm.plugin.TenantId;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 租户操作，影响 增 删  改 查
 *
 * @author bokun.li
 */
public class TenantIdPlugin extends AbstractPlugin {

    @Override
    public String getName() {
        return "tenantIdPlugin";
    }

    @Override
    public void contextPrepared(RuntimeContext<?> context) {
        List<AccessField> columnInfoList = context.getColumnInfoList();
        if (CollUtil.isEmpty(columnInfoList)) return;
        Access<?> access = context.getAccess();
        if (access == null) return;
        OperateType operateType = context.getOperateType();
        // 更新和truncate不走这个逻辑
        if (operateType == OperateType.TRUNCATE) {
            return;
        }
        // save delete query
        AccessConfig accessConfig = context.getAccessUtils().getAccessConfig();
        String globalTenantIdName = accessConfig.getGlobalTenantIdName();
        IObtainTenantId iObtainTenantId = accessConfig.getIObtainTenantId();
        if (iObtainTenantId == null) return;
        // 值的获取逻辑 一般tenantId在 请求头获取
        Object tenantId = iObtainTenantId.getTenantId(context);
        if (tenantId == null) return;
        WhereBuild where = access.getWhere();
        UpdateBuild update = access.getUpdate();
        List<AccessField> insertFields = context.getInsertFields();
        List<AccessField> updateFields = context.getUpdateFields();
        List<String> ignoreTenantIdTables = accessConfig.getIgnoreTenantIdTables();
        String tableName = context.getTableName();
        if (CollUtil.isNotEmpty(ignoreTenantIdTables)) {
            if (ignoreTenantIdTables
                    .stream()
                    .anyMatch(e -> StrUtil.equals(e, tableName))
            ) {
                return;
            }
        }
        List<String> ignoreTenantIdTablePrefix = accessConfig.getIgnoreTenantIdTablePrefix();
        if (CollUtil.isNotEmpty(ignoreTenantIdTablePrefix)) {
            if (ignoreTenantIdTablePrefix.stream().anyMatch(e -> StrUtil.startWithIgnoreCase(tableName, e))) {
                return;
            }
        }
        record Info(String name, Field field) {
        }

        Set<Info> tenantNameList = new HashSet<>();
        for (AccessField accessField : columnInfoList) {
            Field field = accessField.getField();
            if (field == null) continue;
            // 逻辑删除
            boolean annotation2Present = field.isAnnotationPresent(TenantId.class);
            if (annotation2Present) {
                tenantNameList.add(new Info(accessField.getColumnName(), accessField.getField()));
            } else {
                String columnName = accessField.getColumnName();
                if (StrUtil.equalsIgnoreCase(columnName, globalTenantIdName)) {
                    tenantNameList.add(new Info(accessField.getColumnName(), accessField.getField()));
                }
            }
        }
        for (Info s : tenantNameList) {
            // save
            for (AccessField insertField : insertFields) {
                String columnName1 = insertField.getColumnName();
                String key = s.name();
                if (StrUtil.equals(key, columnName1)) {
                    Object columnValue = insertField.getColumnValue();
                    // 说明手动赋值过了
                    if (columnValue != null) {
                        continue;
                    }
                    Wd.setNewValue(insertField, tenantId);
                }
            }

            // 更新的值
            for (AccessField updateField : updateFields) {
                String columnName1 = updateField.getColumnName();
                String key = s.name();
                if (StrUtil.equals(key, columnName1)) {
                    Object columnValue = updateField.getColumnValue();
                    // 说明手动赋值过了
                    if (columnValue != null) {
                        continue;
                    }
                    Wd.setNewValue(updateField, tenantId);
                }
            }

            // 查询/删除 的条件
            if (where != null) {
                where.eq(s.name(), tenantId);
            }

            // 更新的条件 update不为空 说明 updateFields是空的
            if (update != null) {
                if (updateFields.isEmpty()) {
                    update.set(true, s.name(), tenantId);
                }
                update.eq(s.name(), tenantId);
            }
        }
    }
}
