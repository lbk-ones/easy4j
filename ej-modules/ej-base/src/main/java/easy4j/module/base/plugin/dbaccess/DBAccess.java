package easy4j.module.base.plugin.dbaccess;

import cn.hutool.core.lang.Dict;
import org.springframework.core.io.Resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * DBAccess
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface DBAccess {
    void init(Object object);

    /**
     * 保存一个
     *
     * @param record
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> int saveOne(T record, Class<T> aClass);

    /**
     * 批量保存
     *
     * @param record
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> int saveList(List<T> record, Class<T> aClass);

    /**
     * 通用型单个跟新 (过滤空值，空值不更新)
     *
     * @param <T>
     * @param beanObject
     * @param aClass
     * @return
     * @throws SQLException
     */
    <T> T updateByPrimaryKey(T beanObject, Class<T> aClass);

    <T> int saveOrUpdateByPrimaryKey(T beanObject, Class<T> aClass);


    /**
     * 通用型单个跟新 (过滤空值，空值不更新)
     *
     * @param logRecord
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T updateByPrimaryKeySelective(T logRecord, Class<T> aClass);

    /**
     * 通用型批量更新
     *
     * @param objectList
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> int updateListByPrimaryKey(List<T> objectList, Class<T> aClass);


    /**
     * 通用型批量更新 (过滤空值，空值不更新)
     *
     * @param objectList
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> int updateListByPrimaryKeySelective(List<T> objectList, Class<T> aClass);


    /**
     * 根据sql查询某一个对象
     *
     * @param sql
     * @param clazz
     * @param args
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T getObject(String sql, Class<T> clazz, Object... args);

    /**
     * 根据sql查询某一个对象集合
     *
     * @param sql
     * @param clazz
     * @param args
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> getObjectList(String sql, Class<T> clazz, Object... args);


    /**
     * 全部查询
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> getAll(Class<T> clazz);

    /**
     * 根据sql分页查询某一个对象
     *
     * @param sql
     * @param clazz
     * @param args
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> getObjectListByPage(Page<T> page, QueryFilter filter, Class<T> clazz, String sql, Object... args);

    /**
     * 根据主键查询某对象
     *
     * @param <T>
     * @param arg
     * @param clazz
     * @return
     * @throws SQLException
     */
    <T> T getObjectByPrimaryKey(Object arg, Class<T> clazz);

    /**
     * 根据多个主键查询对象集合
     *
     * @param <T>
     * @param args
     * @param clazz
     * @return
     * @throws SQLException
     */
    <T> List<T> getObjectByPrimaryKeys(List<Object> args, Class<T> clazz);

    /**
     * 根据条件查询数量
     *
     * @param object
     * @return
     * @throws SQLException
     */
    long countBy(Object object);

    /**
     * 根据条件map
     *
     * @param object
     * @return
     */
    long countByMap(Map<String, Object> object, Class<?> aClass);

    /**
     * 执行sql脚本
     *
     * @param resource
     * @throws SQLException
     */
    void runScript(Resource resource);

    /**
     * 获取数据库连接
     *
     * @return
     * @throws SQLException
     */
    Connection getConnection();

    /**
     * 删除所有
     *
     * @param tClass
     * @param <T>
     * @return
     */
    <T> int deleteAll(Class<T> tClass);


    /**
     * 根据主键删除
     *
     * @param object
     * @param tClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> int deleteByPrimaryKey(Object object, Class<T> tClass);


    <T> int deleteByMap(Dict dict, Class<T> tClass);

    /**
     * 根据实体类型查询
     *
     * @param object
     * @param tClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> getObjectBy(T object, Class<T> tClass);

    /**
     * 根据条件查询
     *
     * @param dict
     * @param tClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> getObjectByMap(Dict dict, Class<T> tClass);

    /**
     * 根据条件查询单个
     *
     * @param dict
     * @param tClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T getObjectOneByMap(Dict dict, Class<T> tClass);

}