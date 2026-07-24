package easy4j.infra.dbaccess.orm.conditions.wd;

import cn.hutool.db.meta.JdbcType;
import easy4j.infra.dbaccess.orm.handler.TypeHandler;
import lombok.Data;

@Data
public class WdFieldInfo {

    String placeHolder;

    Class<? extends TypeHandler<?>> typeHandler;

    JdbcType jdbcType;
}
