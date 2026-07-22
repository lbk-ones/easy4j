package easy4j.infra.dbaccess.orm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcIgnore;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.DDLField;
import easy4j.infra.dbaccess.dynamic.dll.DDLTable;
import easy4j.infra.dbaccess.exception.DbAccessException;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import easy4j.infra.dbaccess.orm.conditions.Condition;
import easy4j.infra.dbaccess.orm.conditions.UpdateBuild;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import easy4j.infra.dbaccess.orm.runner.LogSql;
import easy4j.infra.dbaccess.orm.runner.PsRes;
import easy4j.infra.dbaccess.orm.runner.SqlRunner;
import easy4j.infra.dbaccess.orm.sql.SqlFactory;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 工具类不存放任何属性 只存放 AccessConfig
 */
@Data
public class AccessUtils implements Serializable {


    private AccessConfig accessConfig;

    public AccessUtils(AccessConfig accessConfig) {
        this.accessConfig = accessConfig;
    }


    public Connection getConnection() {
        DataSource dataSource = accessConfig.getDataSource();
        try {
            Assert.notNull(dataSource);
            if (this.accessConfig.isInTransaction()) {
                return DataSourceUtils.getConnection(dataSource);
            } else {
                Connection connection = dataSource.getConnection();
                connection.setAutoCommit(true);
                return connection;
            }
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("getConnection", null, e);
        }
    }

    /**
     * 根据配置，来决定是否要转下划线
     *
     * @param name 字段/表 的名称
     * @return
     */
    public String fn(String name) {
        if (accessConfig.isFieldNameToUnderline()) {
            return StrUtil.toUnderlineCase(name);
        }
        return name;
    }

    /**
     * 获取表名 注解里面的表名统一原样返回
     *
     * @param clazz 对象类型
     * @return 表名
     */
    public String getTableName(Class<?> clazz, DialectV2 dialect) {
        if (clazz == null) return null;
        StringBuilder sb = new StringBuilder();
        JdbcTable annotation = clazz.getAnnotation(JdbcTable.class);

        if (null != annotation && StrUtil.isNotBlank(annotation.name())) {
            sb.append(annotation.name());
        } else {
            if (clazz.isAnnotationPresent(TableName.class)) {
                TableName annotation1 = clazz.getAnnotation(TableName.class);
                if (Objects.nonNull(annotation1)) {
                    String value = annotation1.value();
                    if (StrUtil.isNotBlank(value)) {
                        return value;
                    }
                }
            } else if (clazz.isAnnotationPresent(Table.class)) {
                Table table = clazz.getAnnotation(Table.class);
                if (Objects.nonNull(table)) {
                    String value = table.name();
                    if (StrUtil.isNotBlank(value)) {
                        return value;
                    }
                }
            } else if (clazz.isAnnotationPresent(DDLTable.class)) {
                DDLTable annotation2 = clazz.getAnnotation(DDLTable.class);
                String s = annotation2.tableName();
                if (StrUtil.isNotBlank(s)) {
                    return s;
                }
            }
            String simpleName = clazz.getSimpleName();
            String underlineCase = dialect.escape(fn(simpleName)).toLowerCase();
            sb.append(underlineCase);
        }
        return sb.toString();
    }

    public boolean isPk(Field field) {
        if (field.isAnnotationPresent(JdbcColumn.class)) {
            JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
            return annotation.isPrimaryKey();
        } else if (field.isAnnotationPresent(TableId.class) || field.isAnnotationPresent(Id.class)) {
            return true;
        } else if (field.isAnnotationPresent(DDLField.class)) {
            return field.getAnnotation(DDLField.class).isPrimary();
        }
        return false;
    }

    public boolean isAutoIncrement(Field field) {
        if (field.isAnnotationPresent(JdbcColumn.class)) {
            JdbcColumn annotation = field.getAnnotation(JdbcColumn.class);
            return annotation.isPrimaryKey() && annotation.autoIncrement();
        } else if (field.isAnnotationPresent(TableId.class) && field.getAnnotation(TableId.class).type() == IdType.AUTO) {
            return true;
        } else if (field.isAnnotationPresent(DDLField.class)) {
            DDLField annotation = field.getAnnotation(DDLField.class);
            return annotation.isPrimary() && annotation.isAutoIncrement();
        }
        return false;
    }


