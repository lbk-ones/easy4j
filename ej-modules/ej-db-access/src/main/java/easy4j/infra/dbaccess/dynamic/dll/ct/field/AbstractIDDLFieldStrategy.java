package easy4j.infra.dbaccess.dynamic.dll.ct.field;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;

import java.util.Collections;
import java.util.List;


public abstract class AbstractIDDLFieldStrategy implements IDDLFieldStrategy {


    @Override
    public String getResColumn(DDLFieldInfo ddlFieldInfo) {
        return null;
    }


    public int getStrDefaultLength() {
        return 255;
    }

    public int getNumLengthDefaultLength() {
        return 6;
    }

    public int getNumDecimalDefaultLength() {
        return 4;
    }

    /**
     * 校验 genConstraint 是否生成字段的额外约束
     *
     * @param ddlFieldInfo
     * @param objects
     */
    public void genConstraint(DDLFieldInfo ddlFieldInfo, List<String> objects) {
        if (ddlFieldInfo.isGenConstraint()) {
            if (ddlFieldInfo.isUnique()) {
                objects.add("unique");
            }
            String check = ddlFieldInfo.getCheck();
            if (StrUtil.isNotBlank(check)) {
                objects.add("check (" + check + ")");
            }
            String[] constraint = ddlFieldInfo.getConstraint();
            if (constraint != null) {
                Collections.addAll(objects, constraint);
            }
        }
    }

    /**
     * 是否是数字类型的默认值
     *
     * @author bokun.li
     * @date 2025-08-21
     */
    public boolean isNumberDefaultType(Class<?> fieldClass) {
        return fieldClass == byte.class ||
                fieldClass == Byte.class ||
                fieldClass == int.class ||
                fieldClass == Integer.class ||
                fieldClass == long.class ||
                fieldClass == Long.class ||
                fieldClass == short.class ||
                fieldClass == Short.class;
    }

}
