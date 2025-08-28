package easy4j.infra.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.collect.Maps;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcIgnore;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import easy4j.infra.dbaccess.helper.PGHelper;
import easy4j.infra.dbaccess.helper.SqlPlaceholderReplacer;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import jodd.util.StringPool;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;


public class CommonDBAccess {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Getter
    private boolean isPrintLog = false;

    @Getter
    private boolean toUnderline = true;

    public void setPrintLog(boolean printLog) {
        isPrintLog = printLog;
    }

    public void setToUnderline(boolean toUnderline) {
        this.toUnderline = toUnderline;
    }

    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String SELECT = "SELECT";
    public static final String INSERT = "INSERT";
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
        boolean skip = false;
        if (field.isAnnotationPresent(TableField.class)) {
            TableField annotation = field.getAnnotation(TableField.class);
            if (!annotation.exist()) {
                skip = true;
            }
        }
        return field.isAnnotationPresent(JdbcIgnore.class) || field.isAnnotationPresent(Transient.class) || skip;
    }

    public String where(String sql) {



        if (StrUtil.isNotBlank(sql)) {
            String trim = StrUtil.trim(sql);
            if(
                    StrUtil.startWithIgnoreCase(trim,"group by") ||
                    StrUtil.startWithIgnoreCase(trim,"order by") ||
                    StrUtil.startWithIgnoreCase(trim,"having")
            ){
                return sql;
            }
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
    public String DDlLine(String type, String tableName, String subSql, String... _fields) {

        if (StrUtil.isBlank(type)) {
            throw new EasyException("please input ddl type!");
        }
        if (StrUtil.isBlank(tableName)) {
            throw new EasyException("please input ddl tableName!");
        }
        List<String> fields = ListTs.asList(_fields);
        // 更新的时候 字段 形式为 name='xx' 这种所以不需要转下划线
        if (!UPDATE.equalsIgnoreCase(type) && toUnderline) {
            fields = ListTs.objectToListT(_fields, String.class, e -> StrUtil.toUnderlineCase(e.toString()));
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
                if (!fields.isEmpty()) {
                    ddlList.add(
                            String.join(StringPool.COMMA + StringPool.SPACE, fields)
                    );
                    ddlList.add(FROM);
                } else {
                    ddlList.add(StringPool.STAR);
                    ddlList.add(FROM);
                }
                break;
            case INSERT:
                ddlList.add(INTO);
                ddlList.add(tableName);
                if (fields.size() == 1) {
                    ddlList.add(fields.get(0));
                } else if (fields.size() > 1) {
                    ddlList.add(
                            StringPool.LEFT_BRACKET +
                                    String.join(StringPool.COMMA + StringPool.SPACE, fields) +
                                    StringPool.RIGHT_BRACKET
                    );
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
            if (fields.size() == 1) {
                ddlList.add(fields.get(0));
            } else if (fields.size() > 1) {
                ddlList.add(String.join(StringPool.COMMA + StringPool.SPACE, fields));
            }
        }
        ddlList.add(subSql);
        return ddlList.stream().filter(StrUtil::isNotBlank).collect(Collectors.joining(StringPool.SPACE));
    }


    /**
     * 从对象（集合或者单个对象）中 获取主键对应的值
     *
     * @param object_
     * @param emptyThrow
     * @return
     */
    public Map<String, Object> getIdMap(Object object_, boolean emptyThrow) {

        Map<String, Object> map = Maps.newHashMap();
        ListTs.loop(object_, (object) -> {
            if (ObjectUtil.isBasicType(object)) {
                return;
            }
            Field[] fields = ReflectUtil.getFields(object.getClass());
            for (Field field : fields) {
                JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
                // compatible jpa mybatis-plus
                if ((Objects.nonNull(annotation) && annotation.isPrimaryKey()) || field.isAnnotationPresent(TableId.class) || field.isAnnotationPresent(Id.class)) {
                    String name = field.getName();
                    Object fieldValue = ReflectUtil.getFieldValue(object, field);
                    if (ObjectUtil.isNotEmpty(fieldValue)) {
                        if (map.containsKey(name)) {
                            Object originObject = map.get(name);
                            List<Object> originObject1 = (List<Object>) originObject;
                            originObject1.add(fieldValue);

                        } else {
                            map.put(name, ListTs.asList(fieldValue));
                        }
                    }

                }
            }
        });
        if (emptyThrow && map.entrySet()
                .stream()
                .allMatch(e -> StrUtil.isBlank(e.getKey()) || ObjectUtil.isEmpty(e.getValue()))
        ) {
            throw new EasyException("When updating, the primary key condition cannot be empty");
        }

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
            String pgFieldType = null;
            if (annotationPresent) {
                JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
                String name1 = annotation.name();
                name = StrUtil.isBlank(name1) ? name : name1;

                toJson = annotation.toJson();
                pgFieldType = annotation.pgType();
            }else if(field.isAnnotationPresent(TableField.class)){
                TableField tableField = field.getAnnotation(TableField.class);
                String value = tableField.value();
                name = StrUtil.isBlank(value) ? name : value;
            }else if(field.isAnnotationPresent(Column.class)){
                Column tableField = field.getAnnotation(Column.class);
                String value = tableField.name();
                name = StrUtil.isBlank(value) ? name : value;
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
                if (StrUtil.isNotBlank(pgFieldType)) {
                    fieldValue = PGHelper.wrap(pgFieldType, fieldValue);
                }
            } else {
                // handle pgtype vs
                if (StrUtil.isNotBlank(pgFieldType)) {
                    fieldValue = PGHelper.wrap(pgFieldType, fieldValue);
                }
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
            }else return field.isAnnotationPresent(TableId.class) || field.isAnnotationPresent(Id.class);
        });
        return Arrays.stream(fields).map(e -> {
            if (e.isAnnotationPresent(JdbcColumn.class)) {
                JdbcColumn annotation = e.getAnnotation(JdbcColumn.class);
                String name = annotation.name();
                if (StrUtil.isNotBlank(name)) {
                    return name;
                }
            }else if (e.isAnnotationPresent(TableId.class)) {
                TableId annotation = e.getAnnotation(TableId.class);
                String name = annotation.value();
                if (StrUtil.isNotBlank(name)) {
                    return name;
                }
            }else if (e.isAnnotationPresent(Id.class)) {
                return e.getName();
            }
            return e.getName();
        }).collect(Collectors.toList());

    }

    public String getTableName(Class<?> clazz, Dialect dialect) {
        StringBuilder sb = new StringBuilder();

        Wrapper wrapper = dialect.getWrapper();
        // compatible db-access mybatis-plus jpa
        JdbcTable annotation = clazz.getAnnotation(JdbcTable.class);
        if (null != annotation && StrUtil.isNotBlank(annotation.name())) {
            String schema = StrUtil.blankToDefault(annotation.schema(), "");
            List<String> list = ListTs.filter(ListTs.asList(wrapper.wrap(schema), wrapper.wrap(annotation.name())), StrUtil::isNotBlank);
            String join = String.join(".", list);
            sb.append(join);
        } else {
            if(clazz.isAnnotationPresent(TableName.class)){
                TableName annotation1 = clazz.getAnnotation(TableName.class);
                if(Objects.nonNull(annotation1)){
                    String value = annotation1.value();
                    if(StrUtil.isNotBlank(value)){
                        return value;
                    }
                }
            }else if(clazz.isAnnotationPresent(Table.class)){
                Table table = clazz.getAnnotation(Table.class);
                if(Objects.nonNull(table)){
                    String value = table.name();
                    if(StrUtil.isNotBlank(value)){
                        return value;
                    }
                }
            }
            String simpleName = clazz.getSimpleName();
            String underlineCase = wrapper.wrap(StrUtil.toUnderlineCase(simpleName)).toLowerCase();
            sb.append(underlineCase);
        }
        return sb.toString();
    }

    public String getSql(String sql, Connection connection, Object... args) {
        List<Object> newArrayList = ListTs.newArrayList();
        if (ArrayUtil.isNotEmpty(args)) {
            if (1 == args.length && args[0] instanceof Collection) {
                Object arg = args[0];
                newArrayList = ListTs.objectToListObject(arg, (object) -> object);
            } else {
                ListTs.flatten(args, newArrayList);
            }
        }
        if (CollUtil.isNotEmpty(newArrayList)) {
            return SqlPlaceholderReplacer.replacePlaceholders(sql, newArrayList, connection);
        } else {
            return sql;
        }
    }

    @Deprecated
    public void logSql(String sql, Connection connection, Object... args) {
        try {
            boolean property = Easy4j.getProperty(SysConstant.EASY4J_ENABLE_PRINT_SYS_DB_SQL, boolean.class);
            if (property && this.isPrintLog) {
                String logSql = getSql(sql, connection, args);
                Easy4j.info("[SQL] -> {}", logSql);

            }
        } catch (Exception e) {
            logger.error("log sql has error,{}", e.getMessage());
        }
    }

    public Pair<String, Date> recordSql(String sql, Connection connection, Object... args) {
        try {
            Date date = new Date();
            boolean property = Easy4j.getProperty(SysConstant.EASY4J_ENABLE_PRINT_SYS_DB_SQL, boolean.class);
            if (property && this.isPrintLog) {
                String logSql = getSql(sql, connection, args);
                return new Pair<>(logSql, date);
            }
        } catch (Exception e) {
            logger.error("log sql has error,{}", e.getMessage());
        }

        return null;
    }

    public void printSql(Pair<String, Date> pair, int effectRows) {
        if (pair == null) return;
        String key = pair.getKey();
        Date value = pair.getValue();
        CheckUtils.notNull(key, "logSql_key");
        CheckUtils.notNull(value, "logSql_value");
        long subTime = new Date().getTime() - value.getTime();
        logger.info("[SQL] -> {}ms {}row {}", subTime,effectRows, key);
    }

    public String getPrintSql(Pair<String, Date> pair) {
        if (pair == null) return "";
        return pair.getKey();
    }

    public String toUnderLine(String str) {
        return StrUtil.toUnderlineCase(str);
    }

}
