package easy4j.module.base.plugin.dbaccess;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import com.google.common.collect.Maps;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcColumn;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcTable;
import easy4j.module.base.plugin.dbaccess.dialect.Dialect;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.utils.ListTs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static easy4j.module.base.plugin.dbaccess.helper.JdbcHelper.getDialect;

@Slf4j
public abstract class AbstractDBAccess implements DBAccess {

    public static final String SYS_LOG_RECORD = "sys_log_record";


    // sql字符串
    protected static final String KEY_SQL = "SQL";
    protected static final String KEY_ARGS = "ARGS";
    // 字段类型
    protected static final String KEY_TYPE = "TYPE";

    // 实体类型
    protected static final String KEY_ENTITY = "ENTITY";

    // 保存或更新的标识
    protected static final String KEY_SU = "SU";
    // 保存
    protected static final String SAVE = "SAVE";
    protected static final String BATCH_SAVE = "BATCH_SAVE";
    // 更新
    protected static final String UPDATE = "UPDATE";
    protected static final String BATCH_UPDATE = "BATCH_UPDATE";
    protected static final String DELETE = "DELETE";

    public String getTableName(Class<?> clazz, Dialect dialect) throws SQLException {
        StringBuilder sb = new StringBuilder();
        if (dialect == null) {
            dialect = JdbcHelper.getDialect(getConnection());
        }
        assert dialect != null;
        Wrapper wrapper = dialect.getWrapper();
        JdbcTable annotation = clazz.getAnnotation(JdbcTable.class);
        if (null != annotation && StrUtil.isNotBlank(annotation.name())) {
            String schema = StrUtil.blankToDefault(annotation.schema(), "");
            List<String> list = ListTs.filter(ListTs.asList(wrapper.wrap(schema), wrapper.wrap(annotation.name())), StrUtil::isNotBlank);
            String join = String.join(".", list);
            sb.append(join);
        } else {
            String simpleName = clazz.getSimpleName();
            String underlineCase = wrapper.wrap(StrUtil.toUnderlineCase(simpleName)).toLowerCase();
            sb.append(underlineCase);
        }
        return sb.toString();
    }

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

    public Map<String, Object> getIdMap(Object object_) {

        Map<String, Object> map = Maps.newHashMap();
        ListTs.loop(object_, (object) -> {
            Field[] fields = ReflectUtil.getFields(object.getClass());
            for (Field field : fields) {
                JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
                if (Objects.nonNull(annotation) && annotation.isPrimaryKey()) {
                    String name = field.getName();
                    Object fieldValue = ReflectUtil.getFieldValue(object, field);
                    if (map.containsKey(name)) {
                        Object originObject = map.get(name);
                        List<Object> originObject1 = (List<Object>) originObject;
                        originObject1.add(fieldValue);

                    } else {
                        map.put(name, ListTs.asList(fieldValue));
                    }
                }
            }
//            if (map.isEmpty()) {
//                log.warn("{} has no specify primary key,default set to id", object.getClass().getName());
//                Object fieldValue = ReflectUtil.getFieldValue(object, "id");
//                if (Objects.nonNull(fieldValue)) {
//                    if (map.containsKey("id")) {
//                        Object originObject = map.get("id");
//                        List<Object> originObject1 = (List<Object>) originObject;
//                        originObject1.add(fieldValue);
//                    } else {
//                        map.put("id", ListTs.asList(fieldValue));
//                    }
//                }
//            }
        });

        return map;

    }

