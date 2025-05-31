package easy4j.module.base.plugin.dbaccess;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcColumn;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcIgnore;
import easy4j.module.base.plugin.dbaccess.helper.SqlPlaceholderReplacer;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.json.JacksonUtil;
import jodd.util.StringPool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;


public abstract class CommonDBAccess {

    protected static final String UPDATE = "UPDATE";
    protected static final String DELETE = "DELETE";
    protected static final String SELECT = "SELECT";
    protected static final String INSERT = "INSERT";
    protected static final String FROM = "FROM";
    protected static final String INTO = "INTO";
    protected static final String SET = "SET";
    protected static final String VALUES = "VALUES";
    protected static final String VALUE = "VALUE";


    public boolean skipColumn(Field field) {
        int modifiers = field.getModifiers();

        if (
                Modifier.isStatic(modifiers) ||
                        Modifier.isFinal(modifiers) ||
                        Modifier.isTransient(modifiers)
        ) {
            return true;
        }
        return field.isAnnotationPresent(JdbcIgnore.class);
    }

    public String where(String sql) {

        if (StrUtil.isNotBlank(sql)) {
            return " WHERE " + sql;
        }
        return "";

    }

    /**
     * 整合DDL语句 不整合 WHERE 字符
     * 测试用例在
     *
     * @param type      DDL类型 SELECT , UPDATE , DELETE , INSERT
     * @param tableName 表名
     * @param subSql    条件
     * @param fields    要查询,写入,修改,的字段
     * @author bokun.li
     * @date 2025-05-31 16:13:34
     * @see easy4j.module.base.plugin.dbaccess.CommonDBAccessTest#DDlLine()
     * @see CommonDBAccess#where(String)
     * 返回如下
     * <p>
     * SELECT [FIELDS] FROM [TABLENAME] [SUBSQL]
     * <p>
     * UPDATE [TABLENAME] SET [FIELDS] [SUBSQL]
     * <p>
     * DELETE FROM [TABLENAME] [SUBSQL]
     * <p>
     * INSERT INTO [TABLENAME] [FIELDS] [SUBSQL]
     */
    public String DDlLine(String type, String tableName, String subSql, String... fields) {
        if (StrUtil.isBlank(type)) {
            throw new EasyException("please input ddl type!");
        }
        if (StrUtil.isBlank(tableName)) {
            throw new EasyException("please input ddl tableName!");
        }
        List<String> ddlList = ListTs.newLinkedList();
        String upperCase = type.toUpperCase();
        ddlList.add(upperCase);
        switch (upperCase) {
            case DELETE:
                ddlList.add(FROM);
                break;
            case UPDATE:
                break;
            case SELECT:
                if (fields.length > 0) {
                    ddlList.add(String.join(StringPool.COMMA + StringPool.SPACE, ListTs.asList(fields)));
                    ddlList.add(FROM);
                } else {
                    ddlList.add(StringPool.STAR);
                    ddlList.add(FROM);
                }
                break;
            case INSERT:
                ddlList.add(INTO);
                ddlList.add(tableName);
                if (fields.length == 1) {
                    ddlList.add(fields[0]);
                } else if (fields.length > 1) {
                    ddlList.add(StringPool.LEFT_BRACKET + String.join(StringPool.COMMA + StringPool.SPACE, ListTs.asList(fields)) + StringPool.RIGHT_BRACKET);
                }
                break;
            default:
                throw new EasyException("not support ddl type:" + upperCase);
        }
        if (!INSERT.equalsIgnoreCase(upperCase)) {
            ddlList.add(tableName);
        }
        if (UPDATE.equalsIgnoreCase(upperCase)) {
            ddlList.add(SET);
            if (fields.length == 1) {
                ddlList.add(fields[0]);
            } else if (fields.length > 1) {
                ddlList.add(String.join(StringPool.COMMA + StringPool.SPACE, ListTs.asList(fields)));
            }
        }
        ddlList.add(subSql);
        return ddlList.stream().filter(StrUtil::isNotBlank).collect(Collectors.joining(StringPool.SPACE));
    }


    /**
     * 获取主键对应的值
     *
     * @param object_
     * @return
     */
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

    /**
     * 将对象转为beanMap
     *
     * @param object
     * @param isToUnderline 转为下划线
     * @param isIgnoreNull  是否忽略控制
     * @return
     */
    public Map<String, Object> castBeanMap(Object object, boolean isToUnderline, boolean isIgnoreNull) {
        Map<String, Object> resMap = Maps.newHashMap();
        if (null == object) {
            return resMap;
        }

        Class<?> aClass = object.getClass();
        Field[] fields = ReflectUtil.getFields(aClass);

        for (Field field : fields) {
            if (skipColumn(field)) {
                continue;
            }
            String name = field.getName();
            boolean annotationPresent = field.isAnnotationPresent(JdbcColumn.class);
            boolean toJson = false;
            if (annotationPresent) {
                JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
                String name1 = annotation.name();
                name = StrUtil.isBlank(name1) ? name : name1;

                toJson = annotation.toJson();

            }
            if (isToUnderline) {
                name = StrUtil.toUnderlineCase(name);
            }
            Object fieldValue = ReflectUtil.getFieldValue(object, field);
            if (isIgnoreNull) {
                if (ObjectUtil.isEmpty(fieldValue)) {
                    continue;
                }
            }
            // force convert to json text
            if (toJson) {
                fieldValue = JacksonUtil.toJson(fieldValue);
            }
            resMap.put(name, fieldValue);
        }

        return resMap;

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


    public void logSql(String sql, Object... args) {
        try {
            boolean property = Easy4j.getProperty(SysConstant.EASY4J_ENABLE_PRINT_SYS_DB_SQL, boolean.class);
            if (property) {
                List<Object> newArrayList;
                if (ArrayUtil.isNotEmpty(args) && 1 == args.length && args[0] instanceof Collection) {
                    Object arg = args[0];
                    newArrayList = ListTs.map(arg, (object) -> object);
                } else {
                    newArrayList = ListTs.asList(args);
                }
                if (!newArrayList.isEmpty()) {
                    String s = SqlPlaceholderReplacer.replacePlaceholders(sql, newArrayList);
                    Easy4j.info("[SQL] -> {}", s);
                } else {
                    Easy4j.info("[SQL] -> {}", sql);
                }

            }
        } catch (Exception ignored) {
        }
    }

}