    public String getSchema(Class<?> clazz) {
        if (clazz == null) return "";
        JdbcTable annotation = clazz.getAnnotation(JdbcTable.class);

        StringBuilder sb = new StringBuilder();
        if (null != annotation && StrUtil.isNotBlank(annotation.name())) {
            String schema = annotation.schema();
            if (StrUtil.isNotBlank(schema)) {
                sb.append(schema);
            }
        } else {
            if (clazz.isAnnotationPresent(TableName.class)) {
                TableName annotation1 = clazz.getAnnotation(TableName.class);
                if (Objects.nonNull(annotation1)) {
                    String schema = annotation1.schema();
                    if (StrUtil.isNotBlank(schema)) {
                        sb.append(schema);
                    }
                }
            }
        }
        return sb.toString();
    }

    public String getColumnNameFormField(Field field) {
        if (field.isAnnotationPresent(TableField.class)) {
            return field.getAnnotation(TableField.class).value();
        } else if (field.isAnnotationPresent(DDLField.class)) {
            return field.getAnnotation(DDLField.class).name();
        } else if (field.isAnnotationPresent(JdbcColumn.class)) {
            return field.getAnnotation(JdbcColumn.class).name();
        } else if (field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class).name();
        }
        return fn(field.getName());
    }

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

    public void assertNotNull(Object object, String name) {
        if (object == null) throw new AccessException(name + " param is not null");
    }

    public <T> RuntimeContext<T> toContext(Access<T> access) {
        Class<T> clazz = access.getClazz();
        if (!access.isReturnMap()) {
            assertNotNull(clazz, "clazz");
        }
        T params = access.getParam();
        Iterable<T> params2 = access.getParams();
        OperateType operateType = access.getOperateType();
        List<T> p = new ArrayList<>();
        ListTs.add(p, params);
        ListTs.addAll(p, params2);
        Field[] fields = new Field[]{};
        if (clazz != null) {
            fields = ReflectUtil.getFields(clazz);
        }
        // obtain datasource connection
        Connection connection = getConnection();
        DialectV2 dialectV2 = DialectFactory.get(connection);
        String dbType = dialectV2.getDbType();
        List<AccessField> columnInfoList = new ArrayList<>();
        List<AccessField> updateList = new LinkedList<>();
        // 写入的字段，如果主键没有指定值，则不应该纳入写入字段里面
        List<AccessField> insertList = new LinkedList<>();
        List<AccessField> idlist = new LinkedList<>();
        int index = 0;
        // 如果没有参数则只记录字段信息 字段信息的值是null
        if (p.isEmpty()) {
            for (Field field : fields) {
                if (skipColumn(field)) continue;
                boolean pk = isPk(field);
                boolean isAutoIncrement = isAutoIncrement(field);
                String columnField = getColumnNameFormField(field);
                AccessField accessField = patchItem(dialectV2, columnInfoList, index, field, pk, isAutoIncrement, columnField);

                // feat: 新增按主键值查询的功能
                Serializable primaryKey = access.getPrimaryKey();
                if (pk && primaryKey != null && accessField != null) {
                    AccessField accessField1 = accessField.cloneNew();
                    Field field1 = accessField1.getField();
                    Object convert = Convert.convert(field1.getType(), primaryKey);
                    accessField1.setColumnValue(convert);
                    idlist.add(accessField1);
                }
            }
        } else {
            for (T t : p) {
                for (Field field : fields) {
                    if (skipColumn(field)) continue;
                    boolean pk = isPk(field);
                    boolean isAutoIncrement = isAutoIncrement(field);
                    String columnField = getColumnNameFormField(field);
                    if (index == 0) {
                        patchItem(dialectV2, columnInfoList, index, field, pk, isAutoIncrement, columnField);
                    }
                    refreshParam(
                            ReflectUtil.getFieldValue(t, field),
                            field,
                            columnField,
                            dialectV2,
                            index,
                            pk,
                            isAutoIncrement,
                            idlist,
                            operateType,
                            access.isSkipNullIs(),
                            updateList,
                            insertList
                    );
                }
                index++;
            }
        }

        String schema = access.getSchema();
        return new RuntimeContext<T>()
                .setSql(access.getSql())
                .setPage(access.getPage())
                .setAccess(access)
                .setAccessUtils(this)
                .setClazz(clazz)
                .setDbType(dbType)
                .setReturnMap(access.isReturnMap())
                .setParams(p)
                .setResultFieldToCamel(access.isResultFieldToCame())
                .setConnection(connection)
                .setOperateType(operateType)
                .setUpdateFields(updateList)
                .setColumnInfoList(columnInfoList)
                .setIdList(idlist)
                .setInsertFields(insertList)
                .setDialectV2(dialectV2)
                .setTableName(dialectV2.escape(StrUtil.blankToDefault(getTableName(clazz, dialectV2), access.getTableName())))
                .setSchema(dialectV2.escape(StrUtil.blankToDefault(getSchema(clazz), schema)));

    }

    private AccessField patchItem(DialectV2 dialectV2, List<AccessField> columnInfoList, int index, Field field, boolean pk, boolean isAutoIncrement, String columnField) {
        AccessField columnInfo = new AccessField();
        columnInfo.setColumnName(columnField);
        columnInfo.setEscapeColumnName(dialectV2.escape(columnField));
        columnInfo.setColumnValue(null);
        columnInfo.setField(field);
        columnInfo.setGroup(index);
        columnInfo.setPkIs(pk);
        columnInfo.setAutoIncrementIs(isAutoIncrement);
        columnInfoList.add(columnInfo);
        return columnInfo;
    }

    /**
     * 刷新一个参数
     *
     * @param fieldValue      参数的值
     * @param parentField     参数的field对象
     * @param columnField     参数的名称
     * @param dialectV2       方言
     * @param index           第几个参数
     * @param pk              是否主键
     * @param isAutoIncrement 是否自动递增
     * @param idlist          主键列表
     * @param operateType     操作类型
     * @param access          传参
     * @param updateList      更新列表
     * @param insertList      写入列表
     * @param <T>             泛型
     */
    private static <T> void refreshParam(
            Object fieldValue,
            Field parentField,
            String columnField,
            DialectV2 dialectV2,
            int index,
            boolean pk,
            boolean isAutoIncrement,
            List<AccessField> idlist,
            OperateType operateType,
            boolean access,
            List<AccessField> updateList,
            List<AccessField> insertList) {
        AccessField accessField = new AccessField();
        accessField.setField(parentField);
        accessField.setColumnName(columnField);
        accessField.setEscapeColumnName(dialectV2.escape(columnField));
        accessField.setColumnValue(fieldValue);
        accessField.setGroup(index);
        accessField.setPkIs(pk);
        accessField.setAutoIncrementIs(isAutoIncrement);
        if (pk) {
            idlist.add(accessField);
        }
        if (operateType == OperateType.UPDATE && !pk) {
            if (access) {
                if (!ObjectUtil.isEmpty(fieldValue)) updateList.add(accessField);
            } else {
                updateList.add(accessField);
            }
        } else if (operateType == OperateType.INSERT) {
            if (isAutoIncrement) {
                Object columnValue = accessField.getColumnValue();
                if (!ObjectUtil.isEmpty(columnValue)) {
                    insertList.add(accessField);
                }
            } else {
                insertList.add(accessField);
            }
        }

    }

    /**
     * 根据传入参数重新刷新上下文列表
     *
     * @param context 上下文
     * @param param   参数
     * @param <T>     泛型
     */
    public <T> void refreshContextByParam(RuntimeContext<T> context, T param) {
        DialectV2 dialectV2 = context.getDialectV2();
        Class<T> clazz = context.getClazz();
        if (clazz == null || param == null) return;
        Field[] fields = ReflectUtil.getFields(clazz);
        OperateType operateType = context.getOperateType();
        Access<T> access = context.getAccess();
        List<AccessField> idlist = new LinkedList<>();
        List<AccessField> updateList = new LinkedList<>();
        // 写入的字段，如果主键没有指定值，则不应该纳入写入字段里面
        List<AccessField> insertList = new LinkedList<>();
        for (Field field : fields) {
            if (skipColumn(field)) continue;
            boolean pk = isPk(field);
            boolean isAutoIncrement = isAutoIncrement(field);
            String columnField = getColumnNameFormField(field);
            refreshParam(
                    ReflectUtil.getFieldValue(param, field),
                    field,
                    columnField,
                    dialectV2,
                    0,
                    pk,
                    isAutoIncrement,
                    idlist,
                    operateType,
                    access.isSkipNullIs(),
                    updateList,
                    insertList
            );
        }
        context.setSql(null);
        context.setIdList(idlist);
        context.setUpdateFields(updateList);
        context.setInsertFields(insertList);

    }

    /**
     * 解析WhereBuild
     *
     * @param where   条件构造器
     * @param context 上下文
     * @param <T>     泛型
     */
    public <T> void parseWhere(WhereBuild where, RuntimeContext<T> context) {
        List<Object> whereArgs = new ArrayList<>();
        String whereSql = null;
        List<String> selectFieldName = new ArrayList<>();
        if (where != null) {
            whereSql = where.build(whereArgs, context, context.isSkipTail());
            List<Condition> selectFields = where.getSelectFields();
            if (CollUtil.isNotEmpty(selectFields)) {
                selectFieldName = selectFields.stream()
                        .filter(e -> StrUtil.isNotBlank(e.getColumn()))
                        .map(e -> fn(e.getColumn()))
                        .toList();
            }

            String last = where.getLast();
            context.setLastSql(last);
        }
        context.setWhereSql(whereSql);
        context.setSelectFields(selectFieldName);
        DialectV2 dialectV2 = context.getDialectV2();
        context.setEscapeSelectFields(selectFieldName.stream().map(dialectV2::escape).toList());
        context.setWhereArgs(whereArgs);
    }

    /**
     * 解析UpdateBuild
     *
     * @param update  条件构造器
     * @param context 上下文
     * @param <T>     泛型
     */
    public <T> void parseUpdate(UpdateBuild update, RuntimeContext<T> context) {
        List<Object> whereArgs = new ArrayList<>();
        List<String> selectFieldName = new ArrayList<>();
        String whereSql = update.build(whereArgs, context, context.isSkipTail());
        List<Condition> selectFields = update.getSelectFields();
        if (CollUtil.isNotEmpty(selectFields)) {
            selectFieldName = selectFields.stream()
                    .filter(e -> StrUtil.isNotBlank(e.getColumn()))
                    .map(e -> fn(e.getColumn()))
                    .toList();
        }
        String last = update.getLast();
        context.setLastSql(last);
        context.setSqlSet(update.getSqlSet());
        context.setUpdateArgs(update.getArgs());
        context.setWhereSql(whereSql);
        context.setSelectFields(selectFieldName);
        DialectV2 dialectV2 = context.getDialectV2();
        context.setEscapeSelectFields(selectFieldName.stream().map(dialectV2::escape).toList());
        context.setWhereArgs(whereArgs);
    }

    /**
     * 解析sql并执行
     *
     * @param context      上下文
     * @param skipParseSql 是否跳过解析sql,因为有些sql是从外部传进来的 有必要做这个判断
     * @param <T>          泛型
     */
    public <T> void parseSql(RuntimeContext<T> context, boolean skipParseSql) {
        LogSql.init(context);
        if (!skipParseSql) {
            SqlFactory.parse(context);
        }
        String sql = context.getSql();
        if (StrUtil.isBlank(sql)) {
            throw new AccessException("sql is not be empty!");
        }
        SqlRunner sqlRunner = new SqlRunner();
        sqlRunner.run(context);
    }


    /**
     * 转义
     *
     * @author bokun.li
     * @date 2025/9/4
     */
    public String escapeCn(String name, DialectV2 dialectV2, boolean forceEscape) {
        if (forceEscape) {
            return dialectV2.forceEscape(name);
        } else {
            return dialectV2.escape(name);
        }
    }

    /**
     * 释放连接
     *
     * @param context
     * @param <T>
     */
    public <T> void releaseConnection(RuntimeContext<T> context) {
        if (context != null) {
            Connection connection = context.getConnection();
            DataSourceUtils.releaseConnection(connection, context.getConfig().getDataSource());
        }
    }

    /**
     * 关闭resultset
     *
     * @param rs
     */
    public void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ignored) {

            }
        }
    }

    public void close(Connection rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ignored) {

            }
        }
    }

    public void close(Statement rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ignored) {

            }
        }
    }

    public void close(PsRes psRes) {
        if (psRes == null) {
            return;
        }
        Statement statement = psRes.getStatement();
        ResultSet resultSet = psRes.getResultSet();
        close(resultSet);
        close(statement);

    }

    private static final SQLErrorCodeSQLExceptionTranslator sqlErrorCodeSQLExceptionTranslator = new SQLErrorCodeSQLExceptionTranslator();


    public static DataAccessException translate(String task, String sql, SQLException sqlException, DataSource dataSource) {
        DataAccessException translate = null;
        try {
            sqlErrorCodeSQLExceptionTranslator.setDataSource(dataSource);
            translate = sqlErrorCodeSQLExceptionTranslator.translate(task, sql, sqlException);
        } catch (Exception ignored) {
        }
        if (translate == null) {
            throw new AccessException(sqlException);
        }
        return translate;
    }

    /**
     * 拼接 where 语句
     *
     * @param prefix   前缀
     * @param whereTxt where条件字符串
     * @return 拼接之后的词
     */
    public String appendWhere(String prefix, String whereTxt) {
        String TEMP = prefix;
        if (!StrUtil.endWith(TEMP, SP.SPACE)) {
            TEMP += SP.SPACE;
        }
        if (StrUtil.isNotBlank(whereTxt)) {
            String trim = whereTxt.trim();
            if (StrUtil.startWithIgnoreCase(trim, "order by") || StrUtil.startWithIgnoreCase(trim, "group by")) {
                TEMP += whereTxt;
            } else {
                TEMP += "where" + SP.SPACE + whereTxt;
            }
        }
        return TEMP;
    }
}
