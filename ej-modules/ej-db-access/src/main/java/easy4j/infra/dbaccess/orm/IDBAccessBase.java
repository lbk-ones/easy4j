package easy4j.infra.dbaccess.orm;

import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.domain.PageRes;
import easy4j.infra.dbaccess.orm.conditions.IUpdateBuild;
import easy4j.infra.dbaccess.orm.conditions.IWhere;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.List;

/**
 * 一个简单的orm框架,去掉
 * <br/>
 * 泛型简化版本，这个版本，可以在调用方法的时候不传入class类型对象，只需要在创建的时候传一次就可以了
 * <pre>
 * 1、支持单表的增删改查
 * 2、支持多种数据库 mysql,postgresql,oracle,sqlserver,h2,db2
 * 3、支持多表联合查询
 * 4、能执行sql脚本
 * 5、支持TypeHandler自定义类型转换 {@link easy4j.infra.dbaccess.orm.handler.TypeHandler}
 * 6、可以使用Wd包装对象代替常用对象 {@link easy4j.infra.dbaccess.orm.conditions.wd.Wd}
 * 7、支持注解式参数自定义 {@link JdbcColumn}
 * 8、兼容mybatis注解，兼容java注解，兼容jpa等注解
 * 9、慢sql打印
 * 10、spring事务混入，或者跳出事务
 * 11、支持插件开发，扩展，内置乐观锁,逻辑删除等插件
 * 12、支持多数据源切换
 * </pre>
 *
 * @author bokun.li
 * @version 2.1.5
 * @since 2.1.4
 */
public interface IDBAccessBase<T> {


    /**
     * 获取数据库连接
     *
     * @return
     */
    Connection getConnection();


    /**
     * 执行sql脚本
     *
     * @param connection        数据库连接不穿则自动从连接池获取
     * @param ddlSql            文本形式的sql脚本
     * @param path              路径名称 可以是绝对路径也可以是相对类路径
     * @param isCloseConnection 执行完是否关闭链接
     * @throws IOException
     */
    void runScript(Connection connection, String ddlSql, List<String> path, boolean isCloseConnection) throws IOException;

    /**
     * 写入一条数据
     *
     * @param params 要写入的数据
     * @return 写入后的数据
     */
    T save(T params);

    /**
     * 写入多条数据
     *
     * @param params 参数
     * @return 写入后的数据
     */
    List<T> save(Iterable<T> params);

    /**
     * 以jdbcBatch的形式去写入，不带数据库自动回写
     * @param params 参数
     * @param batchSize 批量条数如果不设置则默认200条
     * @return 返回写入的条数
     */
    int batchSave(Iterable<T> params,int batchSize);

    /**
     * 以jdbcBatch的形式去写入，不带数据库自动回写
     * @param params 参数
     * @return 返回写入的条数
     */
    int batchSave(Iterable<T> params);

    /**
     * 根据条件删除
     *
     * @param whereBuild 条件构造器
     * @return 删除的条数
     */
    int delete(IWhere whereBuild);

    /**
     * 删除所有
     *
     * @return
     */
    int deleteAll();


    /**
     * 根据主键删除数据
     *
     * @param params 对象实例
     * @return 删除条数
     */
    int deleteById(T params);

    /**
     * 根据主键删除直接传入主键，只适用于单主键那种表
     *
     * @param primaryKey 主键的值 可以传入 Wd包装类
     * @return 受影响的条数
     */
    int deleteByPrimaryKey(Serializable primaryKey);

    /**
     * 根据主键批量删除
     *
     * @param ids   主键
     * @return 删除的条数
     */
    int deleteByIds(Iterable<T> ids);


    /**
     * 根据主键更新
     *
     * @param params     要更新的参数
     * @param isSkipNull 是否更新null值
     * @param whereBuild 条件构造器
     * @return 更新影响条数
     */
    int update(T params, boolean isSkipNull, IWhere whereBuild);

    /**
     * 使用IWhere进行更新
     *
     * @param updateBuild 更新构造器
     * @return 受影响条数
     */
    int update(IUpdateBuild updateBuild);

    /**
     * 根据主键更新
     *
     * @param params     要更新的参数
     * @param isSkipNull 是否更新null值
     * @return 更新影响条数
     */
    int updateById(T params, boolean isSkipNull);


    /**
     * 根据主键批量更新
     *
     * @param params     要更新的集合
     * @param isSkipNull 是否更新null值
     * @return 更新影响条数
     */
    int updateByIds(Iterable<T> params, boolean isSkipNull);


    /**
     * 批量动态更新（循环更新），会跟据主键去更新
     *
     * @param value      map集合
     * @param tableName  tableName
     * @param schema     schema
     * @param isSkipNull 是否更新null值
     * @return int
     */
    int dynamicUpdate(List<EasyMap<String, Object>> value, String tableName, String schema, boolean isSkipNull);


