package easy4j.infra.dbaccess.orm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dll.op.meta.DatabaseColumnMetadata;
import easy4j.infra.dbaccess.helper.DDlHelper;
import easy4j.infra.dbaccess.orm.conditions.*;
import easy4j.infra.dbaccess.domain.PageRes;
import easy4j.infra.dbaccess.orm.conditions.wd.Wd;
import lombok.Getter;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class DBAccessImpl implements IDBAccess {

    @Getter
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
            accessUtils.resolveContext(e, false);
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
            accessUtils.resolveContext(e, false);
            return e.getParams();
        });


    }

    @Override
    public <T> int deleteAll(Class<T> clazz) {
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setClazz(clazz)
                .setWhere(WhereBuild.get())
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.resolveContext(e, false);
            return e.getEffectRows();
        });
    }

    @Override
    public <T> int delete(IWhere whereBuild, Class<T> clazz) {
        if (whereBuild == null) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {

            accessUtils.resolveContext(e, false);
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
        IWhere whereBuild = idEq(context);
        if (whereBuild == null) return 0;
        return deleteByIdWith(context, true);

    }

    @Override
    public <T> int deleteByPrimaryKey(Serializable key, Class<T> clazz) {
        if (key == null) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setPrimaryKey(key)
                .setClazz(clazz)
                .setOperateType(OperateType.DELETE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        IWhere whereBuild = idEq(context);
        if (whereBuild == null) return 0;
        return deleteByIdWith(context, true);
    }

    private <T> Integer deleteByIdWith(RuntimeContext<T> context, boolean callback) {
        if (callback) {
            return exeCallback(context, e -> {
                accessUtils.resolveContext(e, false);
                return e.getEffectRows();
            });
        } else {
            accessUtils.resolveContext(context, false);
            return context.getEffectRows();
        }

    }

    public <T> IWhere idEq(RuntimeContext<T> context) {
        List<AccessField> columnInfoList = context.getIdList();
        IWhere whereBuild = WhereBuild.get();
        columnInfoList.forEach(e -> {
            whereBuild.getWhere().ifPresent(e2 -> e2.eq(e.getColumnName(), Wd.value(e.getColumnValue())));
        });
        List<Condition> conditions = whereBuild.getWhere().orElseThrow().getConditions();
        if (conditions.isEmpty()) {
            return null;
        }
        context.getAccess().setWhere(whereBuild);
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
        try {
            Iterator<T> iterator = params.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                T next = iterator.next();
                context.setParams(ListTs.asList(next));
                accessUtils.refreshContextByParam(context, next);
                IWhere whereBuild = idEq(context);
                if (whereBuild == null) continue;
                i += deleteByIdWith(context, false);
            }
            return i;
        } finally {
            accessUtils.releaseConnection(context);
        }
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
        IWhere whereBuild = idEq(context);
        if (whereBuild == null) return 0;
        return updateByIdWith(context, true);

    }

    private <T> Integer updateByIdWith(RuntimeContext<T> context, boolean callback) {

        if (callback) {
            return exeCallback(context, e -> {
                accessUtils.resolveContext(e, false);
                return e.getEffectRows();
            });
        } else {
            accessUtils.resolveContext(context, false);
            return context.getEffectRows();
        }

    }

    @Override
    public <T> int dynamicUpdate(List<EasyMap<String, Object>> value, String tableName, String schema, boolean isSkipNull) {
        if (CollUtil.isEmpty(value)) return 0;
        if (StrUtil.isBlank(tableName)) return 0;
        Access<T> tAccess = new Access<T>()
                .setMapParams(value)
                .setTableName(tableName)
                .setSchema(schema)
                .setSkipNullIs(isSkipNull)
                .setOperateType(OperateType.UPDATE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        try {
            int i = 0;
            for (EasyMap<String, Object> param : value) {
                accessUtils.refreshContextByMap(context, param);
                IWhere whereBuild = idEq(context);
                if (whereBuild == null) return 0;
                i += updateByIdWith(context, false);
            }
            return i;
        } finally {
            accessUtils.releaseConnection(context);
        }
    }

    @Override
    public <T> int dynamicSave(List<EasyMap<String, Object>> value, String tableName, String schema) {
        if (CollUtil.isEmpty(value)) return 0;
        if (StrUtil.isBlank(tableName)) return 0;
        Access<T> tAccess = new Access<T>()
                .setMapParams(value)
                .setTableName(tableName)
                .setSchema(schema)
                .setOperateType(OperateType.INSERT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.resolveContext(e, false);
            return e.getEffectRows();
        });
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
                IWhere whereBuild = idEq(context);
                if (whereBuild == null) return 0;
                i += updateByIdWith(context, false);
            }
            return i;
        } finally {
            accessUtils.releaseConnection(context);
        }
    }

    @Override
    public <T> int update(T params, boolean isSkipNull, IWhere whereBuild, Class<T> clazz) {
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

            accessUtils.resolveContext(e, false);
            return e.getEffectRows();
        });

    }

    @Override
    public <T> int update(IUpdateBuild updateBuild, Class<T> clazz) {
        if (updateBuild == null) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setUpdate(updateBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.UPDATE);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.resolveContext(e, false);
            return e.getEffectRows();
        });
    }

    @Override
    public <T> List<T> queryJoin(SqlWrapper sql, Class<T> clazz) {
        List<T> empty = new ArrayList<>();
        if (clazz == null) return empty;
        if (sql == null) return empty;
        Access<T> tAccess = new Access<T>()
                .setSqlWrapper(sql)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT_JOIN);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.resolveContext(e, false);
            return e.getResultList();
        });
    }

    @Override
    public <T> List<EasyMap<String, Object>> queryMapJoin(SqlWrapper sql, boolean resultFieldToCame) {
        List<EasyMap<String, Object>> empty = new ArrayList<>();
        if (sql == null) return empty;
        Access<T> tAccess = new Access<T>()
                .setSqlWrapper(sql)
                .setReturnMap(true)
                .setResultFieldToCame(resultFieldToCame)
                .setOperateType(OperateType.SELECT_JOIN);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.resolveContext(e, false);
            return e.getResultMapList();
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
            accessUtils.resolveContext(e, true);
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
            accessUtils.resolveContext(e, true);
            return ListTs.get(e.getResultList(), 0);
        });
    }

    @Override
    public <T> List<EasyMap<String, Object>> queryMapListBySql(String sql, boolean resultFieldToCame, Object... args) {
        if (StrUtil.isBlank(sql)) return null;
        Access<T> tAccess = new Access<T>()
                .setSql(sql)
                .setArgs(ListTs.asList(args))
                .setResultFieldToCame(resultFieldToCame)
                .setReturnMap(true)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.resolveContext(e, true);
            return e.getResultMapList();
        });

    }

    @Override
    public EasyMap<String, Object> queryMapByTableName(String schema, String tableName, boolean resultFieldToCame, IWhere whereBuild, boolean queryRealFields) {
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

            accessUtils.resolveContext(e, false);
            return ListTs.get(e.getResultMapList(), 0);
        });

    }

    @Override
    public PageRes queryPageByTableName(String schema, String tableName, boolean resultFieldToCame, IWhere whereBuild, boolean queryRealFields, Page<Object> page) {
        if (StrUtil.isBlank(tableName)) return new PageRes();
        Access<Object> tAccess = new Access<>()
                .setSchema(schema)
                .setPage(page)
                .setTableName(tableName)
                .setResultFieldToCame(resultFieldToCame)
                .setWhere(whereBuild)
                .setReturnMap(true)
                .setOperateType(OperateType.SELECT);
        if (page != null) {
            tAccess.setOperateType(OperateType.SELECT_COUNT);
            RuntimeContext<Object> context = accessUtils.toContext(tAccess);
            List<Condition> selectFields = whereBuild.getSelectFields();
            flushRealFields(schema, tableName, whereBuild, queryRealFields, selectFields, context);
            return exeCallback(context, e -> {
                e.setSkipTail(true);
                accessUtils.resolveContext(e, false);
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

                accessUtils.resolveContext(e, false);
                List<EasyMap<String, Object>> resultMapList = e.getResultMapList();
                pageRes.setRecords(resultMapList);
                return pageRes;
            });
        } else {
            List<EasyMap<String, Object>> stringObjectEasyMap = queryMapListByTableName(schema, tableName, resultFieldToCame, whereBuild, queryRealFields);
            PageRes pageRes = new PageRes();
            pageRes.setRecords(stringObjectEasyMap);
            return pageRes;
        }
    }

    @Override
    public List<EasyMap<String, Object>> queryMapListByTableName(String schema, String tableName, boolean resultFieldToCame, IWhere whereBuild, boolean queryRealFields) {
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

            accessUtils.resolveContext(e, false);
            return e.getResultMapList();
        });

    }

    private static void flushRealFields(String schema, String tableName, IWhere whereBuild, boolean queryRealFields, List<Condition> selectFields, RuntimeContext<Object> context) {
        // 如果没字段则把字段查出来
        if (selectFields.isEmpty() && queryRealFields) {
            Dialect dialect = context.getDialect();
            Connection connection = context.getConnection();
            String catalog = null;
            try {
                catalog = connection.getCatalog();
            } catch (SQLException ignored) {
            }
            // 不带缓存
            List<DatabaseColumnMetadata> columnsNoCacheQuiet = dialect.getColumnsNoCacheQuiet(catalog, schema, tableName);
            for (DatabaseColumnMetadata databaseColumnMetadata : columnsNoCacheQuiet) {
                whereBuild.getWhere().orElseThrow().select(databaseColumnMetadata.getColumnName());
            }
        }
    }

    @Override
    public <T> List<T> query(IWhere whereBuild, Class<T> clazz) {
        List<T> empty = new ArrayList<>();
        if (clazz == null) return empty;
        if (whereBuild == null) return empty;
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {

            accessUtils.resolveContext(e, false);

            return e.getResultList();
        });

    }

    @Override
    public <T> List<T> queryAll(Class<T> clazz) {
        List<T> empty = new ArrayList<>();
        if (clazz == null) return empty;
        Access<T> tAccess = new Access<T>()
                .setWhere(WhereBuild.get())
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {
            accessUtils.resolveContext(e, false);
            return e.getResultList();
        });
    }

    @Override
    public <T> T queryOne(IWhere whereBuild, Class<T> clazz) {
        if (whereBuild == null) return null;
        if (clazz == null) return null;
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {

            accessUtils.resolveContext(e, false);
            return ListTs.get(e.getResultList(), 0);
        });

    }

    @Override
    public <T> long count(IWhere whereBuild, Class<T> clazz) {
        if (whereBuild == null) return 0;
        if (clazz == null) return 0;
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT_COUNT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {

            accessUtils.resolveContext(e, false);
            return e.getCount();
        });
    }

    @Override
    public <T> boolean exists(IWhere whereBuild, Class<T> clazz) {
        if (whereBuild == null) return false;
        if (clazz == null) return false;
        Access<T> tAccess = new Access<T>()
                .setWhere(whereBuild)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT_EXIST);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        return exeCallback(context, e -> {

            accessUtils.resolveContext(e, false);
            return e.isExists();
        });
    }

    @Override
    public <T> EasyMap<String, Object> queryOneMap(IWhere whereBuild, Class<T> clazz, boolean resultFieldToCame) {
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

            accessUtils.resolveContext(e, false);
            return ListTs.get(e.getResultMapList(), 0);
        });

    }

    @Override
    public <T> PageRes queryPage(IWhere whereBuild, Page<T> page, Class<T> clazz) {
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

            accessUtils.resolveContext(e, false);
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

            accessUtils.resolveContext(e, false);
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
        IWhere whereBuild = idEq(context);
        if (whereBuild == null) return null;
        return exeCallback(context, e -> {
            accessUtils.resolveContext(e, false);
            return ListTs.get(e.getResultList(), 0);
        });
    }

    @Override
    public <T> T queryByPrimaryKey(Serializable primaryKey, Class<T> clazz) {
        if (primaryKey == null) return null;
        if (clazz == null) return null;
        Access<T> tAccess = new Access<T>()
                .setPrimaryKey(primaryKey)
                .setClazz(clazz)
                .setOperateType(OperateType.SELECT);
        RuntimeContext<T> context = accessUtils.toContext(tAccess);
        IWhere whereBuild = idEq(context);
        if (whereBuild == null) return null;
        return exeCallback(context, e -> {
            accessUtils.resolveContext(e, false);
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
            accessUtils.resolveContext(e, false);
            return e.getEffectRows();
        });
    }
}
