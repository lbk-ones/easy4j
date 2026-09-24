package easy4j.infra.dbaccess.orm.runner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.orm.AccessConfig;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.SpringOrmProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

@Slf4j
public class LogSql {

    public static void init(RuntimeContext<?> runtimeContext, Long time, Long getConnectionTime, Long paramHandlerTime) {
        LogResult logResult = new LogResult();
        logResult.setBeginTime(time);
        logResult.setGetConnectionTime(getConnectionTime);
        logResult.setParamHandlerTime(paramHandlerTime);
        runtimeContext.setLogResult(logResult);
    }

    public static void init(RuntimeContext<?> runtimeContext) {
        LogResult logResult = new LogResult();
        logResult.setBeginTime(System.currentTimeMillis());
        runtimeContext.setLogResult(logResult);
    }

    public static void exeBegin(RuntimeContext<?> runtimeContext) {
        LogResult logResult = runtimeContext.getLogResult();

        logResult.setExeBeginTime(new Date());
        runtimeContext.setLogResult(logResult);
    }

    public static void exeEnd(RuntimeContext<?> runtimeContext) {
        LogResult logResult = runtimeContext.getLogResult();
        Date exeBeginTime = logResult.getExeBeginTime();
        if (exeBeginTime != null) {
            long time = exeBeginTime.getTime();
            long l = System.currentTimeMillis() - time;
            logResult.setExeTime(l);
        }
        runtimeContext.setLogResult(logResult);
    }

    /**
     * batch模式打印
     *
     * @param runtimeContext
     * @param sql
     * @param effectRows
     */
    public static void printBath(RuntimeContext<?> runtimeContext, String sql, Integer effectRows) {
        AccessConfig config = runtimeContext.getConfig();
        boolean onlyPrintSlowSql = config.isOnlyPrintSlowSql();
        long slowSqlTime = config.getSlowSqlTime();
        boolean printSqlIs = config.isPrintSqlIs();
        if (printSqlIs) {
            return;
        }
        // 实时判断到底该不该打印sql
        try {
            Boolean property = SpringUtil.getProperty(SpringOrmProperties.ORM_PREFIX + SP.DOT + "print-sql-is", Boolean.class, true);
            if (property != null && !property) {
                return;
            }
        } catch (Exception ignored) {

        }
        LogResult logResult = runtimeContext.getLogResult();
        if (logResult == null) return;
        long exeTime = logResult.getExeTime();
        if (onlyPrintSlowSql && exeTime < slowSqlTime) {
            return;
        }
        logResult.setSql(sql);
        logResult.setCostTime(System.currentTimeMillis() - logResult.getBeginTime());
        if (log.isInfoEnabled()) {
            // #1、从最开始解析到执行完成一共的耗时时间
            // #2、获取连接的耗时
            // #3、sql真正执行的时间
            // 如果 #3 - #1 时间很大 代表前面处理sql参数获取连接等逻辑耗时过长
            // #4 这一批次执行了多少条数据
            // #5 sql 这里不打印参数拼接之后的sql
            log.info("[SQL] [{},{},{}]ms  batch {} rows => ...{}", logResult.getCostTime(), logResult.getGetConnectionTime(), exeTime, effectRows,logResult.getSql());
        }

    }

    public static void print(RuntimeContext<?> runtimeContext) {
        if (runtimeContext.isTempSkipPrintSql()) return;
        AccessConfig config = runtimeContext.getConfig();
        boolean onlyPrintSlowSql = config.isOnlyPrintSlowSql();
        long slowSqlTime = config.getSlowSqlTime();
        boolean printSqlIs = config.isPrintSqlIs();
        // 实时判断到底该不该打印sql
        try {
            Boolean property = SpringUtil.getProperty(SpringOrmProperties.ORM_PREFIX + SP.DOT + "print-sql-is", Boolean.class, true);
            if (property != null && !property) {
                return;
            }
        } catch (Exception ignored) {

        }
        if (printSqlIs) {
            try {
                LogResult logResult = runtimeContext.getLogResult();
                if (logResult == null) return;
                long exeTime = logResult.getExeTime();
                if (onlyPrintSlowSql && exeTime < slowSqlTime) {
                    return;
                }
                String sql = runtimeContext.getSql();
                List<Object> tempPrintSqlArgs = runtimeContext.getTempPrintSqlArgs();
                if (CollUtil.isEmpty(tempPrintSqlArgs)) {
                    tempPrintSqlArgs = runtimeContext.getArgs();
                }
                Dialect dialect = runtimeContext.getDialect();
                String s = SqlReplacer.replacePlaceholders(sql, tempPrintSqlArgs, dialect);

                int effectRows = runtimeContext.getEffectRows();
                if (runtimeContext.getTempEffectRows() != null) {
                    effectRows = runtimeContext.getTempEffectRows();
                }

                logResult.setSql(s);
                logResult.setCostTime(System.currentTimeMillis() - logResult.getBeginTime());
                logResult.setEffectRows(effectRows);
                if (log.isInfoEnabled()) {
                    // #1、从最开始解析到执行完成一共的耗时时间
                    // #2、获取连接的耗时
                    // #3、sql真正执行的时间
                    // 如果 #3 - #1 时间很大 代表前面处理sql参数获取连接等逻辑耗时过长
                    // #4、这里打印参数拼接之后的sql
                    log.info("[SQL] [{},{},{}]ms {} rows => {}", logResult.getCostTime(), logResult.getGetConnectionTime(), exeTime, effectRows, logResult.getSql());
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
