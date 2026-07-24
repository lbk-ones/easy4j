package easy4j.infra.dbaccess.orm;

import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.orm.conditions.UpdateBuild;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import easy4j.infra.dbaccess.domain.PageRes;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.List;

/**
 * 一个简单的orm框架
 *
 * <pre>
 * 1、支持单表的增删改查
 * 2、支持多种数据库 mysql,postgresql,oracle,sqlserver,h2,db2
 * 3、支持多表联合查询
 * 4、能执行sql脚本
 * 5、支持TypeHandler自定义类型转换 {@link easy4j.infra.dbaccess.orm.handler.TypeHandler}
 * 6、可以使用Wd包装对象代替常用对象 {@link easy4j.infra.dbaccess.orm.conditions.wd.Wd}
 * 7、支持注解式参数自定义 {@link easy4j.infra.dbaccess.orm.conditions.wd.WdField}
 * 8、兼容mybatis注解，兼容java注解，兼容jpa等注解
 * </pre>
 *
 * @since 2.1.4
 * @author bokun.li
 */
public interface IDBAccess {


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
     * @param clazz  要写入的字节码对象
     * @param <T>    泛型
     * @return 写入后的数据
     */
    <T> T save(T params, Class<T> clazz);

    /**
     * 写入多条数据
     *
     * @param params 参数
     * @param clazz  对象类型
     * @param <T>    泛型
     * @return 写入后的数据
     */
    <T> List<T> save(Iterable<T> params, Class<T> clazz);

    /**
     * 根据条件删除
     *
     * @param whereBuild 条件构造器
     * @param clazz      对象类型
     * @param <T>        泛型
     * @return 删除的条数
     */
    <T> int delete(WhereBuild whereBuild, Class<T> clazz);

    /**
     * 删除所有
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> int deleteAll(Class<T> clazz);


    /**
     * 根据主键删除数据
     *
     * @param params 对象实例
     * @param clazz  对象类型
     * @param <T>    泛型约束
     * @return 删除条数
     */
    <T> int deleteById(T params, Class<T> clazz);

    /**
     * 根据主键删除直接传入主键，只适用于单主键那种表
     * @param primaryKey 主键的值 可以传入 Wd包装类
     * @param clazz 类字节码对象
     * @return 受影响的条数
     * @param <T> 泛型约束
     */
    <T> int deleteByPrimaryKey(Serializable primaryKey, Class<T> clazz);

    /**
     * 根据主键批量删除
     *
     * @param ids   主键
     * @param clazz 对象类型
     * @param <T>   泛型
     * @return 删除的条数
     */
    <T> int deleteByIds(Iterable<T> ids, Class<T> clazz);


    /**
     * 根据主键更新
     *
     * @param params     要更新的参数
     * @param isSkipNull 是否更新null值
     * @param whereBuild 条件构造器
     * @param clazz      对象类型
     * @param <T>        泛型约束
     * @return 更新影响条数
     */
    <T> int update(T params, boolean isSkipNull, WhereBuild whereBuild, Class<T> clazz);

    /**
     * 使用UpdateBuild进行更新
     *
     * @param updateBuild 更新构造器
     * @param clazz       类
     * @param <T>         泛型
     * @return 受影响条数
     */
    <T> int update(UpdateBuild updateBuild, Class<T> clazz);

    /**
     * 根据主键更新
     *
     * @param params     要更新的参数
     * @param isSkipNull 是否更新null值
     * @param clazz      对象类型
     * @param <T>        泛型约束
     * @return 更新影响条数
     */
    <T> int updateById(T params, boolean isSkipNull, Class<T> clazz);


    /**
     * 根据主键批量更新
     *
     * @param params     要更新的集合
     * @param isSkipNull 是否更新null值
     * @param clazz      对象类型
     * @param <T>        泛型约束
     * @return 更新影响条数
     */
    <T> int updateByIds(Iterable<T> params, boolean isSkipNull, Class<T> clazz);

    /**
     * 可以连表的复杂查询
     *
     * @param sql   带占位符的sql
     * @param clazz 对象类型
     * @param <T>   泛型
     * @return 对象集合
     */
    <T> List<T> queryJoin(SqlWrapper sql,Class<T> clazz);


