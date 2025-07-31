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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.StatementUtil;
import cn.hutool.db.sql.Wrapper;
import com.google.common.collect.Maps;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.condition.LogicOperator;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AbstractDBAccess
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public abstract class AbstractDBAccess extends CommonDBAccess implements DBAccess {


    public abstract int saveOrUpdate(Map<String, Object> map);

    public abstract DataSource getDataSource();

    public static final String SYS_LOG_RECORD = "sys_log_record";


    // sql字符串
    protected static final String KEY_SQL = "SQL";
    protected static final String KEY_ARGS = "ARGS";
    protected static final String INDEX_COLUMN = "INDEX_COLUMN";
    protected static final String CONNECTION = "CONNECTION";
    // 字段类型
    protected static final String KEY_TYPE = "TYPE";

    // 实体类型
    protected static final String KEY_ENTITY = "ENTITY";

    // 保存或更新的标识
    protected static final String KEY_SU = "SU";
    // 保存
    protected static final String SAVE = "SAVE";
    protected static final String QUERY_COUNT = "QUERY_COUNT";
    protected static final String BATCH_SAVE = "BATCH_SAVE";
    // 更新
    protected static final String BATCH_UPDATE = "BATCH_UPDATE";


    public List<String> getColumns(Class<?> aClass, String sqlType) {
        List<String> nameList = ListTs.newArrayList();
        //Wrapper wrapper = dialect.getWrapper();
        Map<String, PropertyDescriptor> propertyDescriptorMap = BeanUtil.getPropertyDescriptorMap(aClass, true);
        for (Map.Entry<String, PropertyDescriptor> stringPropertyDescriptorEntry : propertyDescriptorMap.entrySet()) {
            PropertyDescriptor value = stringPropertyDescriptorEntry.getValue();
            Method writeMethod = value.getWriteMethod();
            Method getReadMethod = value.getReadMethod();
            if (null != writeMethod && null != getReadMethod) {
                String name = value.getName();
                Field field = ReflectUtil.getField(aClass, name);
                if (skipColumn(field)) {
                    continue;
                }
                JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
                if (null != annotation) {
                    if (SAVE.equals(sqlType)) {
                        if (!(annotation.isPrimaryKey() && annotation.autoIncrement())) {
                            String name1 = annotation.name();
                            if (StrUtil.isNotBlank(name1)) {
                                nameList.add(name1);
                            } else {
                                nameList.add(name);
                            }
                        }
                    } else if (UPDATE.equals(sqlType)) {
                        nameList.add(name);
                    }
                } else {
                    nameList.add(name);
                }
            }
        }
        return nameList;
    }

    /**
     * 获取带占位符的sql
     *
     * @param recordMap    要拼接的实体bean
     * @param newArgsList  占位符所对于的参数会放到这个集合后面
     * @param isThrowError 如果参数为空那么是否会抛出异常
     * @return
     * @throws SQLException
     */
    public String getSqlByObject(Map<String, Object> recordMap, List<Object> newArgsList, boolean isThrowError, Dialect dialect) {
        Wrapper wrapper = dialect.getWrapper();
        return getSelectByMap(recordMap, newArgsList, wrapper, isThrowError);
    }

    /**
     * 从map中获取条件 并将占位符中的值按顺序放到参数集合中去
     * 得到类似于下面这种的字符串:
     * id = ? and name = ? and grade in (?,?) and age > ? and content like ?
     *
     * @param recordMap
     * @param _newArgsList
     * @param wrapper
     * @return
     * @throws SQLException
     */
    public static String getSelectByMap(Map<String, Object> recordMap, List<Object> _newArgsList, Wrapper wrapper, boolean isThrowError) {
        Set<String> keySet = recordMap.keySet();
        List<Object> newArgsList = _newArgsList == null ? ListTs.newArrayList() : _newArgsList;
        String collect = keySet.stream().map(e -> {
                    Object o = recordMap.get(e);
                    e = StrUtil.toUnderlineCase(e);
                    String wrapField = wrapper.wrap(e);
                    List<Object> map = ListTs.objectToListObject(o, (object) -> object);
                    if (map.isEmpty()) {
                        return "";
                    }
                    if (map.size() > 1) {
                        newArgsList.addAll(map);
                        return wrapField + " in (" + map.stream().map(e2 -> "?").collect(Collectors.joining(",")) + ")";
                    } else {
                        Object o2 = map.get(0);
                        if (o2.getClass() == String.class) {
                            String o1 = StrUtil.trim((String) o2);
                            if (o1.startsWith("%") || o1.endsWith("%")) {
                                return wrapField + " like ?";
                            } else if (o1.startsWith("<")) {
                                newArgsList.add(o1.substring(1));
                                return wrapField + " < ?";
                            } else if (o1.startsWith(">")) {
                                newArgsList.add(o1.substring(1));
                                return wrapField + " > ?";
                            } else if (o1.startsWith("<>")) {
                                newArgsList.add(o1.substring(2));
                                return wrapField + " <> ?";
                            } else if (o1.startsWith("!=")) {
                                newArgsList.add(o1.substring(2));
                                return wrapField + " != ?";
                            } else if (StrUtil.startWithIgnoreCase(o1, "is not null")) {
                                return wrapField + " is not null";
                            } else if (StrUtil.startWithIgnoreCase(o1, "is null")) {
                                return wrapField + " is null";
                            }
                        } else if (o2 instanceof Iterable) {
                            Iterable<?> o21 = (Iterable<?>) o2;
                            Iterator<?> iterator = o21.iterator();
                            List<Object> objects = ListTs.newArrayList();
                            while (iterator.hasNext()) {
                                Object next = iterator.next();
                                if (ObjectUtil.isNotEmpty(next)) {
                                    objects.add(next);
                                }
                            }
                            if (objects.size() == 2) {
                                Object o1 = objects.get(0);
                                newArgsList.add(objects.get(1));
                                return wrapField + " " + o1 + " " + "?";
                            } else if (objects.size() == 3) {
                                Object var1 = objects.get(0);
                                Object var2 = objects.get(1);
                                Object var3 = objects.get(2);
                                newArgsList.add(var3);
                                return wrapper.wrap(var1.toString()) + " " + var2 + " " + "?";
                            }
                        }
                        newArgsList.addAll(map);
                        return wrapField + " = ?";
                    }
                })
                .filter(e -> !StrUtil.isEmpty(e))
                .collect(Collectors.joining(LogicOperator.get(LogicOperator.AND)));
        if (StrUtil.isBlank(StrUtil.trim(collect)) && isThrowError) {
            throw new EasyException("Incomplete SQL where conditions");
        }
        return collect;
    }

    /**
     * 批量插入和批量修改 主键全部由外部搞定
     *
     * @param object
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    public <T> int saveListByBean(List<T> object, Class<T> aClass) {
        if (ObjectUtil.isEmpty(object) || Object.class.getName().equals(object.getClass().getName()) || ObjectUtil.isEmpty(aClass)) {
            return 0;
        }
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);

        dialect.printPrintLog(this.isPrintLog());
        List<Map<String, Object>> collect = object.stream().map(e -> castBeanMap(e, false, false)).collect(Collectors.toList());
        PreparedStatement batchInsertSql = null;

        try {
            batchInsertSql = dialect.psForBatchInsert(
                    getTableName(aClass, dialect),
                    getColumns(aClass, SAVE).toArray(new String[]{}),
                    collect, connection
            );
            return batchInsertSql.executeUpdate();
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("saveListByBean", null, e);

        } finally {
            JdbcHelper.close(batchInsertSql);
            DataSourceUtils.releaseConnection(connection, getDataSource());
        }

    }

    // 查单个
    @Override
    public <T> T selectOne(String sql, Class<T> clazz, Object... args) {
        List<T> query = selectList(sql, clazz, args);
        return JdbcHelper.requiredSingleResult(query);
    }

    // 查多个
    @Override
    public <T> List<T> selectList(String sql, Class<T> clazz, Object... args) {
        Connection connection = getConnection();
        return selectListWith(sql, clazz, args, connection);
    }

    @Override
    public List<Map<String, Object>> selectListMap(String sql, Object... args) {
        Connection connection = getConnection();
        return selectListMapWith(sql, args, connection);
    }

    private <T> List<Map<String, Object>> selectListMapWith(String sql, Object[] args, Connection connection) {
        MapListHandler tBeanListHandler = new MapListHandler();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            logSql(sql, connection, args);
            if (ObjectUtil.isNotEmpty(args)) {
                preparedStatement = StatementUtil.prepareStatement(connection, sql, args);
            } else {
                preparedStatement = StatementUtil.prepareStatement(connection, sql);
            }
            resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> t = tBeanListHandler.handle(resultSet);

            return ObjectUtil.defaultIfNull(t, new ArrayList<>());
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("selectList", sql, e);
        } finally {
            JdbcHelper.close(resultSet);
            JdbcHelper.close(preparedStatement);
            DataSourceUtils.releaseConnection(connection, getDataSource());
        }
    }

    private <T> List<T> selectListWith(String sql, Class<T> clazz, Object[] args, Connection connection) {
        BeanPropertyHandler<T> tBeanListHandler = new BeanPropertyHandler<>(clazz);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            logSql(sql, connection, args);
            if (ObjectUtil.isNotEmpty(args)) {
                preparedStatement = StatementUtil.prepareStatement(connection, sql, args);
            } else {
                preparedStatement = StatementUtil.prepareStatement(connection, sql);
            }
            resultSet = preparedStatement.executeQuery();
            List<T> t = tBeanListHandler.handle(resultSet);

            return ObjectUtil.defaultIfNull(t, new ArrayList<>());
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("selectList", sql, e);
        } finally {
            JdbcHelper.close(resultSet);
            JdbcHelper.close(preparedStatement);
            DataSourceUtils.releaseConnection(connection, getDataSource());
        }
    }


    /**
     * 查询指定列
     *
     * @return 指定列的结果对象
     */
    public Object query(Map<String, Object> map) {
        Connection connection = Convert.convert(Connection.class, map.get(CONNECTION));
        if (connection == null) {
            connection = getConnection();
        }
        String sql = Convert.toStr(map.get(KEY_SQL));
        Object args = map.get(KEY_ARGS);
        Integer column = Convert.toInt(map.get(INDEX_COLUMN));
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            logSql(sql, connection, args);
            ScalarHandler<Object> objectScalarHandler = new ScalarHandler<>(column);
            if (ObjectUtil.isNotEmpty(args)) {
                preparedStatement = StatementUtil.prepareStatement(connection, sql, (Object[]) args);
            } else {
                preparedStatement = StatementUtil.prepareStatement(connection, sql);
            }
            resultSet = preparedStatement.executeQuery();
            return objectScalarHandler.handle(resultSet);
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("query", sql, e);
        } finally {
            JdbcHelper.close(resultSet);
            JdbcHelper.close(preparedStatement);
            DataSourceUtils.releaseConnection(connection, getDataSource());
        }
    }


    protected Object queryCount(Map<String, Object> map) {
        map.put(INDEX_COLUMN, 1);
        return query(map);
    }


    /**
     * 批量修改
     *
     * @param object           要修改的数据bean集合
     * @param columns          要修改的参数是哪些 如果不传那么就是所有
     * @param updateCondition  更新的条件
     * @param updateIgnoreNull 是否更新空值
     * @param aClass
     * @param <T>
     * @return
     * @throws SQLException
     */
    public <T> int updateListByBean(List<T> object, List<String> columns, Map<String, Object> updateCondition, boolean updateIgnoreNull, Class<T> aClass) {
        if (ObjectUtil.isEmpty(object) || ObjectUtil.isEmpty(aClass)) {
            return 0;
        }
        List<Map<String, Object>> collect = object.stream().map(e -> castBeanMap(e, false, false)).collect(Collectors.toList());

        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);

        // 获取参数列表就不考虑sql注入了
        if (CollUtil.isEmpty(columns)) {
            columns = getColumns(aClass, UPDATE);
        }
        PreparedStatement batchInsertSql = null;
        try {
            dialect.printPrintLog(this.isPrintLog());
            batchInsertSql = dialect.psForBatchUpdate(
                    getTableName(aClass, dialect),
                    columns.toArray(new String[]{}),
                    collect, updateCondition, updateIgnoreNull, connection
            );

            return batchInsertSql.executeUpdate();
        } catch (SQLException sqlException) {
            throw JdbcHelper.translateSqlException("updateListByBean", null, sqlException);
        } finally {
            JdbcHelper.close(batchInsertSql);
            DataSourceUtils.releaseConnection(connection, getDataSource());
        }

    }

    public Map<String, Object> buildMap(Connection connection, String sql, String su, Object[] args) {
        Map<String, Object> pMap = Maps.newHashMap();
        pMap.put(KEY_SQL, sql);
        pMap.put(KEY_SU, su);
        pMap.put(KEY_ARGS, args);
        pMap.put(CONNECTION, connection);
        return pMap;
    }

    @Override
    public <T> int saveOne(T record, Class<T> aClass) {
        return saveListByBean(ListTs.singletonList(record), aClass);
    }

    @Override
    public <T> int saveList(List<T> record, Class<T> aClass) {
        return saveListByBean(record, aClass);
    }

    public <T> List<Object> getIdValue(T record, Class<T> aclass) {
        List<String> idNames = this.getIdNames(aclass);
        if (idNames.isEmpty()) {
            throw new EasyException("There must be a primary key");
        }
        List<Object> newArgsList = ListTs.newLinkedList();
        for (String idName : idNames) {
            Object fieldValue = ReflectUtil.getFieldValue(record, idName);
            if (!ObjectUtil.isEmpty(fieldValue)) {
                newArgsList.add(fieldValue);
                break;
            }
        }
        return newArgsList;
    }

    @Override
    public <T> T updateByPrimaryKey(T beanObject, Class<T> aClass, boolean isQuery) {
        Map<String, Object> idMap = getIdMap(beanObject, true);
        int i = updateListByBean(ListTs.singletonList(beanObject), null, idMap, false, aClass);
        if (i > 0 && isQuery) {
            List<Object> idValue = getIdValue(beanObject, aClass);
            return selectByPrimaryKey(idValue.get(0), aClass);
        }
        return null;
    }

    @Override
    public <T> int saveOrUpdateByPrimaryKey(T beanObject, Class<T> aClass) {
        T objectByPrimaryKey = selectByPrimaryKey(beanObject, aClass);
        if (Objects.nonNull(objectByPrimaryKey)) {
            Map<String, Object> idMap = getIdMap(beanObject, true);
            return updateListByBean(ListTs.singletonList(beanObject), null, idMap, false, aClass);
        } else {
            return saveOne(beanObject, aClass);
        }
    }

    @Override
    public <T> T updateByPrimaryKeySelective(T logRecord, Class<T> aClass, boolean isQuery) {
        Map<String, Object> idMap = getIdMap(logRecord, true);
        int i = updateListByBean(ListTs.singletonList(logRecord), null, idMap, true, aClass);
        if (i > 0) {
            List<Object> idValue = getIdValue(logRecord, aClass);
            return selectByPrimaryKey(idValue.get(0), aClass);
        }
        return null;
    }

    @Override
    public <T> int updateListByPrimaryKey(List<T> objectList, Class<T> aClass) {
        Map<String, Object> idMap = getIdMap(objectList, true);
        return updateListByBean(objectList, null, idMap, false, aClass);
    }

    @Override
    public <T> int updateListByPrimaryKeySelective(List<T> objectList, Class<T> tClass) {
        Map<String, Object> idMap = getIdMap(objectList, true);
        return updateListByBean(objectList, null, idMap, true, tClass);
    }

    // 分页查询实现
    public <T> List<T> selectListByPage(Page<T> page, QueryFilter filter, Class<T> clazz, String sql, Object... args) {
        String orderby = JdbcHelper.buildPageOrder(filter.getOrder(), filter.getOrderBy());
        String querySQL = sql + orderby;
        if (page == null) {
            return selectList(sql, clazz, args);
        }
        // 去除order by 子句
        final int orderByIndex = StrUtil.lastIndexOfIgnoreCase(sql, " order by");
        if (orderByIndex > 0) {
            sql = StrUtil.subPre(sql, orderByIndex);
        }
        String countSQL = DDlLine(SELECT, "(" + sql + ") c", null, "count(1)");
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);

        dialect.printPrintLog(this.isPrintLog());
        querySQL = dialect.getPageSql(querySQL, page);
