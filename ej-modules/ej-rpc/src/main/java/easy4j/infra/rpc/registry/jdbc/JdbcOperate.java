package easy4j.infra.rpc.registry.jdbc;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.dialect.Dialect;
import cn.hutool.db.dialect.DialectFactory;
import cn.hutool.db.sql.Query;
import cn.hutool.db.sql.SqlBuilder;
import easy4j.infra.rpc.config.BaseConfig;
import easy4j.infra.rpc.exception.SqlRuntimeException;
import easy4j.infra.rpc.integrated.ConnectionManager;
import easy4j.infra.rpc.integrated.DefaultConnectionManager;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.List;

/**
 * 一个简单的jdbc封装 传入connection连接 很简单  实现orm部分功能 这里只处理一种逻辑
 * <br/>
 * PS:连接获取和释放由 ConnectionManager 进行管理
 *
 * @author bokun
 * @since 2.0.1
 */
public class JdbcOperate {

    private final ConnectionManager connectionManager;

    private final QueryRunner queryRunner = new QueryRunner();

    public JdbcOperate(BaseConfig baseConfig) {
        this.connectionManager = IntegratedFactory.getOrDefault(ConnectionManager.class, () -> new DefaultConnectionManager(baseConfig));
    }

    public Connection getConnection() {
        try {
            return connectionManager.getConnection();
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        }
    }

    public void releaseConnection(Connection connection) {
        connectionManager.releaseConnection(connection);
    }

    /**
     * 更新
     *
     * @param sql  sql 占位符必须是 ?
     * @param args 参数
     * @return 整数
     */
    public int update(Connection connection, String sql, Object... args) {
        notNull(connection,"connection");
        notNull(sql,"sql");
        try {
            return queryRunner.update(connection, sql, args);
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        }
    }

    /**
     * 写入
     *
     * @param sql  sql 占位符必须是 ?
     * @param args 参数列表
     * @return 自增主键
     */
    public Object insert(Connection connection, String sql, Object... args) {
        notNull(connection,"connection");
        notNull(sql,"sql");
        try {
            return queryRunner.insert(connection, sql, new ScalarHandler<>(), args);
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        }
    }

    /**
     * 查询 并转为 实体集合
     *
     * @param sql   sql 占位符必须是 ?
     * @param clazz 要转成的实体class对象
     * @param args  参数列表
     * @param <T>
     * @return 实体集合
     */
    public <T> List<T> query(Connection connection, String sql, Class<T> clazz, Object... args) {
        notNull(connection,"connection");
        notNull(sql,"sql");
        try {
            QueryRunner queryRunner = new QueryRunner();
            return queryRunner.query(connection, sql, new BeanPropertyHandler<>(clazz), args);
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        }
    }

    /**
     * 查询数量
     *
     * @param sql 占位符必须是 ?
     * @return
     */
    public int count(Connection connection, String sql, Object... args) {
        notNull(connection,"connection");
        notNull(sql,"sql");
        try {
            QueryRunner queryRunner = new QueryRunner();
            return queryRunner.query(connection, sql, new ScalarHandler<>(), args);
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        }
    }

