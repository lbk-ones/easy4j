package easy4j.infra.dbaccess.orm.plugin.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.dbaccess.orm.*;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import easy4j.infra.dbaccess.orm.conditions.wd.Wd;
import easy4j.infra.dbaccess.orm.plugin.AbstractPlugin;
import easy4j.infra.dbaccess.orm.plugin.Version;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * 乐观锁插件，只影响更新和删除
 *
 * @author bokun.li
 */
public class VersionLockPlugin extends AbstractPlugin {

    @Override
    public String getName() {
        return "versionLockPlugin";
    }

    @Override
    public void contextPrepared(RuntimeContext<?> context) {
        List<AccessField> columnInfoList = context.getColumnInfoList();
        Access<?> access = context.getAccess();
        if (access == null) return;
        if (CollUtil.isEmpty(columnInfoList)) return;
        OperateType operateType = context.getOperateType();
        // 更新和删除才操作
        if (!(operateType.isDelete() || operateType.isUpdate()) || operateType == OperateType.TRUNCATE) {
            return;
        }
        WhereBuild where = access.getWhere();
        // 目前只有单条更新 所以这一个列表一定是一条记录的
        List<AccessField> updateFields = context.getUpdateFields();
        Set<String> columnName = new HashSet<>();
        for (AccessField accessField : columnInfoList) {
            Field field = accessField.getField();
            if (field == null) continue;
            boolean annotationPresent = field.isAnnotationPresent(Version.class);
            if (annotationPresent) {
                columnName.add(accessField.getColumnName());
            }
        }
        for (String s : columnName) {
            // 如果通过 updateBuild来更新 就不管
            // 通过对象来更新
            for (AccessField updateField : updateFields) {
                String columnName1 = updateField.getColumnName();
                if (StrUtil.equals(s, columnName1)) {
                    Object columnValue = updateField.getColumnValue();
                    if (columnValue == null) {
                        continue;
                    }
                    Object value = Wd.value(columnValue);
                    Number convert = Convert.convert(Number.class, value);
                    BigDecimal add = NumberUtil.add(convert, 1);
                    Wd.setNewValue(updateField, add);
                    if (where != null) {
                        where.eq(columnName1, columnValue);
                    }
                    break;
                }
            }
        }
    }
}
