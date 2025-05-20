package easy4j.module.base.plugin.dbaccess;

import org.springframework.core.io.Resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DBAccess {
    void init(Object object);

    <T> int saveOne(T record, Class<T> aClass) throws SQLException;

    <T> int saveList(List<T> record, Class<T> aClass) throws SQLException;

    /**
     * 通用型单个跟新 (过滤空值，空值不更新)
     *
     * @param logRecord
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T updateByPrimaryKey(T logRecord, Class<T> aClass) throws SQLException;

    /**
     * 通用型单个跟新 (过滤空值，空值不更新)
     *
     * @param logRecord
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T updateByPrimaryKeySelective(T logRecord, Class<T> aClass) throws SQLException;

    /**
     * 通用型批量更新
     *
     * @param objectList
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> int updateListByPrimaryKey(List<T> objectList, Class<T> aClass) throws SQLException;


    /**
     * 通用型批量更新 (过滤空值，空值不更新)
     *
     * @param objectList
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> int updateListByPrimaryKeySelective(List<T> objectList, Class<T> aClass) throws SQLException;


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
    <T> T getObject(String sql, Class<T> clazz, Object... args) throws SQLException;

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
    <T> List<T> getObjectList(String sql, Class<T> clazz, Object... args) throws SQLException;


    <T> List<T> getAll(Class<T> clazz) throws SQLException;

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
    <T> List<T> getObjectListByPage(Page<T> page, QueryFilter filter, Class<T> clazz, String sql, Object... args) throws SQLException;

    /**
     * 根据主键查询某对象
     *
     * @param clazz
     * @param arg
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T getObjectByPrimaryKey(Class<T> clazz, Object arg) throws SQLException;

    /**
     * 根据多个主键查询对象集合
     *
     * @param clazz
     * @param args
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> getObjectByPrimaryKeys(Class<T> clazz, List<Object> args) throws SQLException;

    /**
     * 根据条件查询数量
     *
     * @param object
     * @return
     * @throws SQLException
     */
    long countBy(Object object) throws SQLException;

    /**
     * 执行sql脚本
     *
     * @param resource
     * @throws SQLException
     */
    void runScript(Resource resource) throws SQLException;

    /**
     * 获取数据库连接
     *
     * @return
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * 删除所有
     *
     * @param workIpClass
     * @param <T>
     * @return
     */
    <T> int deleteAll(Class<T> workIpClass) throws SQLException;
}
