package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.common.annotations.Desc;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DDLTable {

    @Desc("表名")
    String tableName() default "";

    @Desc("数据库引擎")
    String engine() default "";

    @Desc("字符集（支持emoji）")
    String charset() default "";

    @Desc("排序规则")
    String collate() default "";

    @Desc("是否存在，只支持mysql")
    boolean ifNotExists() default false;

    @Desc("表名注释")
    String comment() default "";
}
