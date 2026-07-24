package easy4j.infra.dbaccess.orm.conditions.wd;

import cn.hutool.db.meta.JdbcType;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.handler.DefaultTypeHandler;
import easy4j.infra.dbaccess.orm.handler.TypeHandler;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WdField {

    String placeHolder() default SP.COMMA;

    JdbcType jdbcType() default JdbcType.NULL;

    Class<? extends TypeHandler<?>> typeHandler() default DefaultTypeHandler.class;

}
