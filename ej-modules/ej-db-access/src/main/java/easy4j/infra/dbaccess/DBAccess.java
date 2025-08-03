/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.dbaccess;

import cn.hutool.core.lang.Dict;
import easy4j.infra.dbaccess.condition.WhereBuild;
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

    void printPrintLog(boolean isPrintLog);

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
     * @param isQuery
     * @return
     * @throws SQLException
     */
    <T> T updateByPrimaryKey(T beanObject, Class<T> aClass, boolean isQuery);

    <T> int saveOrUpdateByPrimaryKey(T beanObject, Class<T> aClass);


    /**
     * 通用型单个跟新 (过滤空值，空值不更新)
     *
     * @param <T>
     * @param logRecord
     * @param aClass
     * @param isQuery
     * @return
     * @throws SQLException
     */
    <T> T updateByPrimaryKeySelective(T logRecord, Class<T> aClass, boolean isQuery);

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
    <T> T selectOne(String sql, Class<T> clazz, Object... args);

    /**
     * 查询第一个字段的值
     *
     * @param sql   要传入的sql
     * @param clazz 最后要转成的类型
     * @param args  sql对应的参数
     * @param <T>
     * @return
     */
    <T> T selectScalar(String sql, Class<T> clazz, Object... args);

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
    <T> List<T> selectList(String sql, Class<T> clazz, Object... args);

    /**
     * 返回List<Map>
     *
     * @author bokun.li
     * @date 2025-07-31
     */
    List<Map<String, Object>> selectListMap(String sql, Object... args);


    /**
     * 全部查询
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> selectAll(Class<T> clazz, String... fieldNames);

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
    <T> List<T> selectListByPage(Page<T> page, QueryFilter filter, Class<T> clazz, String sql, Object... args);

    /**
     * 根据主键查询某对象
     *
     * @param <T>
     * @param arg
     * @param clazz
     * @return
     * @throws SQLException
     */
    <T> T selectByPrimaryKey(Object arg, Class<T> clazz);

    /**
     * 根据多个主键查询对象集合
     *
     * @param <T>
     * @param args
     * @param clazz
     * @return
     * @throws SQLException
     */
    <T> List<T> selectByPrimaryKeys(List<Object> args, Class<T> clazz);

    <T> List<T> selectByPrimaryKeysT(List<T> args, Class<T> clazz);

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
     * @param dict
     * @return
     */
    long countByMap(Dict dict, Class<?> aClass);


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
    <T> int deleteByPrimaryKey(T object, Class<T> tClass);


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
    <T> List<T> selectByObject(T object, Class<T> tClass);

    /**
     * 根据MAP条件查询
     *
     * @param dict
     * @param tClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> selectByMap(Dict dict, Class<T> tClass);

    /**
     * 根据条件查询单个
     *
     * @param dict
     * @param tClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T selectOneByMap(Dict dict, Class<T> tClass);


    <T> boolean existByPrimaryKey(Object object, Class<T> tClass);


    // Condition
    long countByCondition(WhereBuild whereBuilder, Class<?> aClass);

    <T> int deleteByCondition(WhereBuild whereBuilder, Class<T> tClass);

    /**
     * 根据条件查询
     *
     * @author bokun.li
     * @date 2025-05-31 17:52:27
     */
    <T> List<T> selectByCondition(WhereBuild whereBuilder, Class<T> tClass);

    /**
     * 根据条件更新
     *
     * @author bokun.li
     * @date 2025-05-31 17:52:27
     */
    <T> int updateByCondition(WhereBuild whereBuilder, T update, Class<T> tClass);

    /**
     * 根据条件查询是否存在
     *
     * @author bokun.li
     * @date 2025-05-31 17:52:27
     */
    <T> boolean existByCondition(WhereBuild whereBuilder, Class<T> tClass);


}
