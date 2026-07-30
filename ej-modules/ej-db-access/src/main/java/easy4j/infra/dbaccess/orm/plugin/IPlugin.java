package easy4j.infra.dbaccess.orm.plugin;

import easy4j.infra.dbaccess.orm.Access;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import javax.sql.DataSource;

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
     * 插件初始化
     */
    void init(Access<?> access);

    /**
     * 获取新的数据源
     */
    DataSource getDataSource(Access<?> access);

    /**
     * 上下文准备完成
     */
    void contextPrepared(RuntimeContext<?> context);


    /**
     * 执行完单条sql之后
     */
    void beforeReturn(RuntimeContext<?> context);

    /**
     * 完成执行
     */
    void finish(RuntimeContext<?> context);

}
