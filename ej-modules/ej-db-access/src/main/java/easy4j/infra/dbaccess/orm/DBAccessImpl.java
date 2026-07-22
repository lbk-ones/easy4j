package easy4j.infra.dbaccess.orm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.DatabaseColumnMetadata;
import easy4j.infra.dbaccess.helper.DDlHelper;
import easy4j.infra.dbaccess.orm.conditions.Condition;
import easy4j.infra.dbaccess.orm.conditions.UpdateBuild;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import easy4j.infra.dbaccess.domain.PageRes;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
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
    public Connection getConnection() {
        return accessUtils.getConnection();
    }

    @Override
    public void runScript(Connection connection, String ddlSql, List<String> path, boolean isCloseConnection) throws IOException {
        DDlHelper.execDDL(connection == null ? getConnection() : connection, ddlSql, path, isCloseConnection);
    }

    @Override
    public <T> T save(T params, Class<T> clazz) {
        if (params == null) return null;
        if (clazz == null) return null;
        Access<T> tAccess = new Access<T>()
                .setParam(params)
                .setClazz(clazz)
                .setOperateType(OperateType.INSERT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e, false);
            return ListTs.get(e.getParams(), 0);
        });
    }

    private <T, R> R exeCallback(RuntimeContext<T> access, Function<RuntimeContext<T>, R> function) {
        try {
            return function.apply(access);
        } finally {
            accessUtils.releaseConnection(access);
        }
    }

    @Override
    public <T> List<T> save(Iterable<T> params, Class<T> clazz) {
        List<T> empty = new ArrayList<>();
        if (CollUtil.isEmpty(params)) return empty;
        if (clazz == null) return empty;
        Access<T> tAccess = new Access<T>()
                .setParams(params)
                .setClazz(clazz)
                .setOperateType(OperateType.INSERT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e, false);
            return e.getParams();
        });


    }

    @Override
    public <T> int deleteAll(Class<T> clazz) {
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e, false);
            return e.getEffectRows();
        });
    }

    @Override
    public <T> int delete(WhereBuild whereBuild, Class<T> clazz) {
        if (whereBuild == null) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            return e.getEffectRows();
        });

    }

    @Override
    public <T> int deleteById(T param, Class<T> clazz) {
        if (param == null) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setParam(param)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        WhereBuild whereBuild = idEq(context);
        if (whereBuild == null) return 0;
        return deleteByIdWith(context, whereBuild, true);

    }

    private <T> Integer deleteByIdWith(RuntimeContext<T> context, WhereBuild whereBuild, boolean callback) {
        if (callback) {
            return exeCallback(context, e -> {
                accessUtils.parseWhere(whereBuild, e);
                accessUtils.parseSql(e, false);
                return e.getEffectRows();
            });
        } else {
            accessUtils.parseWhere(whereBuild, context);
            accessUtils.parseSql(context, false);
            return context.getEffectRows();
        }

    }

    public <T> WhereBuild idEq(RuntimeContext<T> context) {
        List<AccessField> columnInfoList = context.getIdList();
        WhereBuild whereBuild = WhereBuild.get();
        columnInfoList.forEach(e -> {
            whereBuild.eq(e.getColumnName(), e.getColumnValue());
        });
        List<Condition> conditions = whereBuild.getConditions();
        if (conditions.isEmpty()) {
            return null;
        }
        return whereBuild;
    }

    @Override
    public <T> int deleteByIds(Iterable<T> params, Class<T> clazz) {
        if (CollUtil.isEmpty(params)) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        Iterator<T> iterator = params.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            T next = iterator.next();
            context.setParams(ListTs.asList(next));
            accessUtils.refreshContextByParam(context, next);
            WhereBuild whereBuild = idEq(context);
            if (whereBuild == null) continue;
            i += deleteByIdWith(context, whereBuild, false);
        }
        return i;
    }

    @Override
    public <T> int updateById(T param, boolean isSkipNull, Class<T> clazz) {
        if (param == null) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setParam(param)
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);

        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        WhereBuild whereBuild = idEq(context);
        if (whereBuild == null) return 0;
        accessUtils.parseWhere(whereBuild, context);
        return updateByIdWith(context, true);

    }

    private <T> Integer updateByIdWith(RuntimeContext<T> context, boolean callback) {

        if (callback) {
            return exeCallback(context, e -> {
                accessUtils.parseSql(e, false);
                return e.getEffectRows();
            });
        } else {
            accessUtils.parseSql(context, false);
            return context.getEffectRows();
        }

    }


    // 为了简单批量直接循环
    @Override
    public <T> int updateByIds(Iterable<T> params, boolean isSkipNull, Class<T> clazz) {
        if (CollUtil.isEmpty(params)) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        try {
            int i = 0;
            for (T param : params) {
                context.setParams(ListTs.asList(param));
                accessUtils.refreshContextByParam(context, param);
                WhereBuild whereBuild = idEq(context);
                if (whereBuild == null) return 0;
                accessUtils.parseWhere(whereBuild, context);

                i += updateByIdWith(context, false);
            }
            return i;
        } finally {
            accessUtils.releaseConnection(context);
        }
    }

    @Override
    public <T> int update(T params, boolean isSkipNull, WhereBuild whereBuild, Class<T> clazz) {
        if (params == null) return 0;
        if (clazz == null) return 0;
        if (whereBuild == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setParam(params)
                .setWhere(whereBuild)
                .setSkipNullIs(isSkipNull)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            return e.getEffectRows();
        });

    }

    @Override
    public <T> int update(UpdateBuild updateBuild, Class<T> clazz) {
        if (updateBuild == null) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setUpdate(updateBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseUpdate(updateBuild, e);
            accessUtils.parseSql(e, false);
            return e.getEffectRows();
        });
    }

    @Override
    public <T> List<T> query(String sql, Class<T> clazz, Object... args) {
        List<T> empty = new ArrayList<>();
        if (StrUtil.isBlank(sql)) return empty;
        if (clazz == null) return empty;
        Access<T> tAccess = new Access<T>()
                .setSql(sql)
                .setArgs(ListTs.asList(args))
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e, true);
            return e.getResultList();
        });


    }

    @Override
    public <T> T queryOne(String sql, Class<T> clazz, Object... args) {
        if (StrUtil.isBlank(sql)) return null;
        if (clazz == null) return null;
        Access<T> tAccess = new Access<T>()
                .setSql(sql)
                .setArgs(ListTs.asList(args))
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);

        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e, true);
            return ListTs.get(e.getResultList(), 0);
        });
    }

    @Override
    public <T> EasyMap<String, Object> queryMapListBySql(String sql, Object... args) {
        if (StrUtil.isBlank(sql)) return null;
        Access<T> tAccess = new Access<T>()
                .setSql(sql)
                .setArgs(ListTs.asList(args))
                .setReturnMap(true)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e, true);
            return ListTs.get(e.getResultMapList(), 0);
        });

    }

    @Override
    public EasyMap<String, Object> queryMapByTableName(String schema, String tableName, boolean resultFieldToCame, WhereBuild whereBuild, boolean queryRealFields) {
        if (StrUtil.isBlank(tableName)) return EasyMap.get();
        if (whereBuild == null) return EasyMap.get();
        Access<Object> tAccess = new Access<>()
                .setSchema(schema)
                .setTableName(tableName)
                .setResultFieldToCame(resultFieldToCame)
                .setWhere(whereBuild)
                .setReturnMap(true)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<Object> context = accessUtils.toContext(tAccess);
        List<Condition> selectFields = whereBuild.getSelectFields();
        flushRealFields(schema, tableName, whereBuild, queryRealFields, selectFields, context);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            return ListTs.get(e.getResultMapList(), 0);
        });

    }

    @Override
    public List<EasyMap<String, Object>> queryMapListByTableName(String schema, String tableName, boolean resultFieldToCame, WhereBuild whereBuild, boolean queryRealFields) {
        List<EasyMap<String, Object>> empty = new ArrayList<>();
        if (whereBuild == null) return empty;
        Access<Object> tAccess = new Access<>()
                .setWhere(whereBuild)
                .setResultFieldToCame(resultFieldToCame)
                .setTableName(tableName)
                .setSchema(schema)
                .setReturnMap(true)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<Object> context = accessUtils.toContext(tAccess);
        List<Condition> selectFields = whereBuild.getSelectFields();
        flushRealFields(schema, tableName, whereBuild, queryRealFields, selectFields, context);

        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            return e.getResultMapList();
        });

    }

    private static void flushRealFields(String schema, String tableName, WhereBuild whereBuild, boolean queryRealFields, List<Condition> selectFields, RuntimeContext<Object> context) {
        // 如果没字段则把字段查出来
        if (selectFields.isEmpty() && queryRealFields) {
            DialectV2 dialectV2 = context.getDialectV2();
            Connection connection = context.getConnection();
            String catalog = null;
            try {
                catalog = connection.getCatalog();
            } catch (SQLException ignored) {
            }
            // 不带缓存
            List<DatabaseColumnMetadata> columnsNoCacheQuiet = dialectV2.getColumnsNoCacheQuiet(catalog, schema, tableName);
            for (DatabaseColumnMetadata databaseColumnMetadata : columnsNoCacheQuiet) {
                whereBuild.select(databaseColumnMetadata.getColumnName());
            }
        }
    }

    @Override
    public <T> List<T> query(WhereBuild whereBuild, Class<T> clazz) {
        List<T> empty = new ArrayList<>();
        if (clazz == null) return empty;
        if (whereBuild == null) return empty;
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);

            return e.getResultList();
        });

    }

    @Override
    public <T> List<T> queryAll(Class<T> clazz) {
        List<T> empty = new ArrayList<>();
        if (clazz == null) return empty;
        Access<T> tAccess = new Access<T>()
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e, false);
            return e.getResultList();
        });
    }

    @Override
    public <T> T queryOne(WhereBuild whereBuild, Class<T> clazz) {
        if (whereBuild == null) return null;
        if (clazz == null) return null;
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            return ListTs.get(e.getResultList(), 0);
        });

    }

    @Override
    public <T> long count(WhereBuild whereBuild, Class<T> clazz) {
        if (whereBuild == null) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT_COUNT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            return e.getCount();
        });
    }

    @Override
    public <T> boolean exists(WhereBuild whereBuild, Class<T> clazz) {
        if (whereBuild == null) return false;
        if (clazz == null) return false;
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT_EXIST);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            return e.isExists();
        });
    }

    @Override
    public <T> EasyMap<String, Object> queryOneMap(WhereBuild whereBuild, Class<T> clazz, boolean resultFieldToCame) {
        if (whereBuild == null) return EasyMap.get();
        if (clazz == null) return EasyMap.get();
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setReturnMap(true)
                .setResultFieldToCame(resultFieldToCame)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            return ListTs.get(e.getResultMapList(), 0);
        });

    }

    @Override
    public <T> PageRes queryPage(WhereBuild whereBuild, Page<T> page, Class<T> clazz) {
        if (whereBuild == null) return new PageRes();
        if (clazz == null) return new PageRes();
        if (page == null) return new PageRes();
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setPage(page)
                .setOperateType(OperateType.SELECT_COUNT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            e.setSkipTail(true);
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            long count = e.getCount();
            PageRes pageRes = new PageRes();
            pageRes.setPageNo(page.getPageNo());
            pageRes.setPageSize(page.getPageSize());
            if (count <= 0) {
                return pageRes;
            }
            pageRes.setTotal(count);
            e.setOperateType(OperateType.SELECT_PAGE);
            e.setSkipTail(false);
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            List<T> resultList = e.getResultList();
            pageRes.setRecords(resultList);
            return pageRes;
        });


    }

    @Override
    public <T> T queryById(T param, Class<T> clazz) {
        if (param == null) return null;
        if (clazz == null) return null;
        Access<T> tAccess = new Access<T>()
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        WhereBuild whereBuild = idEq(context);
        if (whereBuild == null) return null;
        return exeCallback(context, e -> {
            accessUtils.parseWhere(whereBuild, e);
            accessUtils.parseSql(e, false);
            return ListTs.get(e.getResultList(), 0);
        });
    }

    @Override
    public <T> int truncate(Class<T> clazz) {
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setClazz(clazz)
                .setOperateType(OperateType.TRUNCATE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.parseSql(e, false);
            return e.getEffectRows();
        });
    }
}
