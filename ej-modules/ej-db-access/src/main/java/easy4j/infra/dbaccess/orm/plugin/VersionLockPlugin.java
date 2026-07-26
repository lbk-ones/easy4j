package easy4j.infra.dbaccess.orm.plugin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.dbaccess.orm.*;
import easy4j.infra.dbaccess.orm.conditions.Condition;
import easy4j.infra.dbaccess.orm.conditions.UpdateBuild;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 插件
 *
 * @author bokun.li
 */
public class VersionLockPlugin extends AbstractPlugin {

    @Override
    public String getName() {
        return "versionLock";
    }

    @Override
    public void contextPrepared(RuntimeContext<?> context) {
        List<AccessField> columnInfoList = context.getColumnInfoList();
        Access<?> access = context.getAccess();
        if (access == null) return;
        if (CollUtil.isEmpty(columnInfoList)) return;
        OperateType operateType = context.getOperateType();
        if (!(operateType.isDelete() || operateType.isUpdate()) || operateType == OperateType.TRUNCATE) {
            return;
        }
        WhereBuild where = access.getWhere();
        UpdateBuild update = access.getUpdate();
        // 目前只有单条更新 所以这一个列表一定是一条记录的
        List<AccessField> updateFields = context.getUpdateFields();
        AccessUtils accessUtils = context.getAccessUtils();
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
                    Field field = updateField.getField();
                    Object columnValue = updateField.getColumnValue();
                    if (columnValue == null) {
                        continue;
                    }
                    Number convert = Convert.convert(Number.class, columnValue);
                    BigDecimal add = NumberUtil.add(convert, 1);
                    Object convert1 = Convert.convert(field.getType(), add);
                    updateField.setColumnValue(convert1);
                    if (where != null) {
                        where.eq(columnName1, columnValue);
                    }
                    break;
                }
            }
        }


    }
}
