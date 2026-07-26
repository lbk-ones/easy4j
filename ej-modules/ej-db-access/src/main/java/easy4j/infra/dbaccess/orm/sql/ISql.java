package easy4j.infra.dbaccess.orm.sql;

import easy4j.infra.dbaccess.orm.RuntimeContext;

public interface ISql {

    <T> boolean match(RuntimeContext<T> runtimeContext);

    /**
     * 有方言则执行方言 没有则执行build
     * @param runtimeContext 上下文
     * @return sql字符串
     * @param <T> 反省约束
     */
    <T> String exe(RuntimeContext<T> runtimeContext);


    <T> String build(RuntimeContext<T> runtimeContext);

}
