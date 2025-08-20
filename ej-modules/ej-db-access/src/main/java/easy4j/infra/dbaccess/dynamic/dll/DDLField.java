package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.common.annotations.Desc;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DDLField {

    @Desc("字段名称，默认属性名称转下划线")
    String name() default "";

    @Desc("是否是主键")
    boolean isPrimary() default false;

    @Desc("是否递增")
    boolean isAutoIncrement() default false;

    @Desc("字段类型")
    String dataType() default "";

    @Desc("长度")
    int dataLength() default 0;

    @Desc("精度")
    int dataDecimal() default 0;

    @Desc("数据类型元素通常会被解析为('xxx','xxx')")
    String[] dataTypeAttr() default {};

    @Desc("默认值")
    String def() default "";

    @Desc("默认值")
    int defInt() default -1;

    @Desc("如果是时间的话那么可以选择是否默认时间")
    boolean defTime() default false;

    @Desc("是否为空")
    boolean isNotNull() default false;

    @Desc("是否唯一")
    boolean isUnique() default false;

    @Desc("是否是单字段索引")
    boolean isIndex() default false;

    @Desc("是否长文本")
    boolean isLob() default false;

    @Desc("自定义约束 比如 unique、not null、default")
    String[] constraint() default {};

    @Desc("注释")
    String comment() default "";

    @Desc("check约束")
    String check() default "";
}
