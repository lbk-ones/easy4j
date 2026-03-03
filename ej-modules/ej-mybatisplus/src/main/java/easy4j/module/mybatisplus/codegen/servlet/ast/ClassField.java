package easy4j.module.mybatisplus.codegen.servlet.ast;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.Objects;

/**
 * 字段信息封装
 */
@Data
public class ClassField {
    // 字段名称（如userId）
    private String fieldName;
    // 字段类型（如Long、String、List<String>）
    private String fieldType;
    // 中文描述
    private String cnDesc;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ClassField that = (ClassField) object;
        return StrUtil.equals(fieldName, that.fieldName) && StrUtil.equals(fieldType, that.fieldType) && StrUtil.equals(cnDesc, that.cnDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, fieldType,cnDesc);
    }
}