package easy4j.infra.dbaccess.orm.runner;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.dbaccess.BeanPropertyHandler;
import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SqlRunner {



    public SqlRunner() {
    }

    public <T> void run(RuntimeContext<T> context) {
        Connection connection = context.getConnection();
        AccessUtils accessUtils = context.getAccessUtils();
        accessUtils.assertNotNull(connection,"connection");

        JdbcUtils jdbcUtils = new JdbcUtils(connection);
        Class<T> clazz = context.getClazz();
        OperateType operateType = context.getOperateType();
        PsRes psRes = null;
        try{
            // query
            if (OperateType.SELECT == operateType || OperateType.SELECT_PAGE == operateType  || OperateType.SELECT_JOIN == operateType) {
                List<T> handle;
                LogSql.exeBegin(context);
                psRes = jdbcUtils.query(context);
                LogSql.exeEnd(context);
                try {
                    // 返回map
                    if(context.isReturnMap()){
                        MapListHandler mapHandler = new MapListHandler();
                        List<Map<String, Object>> handle1 = mapHandler.handle(psRes.getResultSet());
                        List<EasyMap<String, Object>> list = new ArrayList<>();
                        if (context.isResultFieldToCamel()) {
                            for (Map<String, Object> stringObjectMap : handle1) {
                                EasyMap<String, Object> objectObjectEasyMap = EasyMap.get();
                                Set<Map.Entry<String, Object>> entries = stringObjectMap.entrySet();
                                for (Map.Entry<String, Object> entry : entries) {
                                    String key = entry.getKey();
                                    Object value = entry.getValue();
                                    objectObjectEasyMap.put(StrUtil.toCamelCase(key.toLowerCase()),value);
                                }
                                list.add(objectObjectEasyMap);
                            }
                        }else{
                            list = handle1.stream().map(EasyMap::of).toList();
                        }
                        context.setResultMapList(list);
                        context.setEffectRows(list.size());
                    }else{
                        BeanPropertyHandler<T> tBeanListHandler = new BeanPropertyHandler<>(clazz);
                        handle = tBeanListHandler.handle(psRes.getResultSet());
                        context.setResultList(handle);
                        context.setEffectRows(handle.size());
                    }
                } catch (SQLException e) {
                    throw AccessUtils.translate("select_run",context.getSql(),e,context.getConfig().getDataSource());

                }
            }else if(OperateType.SELECT_COUNT == operateType ){
                ScalarHandler<Object> tBeanListHandler = new ScalarHandler<>(1);
                Long count;
                LogSql.exeBegin(context);
                psRes = jdbcUtils.query(context);
                LogSql.exeEnd(context);
                try {
                    Object handle = tBeanListHandler.handle(psRes.getResultSet());
                    count = Convert.convert(Long.class,handle);
                } catch (SQLException e) {
                    throw AccessUtils.translate("select_count",context.getSql(),e,context.getConfig().getDataSource());

                }
                context.setCount(count);
                context.setEffectRows(Math.toIntExact(count));
            }else if(OperateType.SELECT_EXIST == operateType){
                ScalarHandler<Object> tBeanListHandler = new ScalarHandler<>(1);
                Long count;
                LogSql.exeBegin(context);
                psRes = jdbcUtils.query(context);
                LogSql.exeEnd(context);
                try {
                    Object handle = tBeanListHandler.handle(psRes.getResultSet());
                    count = Convert.convert(Long.class,handle);
                } catch (SQLException e) {
                    throw AccessUtils.translate("select_exist",context.getSql(),e,context.getConfig().getDataSource());
                }
                context.setExists(count>0);
                context.setEffectRows(Math.toIntExact(count));
            } else {
                LogSql.exeBegin(context);
                psRes = jdbcUtils.update(context);
                LogSql.exeEnd(context);
                context.setEffectRows(psRes.getEffectRows());
            }
            LogSql.print(context);
        }finally {
            accessUtils.close(psRes);
        }

    }
}
