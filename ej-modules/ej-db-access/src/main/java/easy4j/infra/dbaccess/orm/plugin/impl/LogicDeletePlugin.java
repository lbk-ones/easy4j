package easy4j.infra.dbaccess.orm.plugin.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.dbaccess.orm.*;
import easy4j.infra.dbaccess.orm.conditions.IWhere;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import easy4j.infra.dbaccess.orm.conditions.wd.Wd;
import easy4j.infra.dbaccess.orm.plugin.AbstractPlugin;
import easy4j.infra.dbaccess.orm.plugin.LogicDelete;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 逻辑删除，只影响 增 删 查,删除会改为更新语句
 *
 * @author bokun.li
 */
public class LogicDeletePlugin extends AbstractPlugin {

    @Override
    public String getName() {
        return "logicDeletePlugin";
    }

    @Override
    public void contextPrepared(RuntimeContext<?> context) {
        List<AccessField> columnInfoList = context.getColumnInfoList();
        Access<?> access = context.getAccess();
        if (access == null) return;
        if (CollUtil.isEmpty(columnInfoList)) return;
        OperateType operateType = context.getOperateType();
        // 更新和truncate不走这个逻辑
        if (operateType.isUpdate() || operateType == OperateType.TRUNCATE) {
            return;
        }
        // save delete query
        IWhere where = access.getWhere();
        List<AccessField> insertFields = context.getInsertFields();
        AccessUtils accessUtils = context.getAccessUtils();

        record Info(String name,Field field, LogicDelete logicDelete) {}

        Set<Info> deleteNameSet = new HashSet<>();
        for (AccessField accessField : columnInfoList) {
            Field field = accessField.getField();
            if (field == null) continue;
            // 逻辑删除
            boolean annotation2Present = field.isAnnotationPresent(LogicDelete.class);
            if (annotation2Present) {
                LogicDelete annotation = field.getAnnotation(LogicDelete.class);
                deleteNameSet.add(new Info(accessField.getColumnName(),accessField.getField(), annotation));
            }
        }
        // delete
        boolean isDelete = context.getOperateType() == OperateType.DELETE;
        if (isDelete && !deleteNameSet.isEmpty()) {
            context.setOperateType(OperateType.UPDATE);
            List<AccessField> list = new ArrayList<>();
            for (Info stringLogicDeletePair : deleteNameSet) {
                AccessField accessField = new AccessField();
                accessField.setField(stringLogicDeletePair.field());
                accessField.setColumnName(stringLogicDeletePair.name());
                String s = accessUtils.escapeCn(stringLogicDeletePair.name(), context.getDialectV2(), false);
                accessField.setEscapeColumnName(s);
                LogicDelete value = stringLogicDeletePair.logicDelete();
                Object val;
                if (value.isNumber()) {
                    val = Convert.convert(Number.class, value.yes());
                } else {
                    val = value.yes();
                }
                Wd.setNewValue(accessField,val);
                list.add(accessField);
            }
            context.setUpdateFields(list);
        }
        for (Info s : deleteNameSet) {
            // 写入带默认值
            LogicDelete value = s.logicDelete();
            Object val;
            if (value.isNumber()) {
                val = Convert.convert(Number.class, value.no());
            } else {
                val = value.no();
            }
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
                    Wd.setNewValue(insertField, val);
                }
            }
            // 查询
            if (where != null && !isDelete) {
                where.getWhere().orElseThrow().eq(s.name(), val);
            }
        }
    }
}