    /**
     * 批量动态写入
     *
     * @param value
     * @param tableName
     * @param schema
     * @return
     */
    int dynamicSave(List<EasyMap<String, Object>> value, String tableName, String schema);

    /**
     * 可以连表的复杂查询
     *
     * @param sql   join条件构造器
     * @return 对象集合
     */
    List<T> queryJoin(SqlWrapper sql);

    /**
     * 可以连表的复杂查询 (分页)
     * 不传page则效果和queryJoin没区别
     *
     * @param sql   join条件构造器
     * @return
     */
    PageRes queryPageJoin(SqlWrapper sql, Page<T> page);


    /**
     * 可以连表的复杂查询
     *
     * @param sql               带占位符的sql
     * @param resultFieldToCame 是否将返回map中的key转为驼峰
     * @return 对象集合
     */
    List<EasyMap<String, Object>> queryMapJoin(SqlWrapper sql, boolean resultFieldToCame);

    /**
     * 传入sql查询对象集合
     *
     * @param sql   带占位符的sql
     * @param args  可变参数列表
     * @return 对象集合
     */
    List<T> query(String sql, Object... args);

    /**
     * 传入sql查询一个对象
     *
     * @param sql   带占位符的sql
     * @param args  可变参数列表
     * @return 对象集合
     */
    T queryOne(String sql, Object... args);

    /**
     * 传入sql将查询结果以Map的结果返回
     *
     * @param sql               带占位符的sql
     * @param resultFieldToCame 是否将返回map中的key转为驼峰
     * @param args              可变参数列表
     * @return 对象集合
     */
    List<EasyMap<String, Object>> queryMapListBySql(String sql, boolean resultFieldToCame, Object... args);

    /**
     * 传入表名和查询条件将查询结果以Map的结果返回（根据传入的表名自动查询这个表的字段集合）whereBuild=null则是全查询
     *
     * @param schema            数据库schema
     * @param tableName         表名
     * @param resultFieldToCame 是否将结果字段转为驼峰
     * @param whereBuild        条件构造器
     * @param queryRealFields   是否从数据库查询真实字段信息
     * @return 返回结果
     */
    EasyMap<String, Object> queryMapByTableName(String schema, String tableName, boolean resultFieldToCame, IWhere whereBuild, boolean queryRealFields);

    /**
     * 传入表名和查询条件将查询结果以分页的形式返回（根据传入的表名自动查询这个表的字段集合）whereBuild=null则是全查询
     *
     * @param schema            数据库schema
     * @param tableName         表名
     * @param resultFieldToCame 是否将结果字段转为驼峰
     * @param whereBuild        条件构造器
     * @param queryRealFields   是否从数据库查询真实字段信息
     * @return 返回结果
     */
    PageRes queryPageByTableName(String schema, String tableName, boolean resultFieldToCame, IWhere whereBuild, boolean queryRealFields, Page<Object> page);

    /**
     * 根据条件构造器来查询结果集合，集合元素以Map形式返回
     *
     * @param whereBuild        条件构造器
     * @param resultFieldToCame 是否转为驼峰
     * @return List<EasyMap<String,Object>>
     */
    List<EasyMap<String, Object>> queryMapListByTableName(String schema, String tableName, boolean resultFieldToCame, IWhere whereBuild, boolean queryRealFields);

    /**
     * 根据条件构造器来查询结果集合
     *
     * @param whereBuild 条件构造器
     * @return 对象集合
     */
    List<T> query(IWhere whereBuild);

    /**
     * 查询所有
     *
     * @return
     */
    List<T> queryAll();


    /**
     * 根据条件构造器来查询单个结果
     *
     * @param whereBuild 条件构造器
     * @return T
     */
    T queryOne(IWhere whereBuild);

    /**
     * 查询数量
     *
     * @param whereBuild 条件构造器
     * @return 总数
     */
    long count(IWhere whereBuild);

    /**
     * 是否存在
     *
     * @param whereBuild 条件构造器
     * @return boolean
     */
    boolean exists(IWhere whereBuild);

    /**
     * 根据条件构造器来查询单个结果,以map形式返回
     *
     * @param whereBuild 条件构造器
     * @param toCamel    是否转为驼峰
     * @return T
     */
    EasyMap<String, Object> queryOneMap(IWhere whereBuild, boolean toCamel);

    /**
     * 根据条件构造器来分页查询结果集合
     *
     * @param whereBuild 条件构造器
     * @param page       分页传参
     * @return T
     */
    PageRes queryPage(IWhere whereBuild, Page<T> page);

    /**
     * 根据ID查询
     *
     * @param param
     * @return 返回结果
     */
    T queryById(T param);

    /**
     * 根据ID的值查询 只适用于单主键那种表
     *
     * @param primaryKey 可以传入 Wd包装类
     * @return 返回结果
     */
    T queryByPrimaryKey(Serializable primaryKey);

    /**
     * 截断表
     * @return 返回受影响条数
     */
    int truncate();

}
