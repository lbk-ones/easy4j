package easy4j.infra.dbaccess.orm;

import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import easy4j.infra.dbaccess.domain.PageRes;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

public class DBAccessImpl implements IDBAccess{

    private final AccessUtils accessUtils;

    public DBAccessImpl(AccessConfig accessConfig) {
        this.accessUtils = new AccessUtils(accessConfig);
        DataSource dataSource = accessConfig.getDataSource();
        if(dataSource==null){
            throw new AccessException("datasource is not allow null!");
        }
    }

    @Override
    public <T> T save(T params, Class<T> clazz) {
        Access<T> tAccess = new Access<T>().setParam(params)
                .setClazz(clazz)
                .setOperateType(OperateType.INSERT);
        RuntimeContext<T> context = accessUtils.parse(tAccess);

        return null;
    }

    @Override
    public <T> T save(Iterable<T> params, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParams(params)
                .setClazz(clazz)
                .setOperateType(OperateType.INSERT);
        RuntimeContext<T> context = accessUtils.parse(tAccess);
        return null;
    }

    @Override
    public <T> int delete(WhereBuild whereBuild, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.parse(tAccess);
        accessUtils.parseWhere(whereBuild,context);
        return 0;
    }

    @Override
    public <T> int deleteById(Serializable id, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setId(id)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.parse(tAccess);

        return 0;
    }

    @Override
    public <T> int deleteById(T params, Function<T, Serializable> idGet, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParam(params)
                .setIdGet(idGet)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.parse(tAccess);

        return 0;
    }

    @Override
    public <T> int deleteByIds(Iterable<Serializable> ids, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setIds(ids)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.parse(tAccess);

        return 0;
    }

    @Override
    public <T> int deleteByIds(Iterable<T> params, Function<T, Serializable> idGet, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParams(params)
                .setIdGet(idGet)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.parse(tAccess);
        return 0;
    }

    @Override
    public <T> int updateById(T param, boolean isSkipNull, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParam(param)
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);
        RuntimeContext<T> context = accessUtils.parse(tAccess);
        return 0;
    }

    @Override
    public <T> int updateById(T param, Function<T, Serializable> idGet, boolean isSkipNull, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParam(param)
                .setIdGet(idGet)
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);

        RuntimeContext<T> context = accessUtils.parse(tAccess);
        return 0;
    }

    @Override
    public <T> int updateByIds(Iterable<T> params, boolean isSkipNull, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParams(params)
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);
        RuntimeContext<T> context = accessUtils.parse(tAccess);
        return 0;
    }

    @Override
    public <T> int updateByIds(Iterable<T> params, Function<T, Serializable> idGet, boolean isSkipNull, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParams(params)
                .setIdGet(idGet)
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);

        RuntimeContext<T> context = accessUtils.parse(tAccess);

        return 0;
    }

    @Override
    public <T> int update(T params, boolean isSkipNull, WhereBuild whereBuild, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParam(params)
                .setWhere(whereBuild)
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);

        RuntimeContext<T> context = accessUtils.parse(tAccess);
        accessUtils.parseWhere(whereBuild,context);
        return 0;
    }

    @Override
    public <T> List<T> query(String sql, Class<T> clazz, Object... args) {
        Access<T> tAccess = new Access<T>()
                .setSql(sql)
                .setArgs(ListTs.asList(args))
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.parse(tAccess);

        return List.of();
    }

    @Override
    public <T> T queryOne(String sql, Class<T> clazz, Object... args) {
        Access<T> tAccess = new Access<T>()
                .setSql(sql)
                .setArgs(ListTs.asList(args))
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);

        RuntimeContext<T> context = accessUtils.parse(tAccess);
        return null;
    }

    @Override
    public <T> EasyMap<String, Object> queryMap(String sql, Object... args) {
        Access<T> tAccess = new Access<T>()
                .setSql(sql)
                .setArgs(ListTs.asList(args))
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.parse(tAccess);
        return null;
    }

    @Override
    public EasyMap<String, Object> queryMapByTableName(String schema, String tableName, boolean resultFieldToCame, WhereBuild whereBuild) {
        Access<Object> tAccess = new Access<>()
                .setSchema(schema)
                .setTableName(tableName)
                .setResultFieldToCame(resultFieldToCame)
                .setWhere(whereBuild)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<Object> context = accessUtils.parse(tAccess);
        accessUtils.parseWhere(whereBuild,context);
        return null;
    }

    @Override
    public <T> List<T> query(WhereBuild whereBuild, Class<T> clazz) {

        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.parse(tAccess);
        accessUtils.parseWhere(whereBuild,context);

        return List.of();
    }

    @Override
    public List<EasyMap<String, Object>> queryMap(WhereBuild whereBuild, boolean resultFieldToCame) {


        Access<Object> tAccess = new Access<Object>()
                .setWhere(whereBuild)
                .setResultFieldToCame(resultFieldToCame)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<Object> context = accessUtils.parse(tAccess);
        accessUtils.parseWhere(whereBuild,context);
        return List.of();
    }

    @Override
    public <T> T queryOne(WhereBuild whereBuild, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.parse(tAccess);
        accessUtils.parseWhere(whereBuild,context);
        return null;
    }

    @Override
    public <T> EasyMap<String, Object> queryOneMap(WhereBuild whereBuild, Class<T> clazz, boolean resultFieldToCame) {
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setResultFieldToCame(resultFieldToCame)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.parse(tAccess);
        accessUtils.parseWhere(whereBuild,context);

        return null;
    }

    @Override
    public <T> PageRes queryPage(WhereBuild whereBuild, Page<T> page, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setPage(page)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.parse(tAccess);
        accessUtils.parseWhere(whereBuild,context);
        return null;
    }
}