    /**
     * 删除
     *
     * @param object 要删除的表实体
     * @return
     */
    public int delete(Object object) {
        notNull(object,"where");
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            Dialect dialect = DialectFactory.newDialect(connection);
            Entity where = getWhere(object);
            final SqlBuilder delete = SqlBuilder
                    .create(dialect.getWrapper())
                    .delete(where.getTableName())
                    .where(
                            Query.of(where).getWhere()
                    );
            return update(connection, delete.build(), delete.getParamValueArray());
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            connectionManager.releaseConnection(connection);
        }
    }

    /**
     * 传入要写入的实体类
     * 表民是类名的驼峰转下户线
     *
     * @param object 要写入的值  表名为class类名的驼峰转下划线
     * @return 自增值
     */
    public Object insert(Object object) {
        notNull(object,"where");
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            Entity entity = getWhere(object);
            Dialect dialect = DialectFactory.newDialect(connection);
            final SqlBuilder delete = SqlBuilder
                    .create(dialect.getWrapper())
                    .insert(entity, dialect.dialectName());
            return insert(connection, delete.build(), delete.getParamValueArray());
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            connectionManager.releaseConnection(connection);
        }
    }

    /**
     * 删除
     *
     * @param object
     * @return
     */
    public int del(Object object) {
        notNull(object,"where");
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            Entity entity = getWhere(object);
            Dialect dialect = DialectFactory.newDialect(connection);
            final SqlBuilder delete = SqlBuilder
                    .create(dialect.getWrapper())
                    .delete(entity.getTableName())
                    .where(Query.of(entity).getWhere());
            return update(connection, delete.build(), delete.getParamValueArray());
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            connectionManager.releaseConnection(connection);
        }
    }

    /**
     * 改
     *
     * @param entity 要改的数据 其中要包含表名
     * @param where  条件
     * @return
     */
    public int update(Entity entity, Entity where) {
        notNull(where,"where");
        notNull(entity,"entity");
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            where = getWhere(where);
            Dialect dialect = DialectFactory.newDialect(connection);
            final SqlBuilder delete = SqlBuilder
                    .create(dialect.getWrapper())
                    .update(entity)
                    .where(Query.of(where).getWhere());
            return update(connection, delete.build(), delete.getParamValueArray());
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            connectionManager.releaseConnection(connection);
        }
    }

    /**
     * 改
     *
     * @param object 要写/改的数据 其中要包含表名
     * @param where  条件
     * @return
     */
    public int saveOrUpdate(Object object, Entity where) {
        notNull(where,"where");
        notNull(object,"object");
        if (exists(where)) {
            return update(getWhere(object), where);
        } else {
            insert(object);
            return 1;
        }
    }

    /**
     * 是否存在
     *
     * @param where 条件
     * @return
     */
    public boolean exists(Object where) {
        notNull(where,"where");
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            Dialect dialect = DialectFactory.newDialect(connection);
            final SqlBuilder query = SqlBuilder
                    .create(dialect.getWrapper())
                    .query(Query.of(getWhere(where)).setFields("count(*)"));
            return count(connection,query.build(),query.getParamValueArray()) > 0;
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            connectionManager.releaseConnection(connection);
        }
    }

    public void notNull(Object object,String name){

        if(object == null){
            throw new IllegalArgumentException(StrUtil.blankToDefault(name,"object")+" is not null !");
        }

    }
    /**
     * 是否存在
     *
     * @param where 条件
     * @return
     */
    public int count(Object where) {
        notNull(where,"where");
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            Dialect dialect = DialectFactory.newDialect(connection);
            final SqlBuilder query = SqlBuilder
                    .create(dialect.getWrapper())
                    .query(Query.of(getWhere(where)).setFields("count(*)"));
            return count(connection, query.build(), query.getParamValueArray());
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            connectionManager.releaseConnection(connection);
        }
    }


    /**
     * 传入实体类 当作要查询的条件
     * 表民是类名的驼峰转下户线
     *
     * @param object
     * @param tClass 要转成的对象
     * @param fields 要查询的字段
     * @return
     */
    public <T> List<T> queryList(Object object, Class<T> tClass, String... fields) {
        notNull(object,"where");
        notNull(tClass,"tClass");
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            Entity entity = getWhere(object);
            Query query = Query.of(entity);
            if (fields != null && fields.length > 0) {
                query.setFields(fields);
            }
            Dialect dialect = DialectFactory.newDialect(connection);

            final SqlBuilder queryBuilder = SqlBuilder
                    .create(dialect.getWrapper())
                    .query(query);
            return query(connection, queryBuilder.build(), tClass, queryBuilder.getParamValueArray());
        } catch (SQLException e) {
            throw new SqlRuntimeException(e);
        } finally {
            connectionManager.releaseConnection(connection);
        }
    }

    private static Entity getWhere(Object object) {
        if (object instanceof Entity) {
            return (Entity) object;
        }
        Class<?> aClass = object.getClass();
        String simpleName = aClass.getSimpleName();
        String underlineCase = StrUtil.toUnderlineCase(simpleName);
        Entity entity = Entity.create(underlineCase);
        Field[] fields = ReflectUtil.getFields(aClass);
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }
            String name = field.getName();
            String underlineCase1 = StrUtil.toUnderlineCase(name);
            Object fieldValue = ReflectUtil.getFieldValue(object, field);
            if (null != fieldValue) {
                entity.set(underlineCase1, fieldValue);
            }
        }
        return entity;
    }


}
