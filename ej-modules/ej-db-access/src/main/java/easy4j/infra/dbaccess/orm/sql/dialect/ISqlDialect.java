package easy4j.infra.dbaccess.orm.sql.dialect;

import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.runner.PsRes;

import java.util.function.Function;

/**
 * 特定的sql方言接口,主要包括，sql构建输出，执行逻辑
 *
 * @author bokun.li
 * @since 2.1.4
 */
public interface ISqlDialect extends Function<RuntimeContext<?>, PsRes> {

    boolean match(RuntimeContext<?> context);

    /**
     * sql构建方法
     *
     * @param context
     * @return
     */
    String build(RuntimeContext<?> context);


    @Override
    default PsRes apply(RuntimeContext<?> runtimeContext) {
        return prepareStatementAndExe(runtimeContext);
    }

    /**
     * 重写这个方法 则替换掉 easy4j.infra.dbaccess.orm.runner.JdbcUtils 这里面的逻辑
     *
     * @param runtimeContext the function argument
     * @return PsRes
     */
    PsRes prepareStatementAndExe(RuntimeContext<?> runtimeContext);
}
