package easy4j.infra.dbaccess.orm.runner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.orm.AccessConfig;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

@Slf4j
public class LogSql {


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


    public static void print(RuntimeContext<?> runtimeContext) {
        if (runtimeContext.isTempSkipPrintSql()) return;
        AccessConfig config = runtimeContext.getConfig();
        boolean onlyPrintSlowSql = config.isOnlyPrintSlowSql();
        long slowSqlTime = config.getSlowSqlTime();
        boolean printSqlIs = config.isPrintSqlIs();
        try {
            Boolean property = SpringUtil.getProperty("easy4j.db.access.sql.print", boolean.class, true);
            if (property == false) return;
        } catch (Exception ignored) {

        }
        if (printSqlIs) {
            try {
                LogResult logResult = runtimeContext.getLogResult();
                if (logResult == null) return;
                long exeTime = logResult.getExeTime();
                if(onlyPrintSlowSql && exeTime < slowSqlTime){
                    return;
                }
                String sql = runtimeContext.getSql();
                List<Object> tempPrintSqlArgs = runtimeContext.getTempPrintSqlArgs();
                if (CollUtil.isEmpty(tempPrintSqlArgs)) {
                    tempPrintSqlArgs = runtimeContext.getArgs();
                }
                DialectV2 dialectV2 = runtimeContext.getDialectV2();
                String s = SqlReplacer.replacePlaceholders(sql, tempPrintSqlArgs, dialectV2);

                int effectRows = runtimeContext.getEffectRows();
                if (runtimeContext.getTempEffectRows() != null) {
                    effectRows = runtimeContext.getTempEffectRows();
                }

                logResult.setSql(s);
                logResult.setCostTime(System.currentTimeMillis() - logResult.getBeginTime());
                logResult.setEffectRows(effectRows);
                if (log.isInfoEnabled()) {
                    log.info("[SQL] [{}ms {}ms] {} rows => {}", logResult.getCostTime(), exeTime, effectRows, logResult.getSql());
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
