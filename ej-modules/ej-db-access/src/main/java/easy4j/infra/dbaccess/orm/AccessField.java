package easy4j.infra.dbaccess.orm;

import lombok.Data;

import java.lang.reflect.Field;

@Data
public class AccessField {

    // 字段field
    private Field field;
    // 字段名称
    private String columnName;
    // 转义之后的字段名称
    private String escapeColumnName;
    // 字段值
    private Object columnValue;
    // 数据行数
    private int group;
    // 占位符
    private String placeHolder = "?";
    // 是否主键
    private boolean pkIs;
    // 是否递增
    private boolean autoIncrementIs;
}
