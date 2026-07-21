package easy4j.infra.dbaccess.orm;

import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.orm.conditions.UpdateBuild;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * 这是传参层包装类
 * @param <T>
 */
@Data
@Accessors(chain = true)
public class Access<T> {

    private T param;

    private Iterable<T> params;

    private Class<T> clazz;

    private Page<T> page;

    private OperateType operateType;

    private String tableName;

    private String schema;

    private String sql;

    private WhereBuild where;

    private UpdateBuild update;

    private boolean skipNullIs;

    private List<Object> args;

    private boolean resultFieldToCame;

    private boolean returnMap = false;

}
