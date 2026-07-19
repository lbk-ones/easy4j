package easy4j.infra.dbaccess.orm;

import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import easy4j.infra.dbaccess.domain.PageRes;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 支持增删改查
 */
public interface IDBAccess {

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
     * 根据主键删除
     *
     * @param id    主键
     * @param clazz 对象类型
     * @param <T>   泛型
     * @return 删除的条数
     */
    <T> int deleteById(Serializable id, Class<T> clazz);

    /**
     * 根据主键删除数据
     *
     * @param params 对象实例
     * @param idGet  获取id的函数
     * @param clazz  对象类型
     * @param <T>    泛型约束
     * @return 删除条数
     */
    <T> int deleteById(T params, Function<T, Serializable> idGet, Class<T> clazz);

    /**
     * 根据主键批量删除
     *
     * @param ids   主键
     * @param clazz 对象类型
     * @param <T>   泛型
     * @return 删除的条数
     */
    <T> int deleteByIds(Iterable<Serializable> ids, Class<T> clazz);

    /**
     * 根据主键批量删除，如果传入的idGet=null则默认自动推断主键
     *
     * @param ids   主键
     * @param idGet 获取id的函数
     * @param clazz 对象类型
     * @param <T>   泛型
     * @return 删除的条数
     */
    <T> int deleteByIds(Iterable<T> ids, Function<T, Serializable> idGet, Class<T> clazz);


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
     * 根据主键更新，如果传入的idGet=null则默认自动推断主键
     *
     * @param params     要更新的参数
     * @param idGet      获取id的函数
     * @param isSkipNull 是否更新null值
     * @param clazz      对象类型
     * @param <T>        泛型约束
     * @return 更新影响条数
     */
    <T> int updateById(T params, Function<T, Serializable> idGet, boolean isSkipNull, Class<T> clazz);

    /**
     * 根据主键批量更新(默认自动推断主键)
     *
     * @param params     要更新的集合
     * @param isSkipNull 是否更新null值
     * @param clazz      对象类型
     * @param <T>        泛型约束
     * @return 更新影响条数
     */
    <T> int updateByIds(Iterable<T> params, boolean isSkipNull, Class<T> clazz);

    /**
     * 根据主键批量更新，如果传入的idGet=null则默认自动推断主键
     *
     * @param params     要更新的集合
     * @param isSkipNull 是否更新null值
     * @param clazz      对象类型
     * @param <T>        泛型约束
     * @return 更新影响条数
     */
    <T> int updateByIds(Iterable<T> params, Function<T, Serializable> idGet, boolean isSkipNull, Class<T> clazz);

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
     * @param sql  带占位符的sql
     * @param args 可变参数列表
     * @param <T>  泛型
     * @return 对象集合
     */
    <T> EasyMap<String, Object> queryMap(String sql, Object... args);

    /**
     * 传入表名和查询条件将查询结果以Map的结果返回（根据传入的表名自动查询这个表的字段集合）whereBuild=null则是全查询
     *
     * @param schema            数据库schema
     * @param tableName         表名
     * @param resultFieldToCame 是否将结果字段转为驼峰
     * @param whereBuild        条件构造器
     * @return 返回结果
     */
    EasyMap<String, Object> queryMapByTableName(String schema, String tableName, boolean resultFieldToCame, WhereBuild whereBuild);

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
     * 根据条件构造器来查询结果集合，集合元素以Map形式返回
     *
     * @param whereBuild 条件构造器
     * @param toCamel    是否转为驼峰
     * @return List<EasyMap<String,Object>>
     */
    List<EasyMap<String, Object>> queryMap(WhereBuild whereBuild, boolean toCamel);

    /**
     * 根据条件构造器来查询单个结果
     *
     * @param whereBuild 条件构造器
     * @return T
     */
    <T> T queryOne(WhereBuild whereBuild, Class<T> clazz);

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

}