    /**
     * 可以连表的复杂查询
     *
     * @param sql   带占位符的sql
     * @param resultFieldToCame 是否将返回map中的key转为驼峰
     * @param <T>   泛型
     * @return 对象集合
     */
    <T> List<EasyMap<String,Object>> queryMapJoin(SqlWrapper sql,boolean resultFieldToCame);

    /**
     * 传入sql查询对象集合
     *
     * @param sql   带占位符的sql
     * @param clazz 对象类型
     * @param args  可变参数列表
     * @param <T>   泛型
     * @return 对象集合
     */
    <T> List<T> query(String sql, Class<T> clazz, Object... args);

    /**
     * 传入sql查询一个对象
     *
     * @param sql   带占位符的sql
     * @param clazz 对象类型
     * @param args  可变参数列表
     * @param <T>   泛型
     * @return 对象集合
     */
    <T> T queryOne(String sql, Class<T> clazz, Object... args);

    /**
     * 传入sql将查询结果以Map的结果返回
     *
     * @param <T>               泛型
     * @param sql               带占位符的sql
     * @param resultFieldToCame 是否将返回map中的key转为驼峰
     * @param args              可变参数列表
     * @return 对象集合
     */
    <T> EasyMap<String, Object> queryMapListBySql(String sql, boolean resultFieldToCame, Object... args);

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
    EasyMap<String, Object> queryMapByTableName(String schema, String tableName, boolean resultFieldToCame, WhereBuild whereBuild, boolean queryRealFields);

    /**
     * 根据条件构造器来查询结果集合，集合元素以Map形式返回
     *
     * @param whereBuild        条件构造器
     * @param resultFieldToCame 是否转为驼峰
     * @return List<EasyMap<String,Object>>
     */
    List<EasyMap<String, Object>> queryMapListByTableName(String schema, String tableName, boolean resultFieldToCame, WhereBuild whereBuild, boolean queryRealFields);

    /**
     * 根据条件构造器来查询结果集合
     *
     * @param whereBuild 条件构造器
     * @param clazz      对象类型
     * @param <T>        泛型
     * @return 对象集合
     */
    <T> List<T> query(WhereBuild whereBuild, Class<T> clazz);

    /**
     * 查询所有
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> List<T> queryAll(Class<T> clazz);


    /**
     * 根据条件构造器来查询单个结果
     *
     * @param whereBuild 条件构造器
     * @return T
     */
    <T> T queryOne(WhereBuild whereBuild, Class<T> clazz);

    /**
     * 查询数量
     *
     * @param whereBuild 条件构造器
     * @param clazz      类型
     * @param <T>        泛型
     * @return 总数
     */
    <T> long count(WhereBuild whereBuild, Class<T> clazz);

    /**
     * 是否存在
     *
     * @param whereBuild 条件构造器
     * @param clazz      类型
     * @param <T>        泛型
     * @return boolean
     */
    <T> boolean exists(WhereBuild whereBuild, Class<T> clazz);

    /**
     * 根据条件构造器来查询单个结果,以map形式返回
     *
     * @param whereBuild 条件构造器
     * @param toCamel    是否转为驼峰
     * @return T
     */
    <T> EasyMap<String, Object> queryOneMap(WhereBuild whereBuild, Class<T> clazz, boolean toCamel);

    /**
     * 根据条件构造器来分页查询结果集合
     *
     * @param whereBuild 条件构造器
     * @param page       分页传参
     * @param clazz      对象类型
     * @return T
     */
    <T> PageRes queryPage(WhereBuild whereBuild, Page<T> page, Class<T> clazz);

    /**
     * 根据ID查询
     *
     * @param param
     * @param clazz
     * @param <T>
     * @return 返回结果
     */
    <T> T queryById(T param, Class<T> clazz);

    /**
     * 根据ID的值查询 只适用于单主键那种表
     *
     * @param primaryKey 可以传入 Wd包装类
     * @param clazz
     * @param <T>
     * @return 返回结果
     */
    <T> T queryByPrimaryKey(Serializable primaryKey, Class<T> clazz);

    /**
     * 截断表
     * @param clazz
     * @param <T>
     * @return 返回受影响条数
     */
    <T> int truncate(Class<T> clazz);

}
