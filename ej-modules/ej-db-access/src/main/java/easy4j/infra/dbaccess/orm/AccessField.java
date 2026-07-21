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


    public AccessField cloneNew() {
        AccessField accessField1 = new AccessField();
        accessField1.setField(this.getField());
        accessField1.setColumnName(this.getColumnName());
        accessField1.setEscapeColumnName(this.getEscapeColumnName());
        accessField1.setColumnValue(this.getColumnValue());
        accessField1.setGroup(this.getGroup());
        accessField1.setPlaceHolder(this.getPlaceHolder());
        accessField1.setPkIs(this.isPkIs());
        accessField1.setAutoIncrementIs(this.isAutoIncrementIs());
        return accessField1;
    }
}
