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

public class DBAccessImpl implements IDBAccess {

    private final AccessUtils accessUtils;

    public DBAccessImpl(AccessConfig accessConfig) {
        this.accessUtils = new AccessUtils(accessConfig);
        DataSource dataSource = accessConfig.getDataSource();
        if (dataSource == null) {
            throw new AccessException("datasource is not allow null!");
        }
    }

    @Override
    public <T> T save(T params, Class<T> clazz) {
        Access<T> tAccess = new Access<T>().setParam(params)
                .setClazz(clazz)
                .setOperateType(OperateType.INSERT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e);
            return ListTs.get(e.getParams(), 0);
        });


    }

    private  <T, R> R exeCallback(RuntimeContext<T> access, Function<RuntimeContext<T>, R> function) {
        try {
            return function.apply(access);
        } finally {
            accessUtils.releaseConnection(access);
        }
    }

    @Override
    public <T> List<T> save(Iterable<T> params, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParams(params)
                .setClazz(clazz)
                .setOperateType(OperateType.INSERT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e);
            return e.getParams();
        });


    }

    @Override
    public <T> int delete(WhereBuild whereBuild, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e);
            return e.getEffectRows();
        });

    }

    @Override
    public <T> int deleteById(T param, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParam(param)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e);
            return e.getEffectRows();
        });

    }

    @Override
    public <T> int deleteByIds(Iterable<T> params, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParams(params)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e);

            return e.getEffectRows();
        });

    }

    @Override
    public <T> int updateById(T param, boolean isSkipNull, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParam(param)
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return updateByIdWith(context,true);

    }

    private <T> Integer updateByIdWith( RuntimeContext<T> context,boolean callback) {

        if(callback){
            return exeCallback(context, e -> {
                accessUtils.parseSql(e);
                return e.getEffectRows();
            });
        }else{
            accessUtils.parseSql(context);
            return context.getEffectRows();
        }

    }


    // 为了简单批量直接循环
    @Override
    public <T> int updateByIds(Iterable<T> params, boolean isSkipNull, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        try{
            int i = 0;
            for (T param : params) {
                context.setParams(ListTs.asList(param));
                i += updateByIdWith(context,false);
            }
            return i;
        }finally {
            accessUtils.releaseConnection(context);
        }
    }

    @Override
    public <T> int update(T params, boolean isSkipNull, WhereBuild whereBuild, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setParam(params)
                .setWhere(whereBuild)
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e);
            return e.getEffectRows();
        });

    }

    @Override
    public <T> List<T> query(String sql, Class<T> clazz, Object... args) {
        Access<T> tAccess = new Access<T>()
                .setSql(sql)
                .setArgs(ListTs.asList(args))
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e);
            return e.getResultList();
        });


    }

    @Override
    public <T> T queryOne(String sql, Class<T> clazz, Object... args) {
        Access<T> tAccess = new Access<T>()
                .setSql(sql)
                .setArgs(ListTs.asList(args))
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);

        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e);
            return ListTs.get(e.getResultList(), 0);
        });
    }

    @Override
    public <T> EasyMap<String, Object> queryMap(String sql, Object... args) {
        Access<T> tAccess = new Access<T>()
                .setSql(sql)
                .setArgs(ListTs.asList(args))
                .setReturnMap(true)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e);
            return ListTs.get(e.getResultMapList(), 0);
        });

    }

    @Override
    public EasyMap<String, Object> queryMapByTableName(String schema, String tableName, boolean resultFieldToCame, WhereBuild whereBuild) {
        Access<Object> tAccess = new Access<>()
                .setSchema(schema)
                .setTableName(tableName)
                .setResultFieldToCame(resultFieldToCame)
                .setWhere(whereBuild)
                .setReturnMap(true)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<Object> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e);
            return ListTs.get(e.getResultMapList(), 0);
        });

    }

    @Override
    public <T> List<T> query(WhereBuild whereBuild, Class<T> clazz) {

        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e);

            return e.getResultList();
        });

    }

    @Override
    public List<EasyMap<String, Object>> queryMap(WhereBuild whereBuild, boolean resultFieldToCame) {


        Access<Object> tAccess = new Access<Object>()
                .setWhere(whereBuild)
                .setResultFieldToCame(resultFieldToCame)
                .setReturnMap(true)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<Object> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e);
            return e.getResultMapList();
        });

    }

    @Override
    public <T> T queryOne(WhereBuild whereBuild, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e);
            return ListTs.get(e.getResultList(), 0);
        });

    }

    @Override
    public <T> EasyMap<String, Object> queryOneMap(WhereBuild whereBuild, Class<T> clazz, boolean resultFieldToCame) {
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setReturnMap(true)
                .setResultFieldToCame(resultFieldToCame)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e);
            return ListTs.get(e.getResultMapList(), 0);
        });

    }

    @Override
    public <T> PageRes queryPage(WhereBuild whereBuild, Page<T> page, Class<T> clazz) {
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setPage(page)
                .setOperateType(OperateType.SELECT_COUNT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e);
            long count = e.getCount();
            PageRes pageRes = new PageRes();
            pageRes.setPageNo(page.getPageNo());
            pageRes.setPageSize(page.getPageSize());
            if (count <= 0) {
                return pageRes;
            }
            pageRes.setTotal(count);
            e.setOperateType(OperateType.SELECT_PAGE);
            accessUtils.parseSql(e);
            List<T> resultList = e.getResultList();
            pageRes.setRecords(resultList);
            return pageRes;
        });


    }
}
