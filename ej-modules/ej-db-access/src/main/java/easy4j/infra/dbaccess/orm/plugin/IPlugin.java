package easy4j.infra.dbaccess.orm.plugin;

import easy4j.infra.dbaccess.orm.Access;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.sql.Connection;

/**
 * 插件
 *
 * @author bokun.li
 */
public interface IPlugin {

    /**
     * 插件名称
     */
    String getName();

    /**
     * 获取连接
     */
    Connection getConnection(Access<?> access);

    /**
     * 上下文准备完成
     */
    void contextPrepared(RuntimeContext<?> context);


    /**
     * 执行完单条sql之后
     */
    void beforeReturn(RuntimeContext<?> context);

}
