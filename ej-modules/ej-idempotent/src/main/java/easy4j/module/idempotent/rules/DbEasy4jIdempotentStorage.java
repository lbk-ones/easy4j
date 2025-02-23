package easy4j.module.idempotent.rules;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.plugin.idempotent.Easy4jIdempotentStorage;
import easy4j.module.base.starter.EnvironmentHolder;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SqlFileExecute;
import easy4j.module.base.utils.SysLog;
import easy4j.module.idempotent.rules.datajdbc.Easy4jIdempotentDao;
import easy4j.module.idempotent.rules.datajdbc.Easy4jKeyIdempotent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * storage-type db
 */
@Component("dbIdempotentStorage")
@ConditionalOnBean(value = {DataSource.class,JdbcTemplate.class})
@Slf4j
public class DbEasy4jIdempotentStorage implements Easy4jIdempotentStorage, InitializingBean {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Easy4jIdempotentDao easy4jIdempotentDao;

    private static final List<Easy4jKeyIdempotent> cache = ListTs.newArrayList();


    @Override
    public void afterPropertiesSet() throws Exception {
        String dbType = EnvironmentHolder.getDbType().toLowerCase();

        String classPathSqlName = "";
        if(StrUtil.equals(dbType,"h2")){
            classPathSqlName = "idempotent_h2.sql";
        }else if(StrUtil.contains(dbType,"sqlserver")){
            classPathSqlName = "idempotent_sqlserver.sql";
        }else if(StrUtil.equals(dbType,"postgresql")){
            classPathSqlName = "idempotent_postgresql.sql";
        }else if(StrUtil.equals(dbType,"mysql")){
            classPathSqlName = "idempotent_mysql.sql";
        }else if(StrUtil.equals(dbType,"oracle")){
            classPathSqlName = "idempotent_oracle.sql";
        }
        if(StrUtil.isNotBlank(classPathSqlName)){
            try{
                SqlFileExecute.executeSqlFile(jdbcTemplate,classPathSqlName);
            }catch (Exception ignored){
                log.info(SysLog.compact("幂等表已创建"));
            }
        }

        schedule();
    }

    private void schedule() {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    if (!Thread.currentThread().isInterrupted()) {
                        TimeUnit.MINUTES.sleep(10L);
                        if (!cache.isEmpty()) {
                            for (Easy4jKeyIdempotent keyIdempotent : cache) {
                                if (keyIdempotent.getExpireDate().getTime() < System.currentTimeMillis()) {
                                    try {
                                        easy4jIdempotentDao.deleteById(keyIdempotent.getIdeKey());
                                    } catch (Exception e) {
                                        log.error(SysLog.compact("幂等表删除失败"), e);
                                    }
                                }
                            }
                        }else{
                            Iterable<Easy4jKeyIdempotent> all = easy4jIdempotentDao.findAll();
                            for (Easy4jKeyIdempotent keyIdempotent : all) {
                                if (keyIdempotent.getExpireDate().getTime() < System.currentTimeMillis()) {
                                    try {
                                        easy4jIdempotentDao.deleteById(keyIdempotent.getIdeKey());
                                    } catch (Exception e) {
                                        log.error(SysLog.compact("幂等表删除失败"), e);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException ignored) {
                // elegant interrupt thread
                Thread.currentThread().interrupt();
            }catch (Exception e){
                log.error("idempotent schedule error:", e);
            }

        });
        thread.setDaemon(true);
        thread.setName("delete-expired-idempotent-records-thread");
        thread.start();
    }

    @Override
    public boolean acquireLock(String key, int expireSeconds) {

        try{
            Easy4jKeyIdempotent easy4jKeyIdempotent = new Easy4jKeyIdempotent();
            easy4jKeyIdempotent.setIdeKey(key);
            Date date = new Date();
            long time = date.getTime()+(expireSeconds * 1000L);
            Date da = new Date(time);
            easy4jKeyIdempotent.setExpireDate(da);
            easy4jIdempotentDao.save(easy4jKeyIdempotent);
            cache.add(easy4jKeyIdempotent);
        }catch (DuplicateKeyException e){
            return false;
        }
        return true;
    }

    @Override
    public void releaseLock(String key) {
        if(StrUtil.isEmpty(key))return;
        easy4jIdempotentDao.deleteById(key);
        cache.removeIf(e->e.getIdeKey().equals(key));
    }
}