//        if (log.isDebugEnabled()) {
//            log.debug("查询分页countSQL=\n" + countSQL);
//            log.debug("查询分页querySQL=\n" + querySQL);
//        }
        try {
            Map<String, Object> stringObjectMap = buildMap(connection, countSQL, QUERY_COUNT, args);
            Object count = queryCount(stringObjectMap);
            List<T> list = selectList(querySQL, clazz, args);
            if (list == null) list = Collections.emptyList();
            page.setResult(list);
            page.setTotalCount(Convert.toLong(count));
            return list;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // 只能传值进来
    @Override
    public <T> T selectByPrimaryKey(Object arg, Class<T> clazz) {
        List<T> objectByIdList = selectByPrimaryKeys(ListTs.asList(arg), clazz);
        if (ListTs.isEmpty(objectByIdList)) {
            return null;
        } else {
            return objectByIdList.get(0);
        }
    }

    @Override
    public <T> List<T> selectAll(Class<T> clazz, String... fieldNames) {
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);
        return selectListWith(DDlLine(SELECT, this.getTableName(clazz, dialect), null, fieldNames), clazz, null, connection);
    }

    @Override
    public <T> int deleteAll(Class<T> tClass) {

        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);

        String tableName = this.getTableName(tClass, dialect);
        String sql = DDlLine(DELETE, tableName, null);
        Map<String, Object> stringObjectMap = buildMap(connection, sql, DELETE, null);
        return this.saveOrUpdate(stringObjectMap);
    }

    @Override
    public <T> int deleteByPrimaryKey(T object, Class<T> tClass) {
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);

        String tableName = this.getTableName(tClass, dialect);
        Map<String, Object> idMap = this.getIdMap(object, true);
        List<Object> args = ListTs.newLinkedList();
        String sqlByObject = this.getSqlByObject(idMap, args, true, dialect);
        String sql = DDlLine(DELETE, tableName, where(sqlByObject));
        Map<String, Object> stringObjectMap = buildMap(connection, sql, DELETE, args.toArray(new Object[]{}));
        return this.saveOrUpdate(stringObjectMap);
    }


    @Override
    public <T> int deleteByMap(Dict dict, Class<T> tClass) {
        if (dict.isEmpty()) {
            throw new EasyException(BusCode.A00038);
        }
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);
        String tableName = this.getTableName(tClass, dialect);
        List<Object> args = ListTs.newLinkedList();
        String sqlByObject = this.getSqlByObject(dict, args, true, dialect);
        String sql = DDlLine(DELETE, tableName, where(sqlByObject));
        Map<String, Object> stringObjectMap = buildMap(connection, sql, DELETE, args.toArray(new Object[]{}));
        return this.saveOrUpdate(stringObjectMap);
    }

    @Override
    public <T> List<T> selectByObject(T recordData, Class<T> tClass) {
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);
        String tableName = this.getTableName(tClass, dialect);
        Map<String, Object> paramMap = castBeanMap(recordData, true, false);
        List<Object> args = ListTs.newLinkedList();
        String sqlByObject = this.getSqlByObject(paramMap, args, true, dialect);
        String sql = DDlLine(SELECT, tableName, where(sqlByObject));
        return this.selectListWith(sql, tClass, args.toArray(new Object[]{}), connection);
    }

    @Override
    public <T> List<T> selectByMap(Dict params, Class<T> tClass) {
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);
        String tableName = this.getTableName(tClass, dialect);
        List<Object> args = ListTs.newLinkedList();
        String sqlByObject = this.getSqlByObject(params, args, true, dialect);
        String sql = DDlLine(SELECT, tableName, where(sqlByObject));
        return this.selectListWith(sql, tClass, args.toArray(new Object[]{}), connection);
    }

    @Override
    public <T> T selectOneByMap(Dict dict, Class<T> tClass) {
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);
        String tableName = this.getTableName(tClass, dialect);
        List<Object> args = ListTs.newLinkedList();
        String sqlByObject = this.getSqlByObject(dict, args, true, dialect);
        String sql = DDlLine(SELECT, tableName, where(sqlByObject));
        return ListTs.get(this.selectListWith(sql, tClass, args.toArray(new Object[]{}), connection), 0);
    }

    @Override
    public long countBy(Object object) {
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);
        Class<?> aClass1 = object.getClass();
        String tableName = getTableName(aClass1, dialect);
        if (StrUtil.isNotBlank(tableName)) {
            Map<String, Object> stringObjectMap = castBeanMap(object, true, false);
            return conditionMap(stringObjectMap, tableName, connection, dialect);
        }
        return 0;
    }

    @Override
    public long countByMap(Dict dict, Class<?> aClass) {
        if (null == aClass || dict == null) {
            throw new EasyException("dict or aClass is null,is not allow!");
        }
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);
        String tableName = getTableName(aClass, dialect);
        if (StrUtil.isNotBlank(tableName)) {
            return conditionMap(dict, tableName, connection, dialect);
        }
        return 0;
    }

    private long conditionMap(Map<String, Object> object, String tableName, Connection connection, Dialect dialect) {
        List<Object> newArgsList = ListTs.newLinkedList();
        String sqlByObject = getSqlByObject(object, newArgsList, false, dialect);
        String sql = DDlLine(SELECT, tableName, where(sqlByObject), "count(*)");
        Map<String, Object> buildMap = buildMap(connection, sql, QUERY_COUNT, newArgsList.toArray(new Object[]{}));
        return (long) queryCount(buildMap);
    }


    @Override
    public <T> List<T> selectByPrimaryKeys(List<Object> args, Class<T> clazz) {
        return selectByPrimaryKeysWith(args, clazz);
    }

    private <T> List<T> selectByPrimaryKeysWith(List<Object> args, Class<T> clazz) {
        if (args.isEmpty()) {
            throw new EasyException("id list is empty");
        } else {
            if (!BeanUtil.isBean(clazz)) {
                throw new EasyException("Please pass in the bytecode object of the java bean");
            }
            List<String> idNames = this.getIdNames(clazz);
            if (idNames.isEmpty()) {
                throw new EasyException("There must be a primary key");
            } else if (idNames.size() > 1) {
                throw new EasyException("There cannot be multiple primary keys：" + clazz.getName());
            }
            Map<String, Object> idMap = this.getIdMap(args, false);
            if (idMap.isEmpty()) {
                idMap.put(idNames.get(0), args);
            }

            Connection connection = getConnection();
            Dialect dialect = JdbcHelper.getDialect(connection);

            List<Object> newArgsList = ListTs.newLinkedList();

            String sql = DDlLine(
                    SELECT,
                    getTableName(clazz, dialect),
                    where(
                            getSqlByObject(idMap, newArgsList, true, dialect)
                    )
            );
            return selectListWith(sql, clazz, newArgsList.toArray(new Object[]{}), connection);
        }
    }

    @Override
    public <T> List<T> selectByPrimaryKeysT(List<T> args, Class<T> clazz) {
        List<Object> map = ListTs.objectToListObject(args, e -> e);
        return selectByPrimaryKeysWith(map, clazz);
    }

    @Override
    public <T> boolean existByPrimaryKey(Object object, Class<T> tClass) {
        if (Objects.isNull(object)) {
            throw new EasyException("PrimaryKey is not allow null");
        }
        List<Object> objects = ListTs.newArrayList();
        List<String> idNames = this.getIdNames(tClass);
        if (idNames.isEmpty()) {
            throw new EasyException("There must be a primary key");
        } else if (idNames.size() > 1) {
            throw new EasyException("There cannot be multiple primary keys：" + tClass.getName());
        }
        Map<String, Object> idMap = this.getIdMap(object, false);
        if (idMap.isEmpty()) {
            idMap.put(idNames.get(0), ListTs.asList(object));
        }
        Connection connection = getConnection();
        Dialect dialect = JdbcHelper.getDialect(connection);
        String sqlByObject = getSqlByObject(idMap, objects, true, dialect);
        String s = DDlLine(SELECT, getTableName(tClass, dialect), where(sqlByObject), idNames.toArray(new String[]{}));
        List<T> objectList = selectListWith(s, tClass, objects.toArray(new Object[]{}), connection);
        return CollUtil.isNotEmpty(objectList);
    }

    @Override
    public <T> int deleteByCondition(WhereBuild whereBuilder, Class<T> tClass) {
        Connection connection = getConnection();
        whereBuilder.bind(connection);
        List<Object> objects = new ArrayList<>();
        Dialect dialect = JdbcHelper.getDialect(connection);
        String tableName = getTableName(tClass, dialect);
        String sql = whereBuilder.build(objects);
        sql = DDlLine(DELETE, tableName, where(sql));
        return saveOrUpdate(buildMap(connection, sql, DELETE, objects.toArray(new Object[]{})));
    }

    @Override
    public long countByCondition(WhereBuild whereBuilder, Class<?> aClass) {

        Connection connection = getConnection();
        whereBuilder.bind(connection);
        List<Object> objects = new ArrayList<>();
        Dialect dialect = JdbcHelper.getDialect(connection);
        String tableName = getTableName(aClass, dialect);
        String sql = whereBuilder.build(objects);
        sql = DDlLine(SELECT, tableName, where(sql), "count(1)");
        Map<String, Object> map = buildMap(connection, sql, QUERY_COUNT, objects.toArray(new Object[]{}));
        return ((long) queryCount(map));
    }

    @Override
    public <T> List<T> selectByCondition(WhereBuild whereBuilder, Class<T> tClass) {
        Connection connection = getConnection();
        whereBuilder.bind(connection);
        List<Object> newArgList = ListTs.newArrayList();

        List<String> selectFields = whereBuilder.getSelectFields();
        String[] array = selectFields.toArray(new String[]{});


        Dialect dialect = JdbcHelper.getDialect(connection);
        String build = whereBuilder.build(newArgList);
        if (StrUtil.isBlank(build)) {
            return selectListWith(DDlLine(SELECT, this.getTableName(tClass, dialect), null), tClass, array, connection);
        } else {
            String sql = DDlLine(
                    SELECT,
                    getTableName(tClass, dialect),
                    where(build),
                    array
            );
            return selectListWith(sql, tClass, newArgList.toArray(new Object[]{}), connection);
        }
    }

    @Override
    public <T> int updateByCondition(WhereBuild whereBuilder, T update, Class<T> tClass) {

        Connection connection = getConnection();
        whereBuilder.bind(connection);

        List<Object> newArgList = ListTs.newArrayList();

        String build = whereBuilder.build(newArgList);
        if (StrUtil.isBlank(build)) {
            throw new EasyException("please input update conditions!");
        }
        Dialect dialect = JdbcHelper.getDialect(connection);
        String tableName = getTableName(tClass, dialect);
        Map<String, Object> stringObjectMap = castBeanMap(update, true, true);

        dialect.printPrintLog(this.isPrintLog());
        PreparedStatement preparedStatement = dialect.psForUpdateBySqlBuildStr(tableName, stringObjectMap, build, newArgList, true, connection);
        int effectRows;
        try {
            effectRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("updateByCondition", build, e);
        } finally {
            JdbcHelper.close(preparedStatement);
            DataSourceUtils.releaseConnection(connection, getDataSource());
        }
        return effectRows;
    }

    @Override
    public <T> boolean existByCondition(WhereBuild whereBuilder, Class<T> tClass) {
        return this.countByCondition(whereBuilder, tClass) > 0;
    }

    @Override
    public void printPrintLog(boolean isPrintLog) {
        this.setPrintLog(isPrintLog);
    }
}
