package easy4j.infra.dbaccess.orm;

import easy4j.infra.dbaccess.orm.conditions.wd.WdFieldInfo;
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
    /**
     * 字段值 这个值必须要经过 Wd包裹（null不包装） {@link easy4j.infra.dbaccess.orm.conditions.wd.Wd#setNewValue} {@link easy4j.infra.dbaccess.orm.conditions.wd.Wd#wrapIf(Object, WdFieldInfo)}
     * <hr/>
     * 获取真实值 {@link easy4j.infra.dbaccess.orm.conditions.wd.Wd#value(Object)}
     * <hr/>
     * 获取参数类型 {@link easy4j.infra.dbaccess.orm.conditions.wd.Wd#type(Class)}
     * <hr/>
     * 获取占位符 {@link easy4j.infra.dbaccess.orm.conditions.wd.Wd#place(Object)}
     * <hr/>
     * 获取类型转换器 {@link easy4j.infra.dbaccess.orm.conditions.wd.Wd#getTypeHandler(Object)}
     * <hr/>
     * 获取JdbcType {@link easy4j.infra.dbaccess.orm.conditions.wd.Wd#getJdbcType(Object)}
     */
    private Object columnValue;
    // 数据行数,写入会根据这个排序
    private int group;
    // 占位符
    private String placeHolder = "?";
    // 别名
    private String alias;
    // 是否主键
    private boolean pkIs;
    // 是否递增
    private boolean autoIncrementIs;

    // 是否跳过ps赋值
    private boolean skipPsSet = false;


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
        accessField1.setSkipPsSet(this.isSkipPsSet());
        accessField1.setAlias(this.getAlias());
        return accessField1;
    }
}
