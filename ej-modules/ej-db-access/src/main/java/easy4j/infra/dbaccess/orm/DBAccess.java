package easy4j.infra.dbaccess.orm;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.domain.PageRes;
import easy4j.infra.dbaccess.orm.conditions.IUpdateBuild;
import easy4j.infra.dbaccess.orm.conditions.IWhere;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.List;

/**
 * 泛型简化版本，这个版本，可以在调用方法的时候不传入class类型对象，只需要在创建的时候传一次就可以了
 *
 * @param <T>
 * @author bokun.li
 * @since 2.1.5.8
 */
@Slf4j
public class DBAccess<T> implements IDBAccessBase<T> {

    public Class<T> clazz;

    public IDBAccess idbAccess;

    /**
     * 会自动去获取 IDBAccess bean实例
     *
     * @param clazz orm类的类型
     */
    public static <T> IDBAccessBase<T> of(Class<T> clazz) {
        Assert.notNull(clazz, "clazz");
        return new DBAccess<>(clazz, null);
    }


    public static <T> IDBAccessBase<T> of(Class<T> clazz, DataSource dataSource) {
        Assert.notNull(clazz, "clazz");
        Assert.notNull(dataSource, "dataSource");
        AccessConfig accessConfig = new AccessConfig().setDataSource(dataSource);
        return new DBAccess<>(clazz, accessConfig);
    }

    public DBAccess(Class<T> clazz, AccessConfig accessConfig) {
        Assert.notNull(clazz, "clazz");
        if (accessConfig != null) {
            try {
                SpringOrmProperties bean = SpringUtil.getBean(SpringOrmProperties.class);
                PluginLoader.loader(bean.getPlugins(), accessConfig);
            } catch (Exception ex) {
                if (log.isWarnEnabled()) {
                    log.warn("dbaccess plugins load error", ex);
                }
            }
            idbAccess = new DBAccessImpl(accessConfig);
        } else {
            idbAccess = SpringUtil.getBean(IDBAccess.class);
        }
        this.clazz = clazz;
    }

    @Override
    public Connection getConnection() {
        return idbAccess.getConnection();
    }

    @Override
    public void runScript(Connection connection, String ddlSql, List<String> path, boolean isCloseConnection) throws IOException {
        idbAccess.runScript(connection, ddlSql, path, isCloseConnection);
    }

    @Override
    public T save(T params) {
        return idbAccess.save(params, clazz);
    }

    @Override
    public List<T> save(Iterable<T> params) {
        return idbAccess.save(params, clazz);

    }

    @Override
    public int batchSave(Iterable<T> params, int batchSize) {
        return idbAccess.batchSave(params, clazz, batchSize);
    }

    @Override
    public int batchSave(Iterable<T> params) {
        return idbAccess.batchSave(params, clazz);
    }

    @Override
    public int delete(IWhere whereBuild) {
        return idbAccess.delete(whereBuild, clazz);
    }

    @Override
    public int deleteAll() {
        return idbAccess.deleteAll(clazz);
    }

    @Override
    public int deleteById(T params) {
        return idbAccess.deleteById(params, clazz);

    }

    @Override
    public int deleteByPrimaryKey(Serializable primaryKey) {
        return idbAccess.deleteByPrimaryKey(primaryKey, clazz);
    }

    @Override
    public int deleteByIds(Iterable<T> ids) {
        return idbAccess.deleteByIds(ids, clazz);
    }

    @Override
    public int update(T params, boolean isSkipNull, IWhere whereBuild) {
        return idbAccess.update(params, isSkipNull, whereBuild, clazz);
    }

    @Override
    public int update(IUpdateBuild updateBuild) {
        return idbAccess.update(updateBuild, clazz);
    }

    @Override
    public int updateById(T params, boolean isSkipNull) {
        return idbAccess.updateById(params, isSkipNull, clazz);

    }

    @Override
    public int updateByIds(Iterable<T> params, boolean isSkipNull) {
        return idbAccess.updateByIds(params, isSkipNull, clazz);
    }

    @Override
    public int dynamicUpdate(List<EasyMap<String, Object>> value, String tableName, String schema, boolean isSkipNull) {
        return idbAccess.dynamicUpdate(value, tableName, schema, isSkipNull);

    }

    @Override
    public int dynamicSave(List<EasyMap<String, Object>> value, String tableName, String schema) {
        return idbAccess.dynamicSave(value, tableName, schema);

    }

    @Override
    public List<T> queryJoin(SqlWrapper sql) {
        return idbAccess.queryJoin(sql, clazz);

    }

    @Override
    public PageRes queryPageJoin(SqlWrapper sql, Page<T> page) {
        return idbAccess.queryPageJoin(sql, page, clazz);

    }

    @Override
    public List<EasyMap<String, Object>> queryMapJoin(SqlWrapper sql, boolean resultFieldToCame) {
        return idbAccess.queryMapJoin(sql, resultFieldToCame);
    }

    @Override
    public List<T> query(String sql, Object... args) {
        return idbAccess.query(sql, clazz, args);

    }

    @Override
    public T queryOne(String sql, Object... args) {
        return idbAccess.queryOne(sql, clazz, args);

    }

    @Override
    public List<EasyMap<String, Object>> queryMapListBySql(String sql, boolean resultFieldToCame, Object... args) {
        return idbAccess.queryMapListBySql(sql, resultFieldToCame, clazz, args);

    }

    @Override
    public EasyMap<String, Object> queryMapByTableName(String schema, String tableName, boolean resultFieldToCame, IWhere whereBuild, boolean queryRealFields) {
        return idbAccess.queryMapByTableName(schema, tableName, resultFieldToCame, whereBuild, queryRealFields);
    }

    @Override
    public PageRes queryPageByTableName(String schema, String tableName, boolean resultFieldToCame, IWhere whereBuild, boolean queryRealFields, Page<Object> page) {
        return idbAccess.queryPageByTableName(schema, tableName, resultFieldToCame, whereBuild, queryRealFields, page);
    }

    @Override
    public List<EasyMap<String, Object>> queryMapListByTableName(String schema, String tableName, boolean resultFieldToCame, IWhere whereBuild, boolean queryRealFields) {
        return idbAccess.queryMapListByTableName(schema, tableName, resultFieldToCame, whereBuild, queryRealFields);
    }

    @Override
    public List<T> query(IWhere whereBuild) {
        return idbAccess.query(whereBuild, clazz);
    }

    @Override
    public List<T> queryAll() {
        return idbAccess.queryAll(clazz);
    }

    @Override
    public T queryOne(IWhere whereBuild) {
        return idbAccess.queryOne(whereBuild, clazz);
    }

    @Override
    public long count(IWhere whereBuild) {
        return idbAccess.count(whereBuild, clazz);
    }

    @Override
    public boolean exists(IWhere whereBuild) {
        return idbAccess.exists(whereBuild, clazz);
    }

    @Override
    public EasyMap<String, Object> queryOneMap(IWhere whereBuild, boolean toCamel) {
        return idbAccess.queryOneMap(whereBuild, clazz, toCamel);
    }

    @Override
    public PageRes queryPage(IWhere whereBuild, Page<T> page) {
        return idbAccess.queryPage(whereBuild, page, clazz);
    }

    @Override
    public T queryById(T param) {
        return idbAccess.queryById(param, clazz);
    }

    @Override
    public T queryByPrimaryKey(Serializable primaryKey) {
        return idbAccess.queryByPrimaryKey(primaryKey, clazz);
    }

    @Override
    public int truncate() {
        return idbAccess.truncate(clazz);
    }
}