    public List<String> getIdNames(Class<?> aclass) {
        Field[] fields = ReflectUtil.getFields(aclass, field -> {
            if (field.isAnnotationPresent(JdbcColumn.class)) {
                JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
                return annotation.isPrimaryKey();
            }
            return false;
        });
        return Arrays.stream(fields).map(e -> {
            if (e.isAnnotationPresent(JdbcColumn.class)) {
                JdbcColumn annotation = e.getAnnotation(JdbcColumn.class);
                String name = annotation.name();
                if (StrUtil.isNotBlank(name)) {
                    return name;
                }
            }
            return e.getName();
        }).collect(Collectors.toList());

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
    public String getSqlByObject(Map<String, Object> recordMap, List<Object> newArgsList, boolean isThrowError) throws SQLException {
        Dialect dialect = JdbcHelper.getDialect(getConnection());
        assert dialect != null;
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
    public static String getSelectByMap(Map<String, Object> recordMap, List<Object> _newArgsList, Wrapper wrapper, boolean isThrowError) throws SQLException {
        Set<String> keySet = recordMap.keySet();
        List<Object> newArgsList = _newArgsList == null ? ListTs.newArrayList() : _newArgsList;
        String collect = keySet.stream().map(e -> {
                    Object o = recordMap.get(e);
                    e = StrUtil.toUnderlineCase(e);
                    List<Object> map = ListTs.map(o, (object) -> object);
                    if (map.isEmpty()) {
                        return "";
                    }
                    if (map.size() > 1) {
                        newArgsList.addAll(map);
                        return wrapper.wrap(e) + " in (" + map.stream().map(e2 -> "?").collect(Collectors.joining(",")) + ")";
                    } else {
                        Object o2 = map.get(0);
                        if (o2.getClass() == String.class) {
                            String o1 = StrUtil.trim((String) o2);
                            if (o1.startsWith("%") || o1.endsWith("%")) {
                                return wrapper.wrap(e) + " like ?";
                            } else if (o1.startsWith("<")) {
                                newArgsList.add(o1.substring(1));
                                return wrapper.wrap(e) + " < ?";
                            } else if (o1.startsWith(">")) {
                                newArgsList.add(o1.substring(1));
                                return wrapper.wrap(e) + " > ?";
                            } else if (o1.startsWith("<>")) {
                                newArgsList.add(o1.substring(2));
                                return wrapper.wrap(e) + " <> ?";
                            } else if (o1.startsWith("!=")) {
                                newArgsList.add(o1.substring(2));
                                return wrapper.wrap(e) + " != ?";
                            } else if (StrUtil.startWithIgnoreCase(o1, "is not null")) {
                                return wrapper.wrap(e) + " is not null";
                            } else if (StrUtil.startWithIgnoreCase(o1, "is null")) {
                                return wrapper.wrap(e) + " is null";
                            }
                        }
                        newArgsList.addAll(map);
                        return wrapper.wrap(e) + " = ?";
                    }
                })
                .filter(e -> !StrUtil.isEmpty(e))
                .collect(Collectors.joining(" and "));
        if (StrUtil.isBlank(StrUtil.trim(collect)) && isThrowError) {
            throw new SQLException("Incomplete SQL where conditions");
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
    public <T> int saveListByBean(List<T> object, Class<T> aClass) throws SQLException {
        if (ObjectUtil.isEmpty(object) || Object.class.getName().equals(object.getClass().getName()) || ObjectUtil.isEmpty(aClass)) {
            return 0;
        }
        Connection connection = getConnection();
        Dialect dialect = getDialect(connection);
        assert dialect != null;
        List<Map<String, Object>> collect = object.stream().map(e -> BeanUtil.beanToMap(e, false, false)).collect(Collectors.toList());
        PreparedStatement batchInsertSql = dialect.psForBatchInsert(
                getTableName(aClass, dialect),
                getColumns(aClass, SAVE).toArray(new String[]{}),
                collect, connection
        );
        int i = batchInsertSql.executeUpdate();
        DbUtils.close(batchInsertSql);

        return i;
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
    public <T> int updateListByBean(List<T> object, List<String> columns, Map<String, Object> updateCondition, boolean updateIgnoreNull, Class<T> aClass) throws SQLException {
        if (ObjectUtil.isEmpty(object) || Object.class.getName().equals(object.getClass().getName()) || ObjectUtil.isEmpty(aClass)) {
            return 0;
        }
        List<Map<String, Object>> collect = object.stream().map(e -> BeanUtil.beanToMap(e, false, false)).collect(Collectors.toList());

        Connection connection = getConnection();
        Dialect dialect = getDialect(connection);
        assert dialect != null;
        if (CollUtil.isEmpty(columns)) {
            columns = getColumns(aClass, UPDATE);
        }
        PreparedStatement batchInsertSql = dialect.psForBatchUpdate(
                getTableName(aClass, dialect),
                columns.toArray(new String[]{}),
                collect, updateCondition, updateIgnoreNull, connection
        );
        int i = batchInsertSql.executeUpdate();
        DbUtils.close(batchInsertSql);

        return i;
    }

    public abstract int saveOrUpdate(Map<String, Object> map) throws SQLException;


    /**
     * 分页查询时，符合条件的总记录数
     *
     * @param sql  sql语句
     * @param args 参数数组
     * @return 总记录数
     */
    protected abstract Object queryCount(String sql, Object... args) throws SQLException;

    public Map<String, Object> buildMap(String sql, String su, Object[] args) {
        Map<String, Object> pMap = Maps.newHashMap();
        pMap.put(KEY_SQL, sql);
        pMap.put(KEY_SU, su);
        pMap.put(KEY_ARGS, args);
        return pMap;
    }

    @Override
    public void init(Object object) {

    }

    @Override
    public <T> int saveOne(T record, Class<T> aClass) throws SQLException {
        return saveListByBean(ListTs.singletonList(record), aClass);
    }

    @Override
    public <T> int saveList(List<T> record, Class<T> aClass) throws SQLException {
        return saveListByBean(record, aClass);
    }

    public <T> List<Object> getIdValue(T record, Class<T> aclass) throws SQLException {
        List<String> idNames = this.getIdNames(aclass);
        if (idNames.isEmpty()) {
            throw new SQLException("There must be a primary key");
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
    public <T> T updateByPrimaryKey(T logRecord, Class<T> aClass) throws SQLException {
        Map<String, Object> idMap = getIdMap(logRecord);
        int i = updateListByBean(ListTs.singletonList(logRecord), null, idMap, false, aClass);
        if (i > 0) {
            List<Object> idValue = getIdValue(logRecord, aClass);
            return getObjectByPrimaryKey(idValue.get(0), aClass);
        }
        return null;
    }

    @Override
    public <T> T updateByPrimaryKeySelective(T logRecord, Class<T> aClass) throws SQLException {
        Map<String, Object> idMap = getIdMap(logRecord);
        int i = updateListByBean(ListTs.singletonList(logRecord), null, idMap, true, aClass);
        if (i > 0) {
            List<Object> idValue = getIdValue(logRecord, aClass);
            return getObjectByPrimaryKey(idValue.get(0), aClass);
        }
        return null;
    }

    @Override
    public <T> int updateListByPrimaryKey(List<T> objectList, Class<T> aClass) throws SQLException {
        Map<String, Object> idMap = getIdMap(objectList);
        return updateListByBean(objectList, null, idMap, false, aClass);
    }

    @Override
    public <T> int updateListByPrimaryKeySelective(List<T> objectList, Class<T> tClass) throws SQLException {
        Map<String, Object> idMap = getIdMap(objectList);
        return updateListByBean(objectList, null, idMap, true, tClass);
    }

    // 分页查询实现
    public <T> List<T> getObjectListByPage(Page<T> page, QueryFilter filter, Class<T> clazz, String sql, Object... args) throws SQLException {
        String orderby = JdbcHelper.buildPageOrder(filter.getOrder(), filter.getOrderBy());
        String querySQL = sql + orderby;
        if (page == null) {
            return getObjectList(sql, clazz, args);
        }
        // 去除order by 子句
        final int orderByIndex = StrUtil.lastIndexOfIgnoreCase(sql, " order by");
        if (orderByIndex > 0) {
            sql = StrUtil.subPre(sql, orderByIndex);
        }
        String countSQL = "select count(1) from (" + sql + ") c ";
        Dialect dialect = getDialect(getConnection());
        assert dialect != null;
        querySQL = dialect.getPageSql(querySQL, page);
        if (log.isDebugEnabled()) {
            log.debug("查询分页countSQL=\n" + countSQL);
            log.debug("查询分页querySQL=\n" + querySQL);
        }
        try {
            Object count = queryCount(countSQL, args);
            List<T> list = getObjectList(querySQL, clazz, args);
            if (list == null) list = Collections.emptyList();
            page.setResult(list);
            page.setTotalCount(Convert.toLong(count));
            return list;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public <T> T getObjectByPrimaryKey(Object arg, Class<T> clazz) throws SQLException {
        List<T> objectByIdList = getObjectByPrimaryKeys(ListTs.asList(arg), clazz);
        if (ListTs.isEmpty(objectByIdList)) {
            return null;
        } else {
            return objectByIdList.get(0);
        }
    }

    @Override
    public <T> List<T> getAll(Class<T> clazz) throws SQLException {
        String tableName = this.getTableName(clazz, null);
        return getObjectList("select * from " + tableName, clazz);
    }

    @Override
    public <T> int deleteAll(Class<T> workIpClass) throws SQLException {

        String tableName = this.getTableName(workIpClass, null);
        String sql = "delete from " + tableName;
        Map<String, Object> stringObjectMap = buildMap(sql, DELETE, null);
        return this.saveOrUpdate(stringObjectMap);
    }

    @Override
    public <T> int deleteByPrimaryKey(Object object, Class<T> easy4jKeyIdempotentClass) throws SQLException {
        String tableName = this.getTableName(easy4jKeyIdempotentClass, null);
        Map<String, Object> idMap = this.getIdMap(object);
        List<Object> args = ListTs.newLinkedList();
        String sqlByObject = this.getSqlByObject(idMap, args, true);
        String sql = "delete from " + tableName + " where " + sqlByObject;
        Map<String, Object> stringObjectMap = buildMap(sql, DELETE, args.toArray(new Object[]{}));
        return this.saveOrUpdate(stringObjectMap);
    }

    @Override
    public <T> List<T> getObjectBy(T localMessage, Class<T> localMessageClass) throws SQLException {

        String tableName = this.getTableName(localMessageClass, null);
        Map<String, Object> paramMap = BeanUtil.beanToMap(localMessage, true, false);
        List<Object> args = ListTs.newLinkedList();
        String sqlByObject = this.getSqlByObject(paramMap, args, true);
        String sql = "select * from " + tableName + " where " + sqlByObject;
        return this.getObjectList(sql, localMessageClass, args.toArray(new Object[]{}));
    }

    @Override
    public long countBy(Object object) throws SQLException {
        Class<?> aClass1 = object.getClass();
        String tableName = getTableName(aClass1, null);
        if (StrUtil.isNotBlank(tableName)) {
            Map<String, Object> stringObjectMap = BeanUtil.beanToMap(object, true, false);
            StringBuilder sql = new StringBuilder("select count(*) from " + tableName);
            List<Object> newArgsList = ListTs.newLinkedList();
            String sqlByObject = getSqlByObject(stringObjectMap, newArgsList, false);
            if (StrUtil.isNotBlank(sqlByObject)) {
                sql.append(" where ");
            }
            sql.append(sqlByObject);
            return (long) queryCount(sql.toString(), newArgsList.toArray(new Object[]{}));
        }
        return 0;
    }


    @Override
    public <T> List<T> getObjectByPrimaryKeys(List<Object> args, Class<T> clazz) throws SQLException {
        if (args.isEmpty()) {
            throw new SQLException("id list is empty");
        } else {
            if (!BeanUtil.isBean(clazz)) {
                throw new SQLException("Please pass in the bytecode object of the java bean");
            }
            List<String> idNames = this.getIdNames(clazz);
            if (idNames.size() > 1) {
                throw new SQLException("There can only be one primary key");
            } else if (idNames.isEmpty()) {
                throw new SQLException("There must be a primary key");
            }
            String s = idNames.get(0);
            Map<String, Object> idMap = Maps.newHashMap();
            idMap.put(s, args);
            Dialect dialect = JdbcHelper.getDialect(getConnection());
            assert dialect != null;
            String sql = "select * from " + getTableName(clazz, null) + " where ";
            List<Object> newArgsList = ListTs.newLinkedList();
            sql += getSqlByObject(idMap, newArgsList, true);
            return getObjectList(sql, clazz, newArgsList.toArray(new Object[]{}));
        }
    }
}
