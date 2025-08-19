package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.common.annotations.Desc;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndex;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DDLTable {

    @Desc("表名")
    String tableName() default "";

    @Desc("数据库引擎，mysql专用")
    String engine() default "";

    @Desc("字符集（支持emoji），mysql专用")
    String charset() default "";

    @Desc("排序规则，mysql专用")
    String collate() default "";

    @Desc("是否存在，只支持mysql、pg")
    boolean ifNotExists() default false;

    @Desc("表名注释")
    String comment() default "";

    @Desc("索引信息")
    DDLIndex[] indexes() default {};
}
