/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.idempotent.rules;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.plugin.idempotent.Easy4jIdempotentStorage;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SysLog;
import easy4j.module.idempotent.rules.datajdbc.Easy4jKeyIdempotent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * storage-type db
 */
@Component("dbIdempotentStorage")
@Slf4j
public class DbEasy4jIdempotentStorage implements Easy4jIdempotentStorage, InitializingBean {

    private DBAccess dbAccess;


//    @Autowired
//    Easy4jIdempotentDao easy4jIdempotentDao;

    private static final List<Easy4jKeyIdempotent> cache = ListTs.newArrayList();


    @Override
    public void afterPropertiesSet() throws Exception {
        DBAccessFactory.INIT_DB_FILE_PATH.add("db/idempotent");
        dbAccess = DBAccessFactory.getDBAccess(SpringUtil.getBean(DataSource.class));
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
                                        dbAccess.deleteByPrimaryKey(keyIdempotent, Easy4jKeyIdempotent.class);
                                    } catch (Exception e) {
                                        log.error(SysLog.compact("幂等表删除失败"), e);
                                    }
                                }
                            }
                        } else {
                            List<Easy4jKeyIdempotent> all = dbAccess.selectAll(Easy4jKeyIdempotent.class);
                            //Iterable<Easy4jKeyIdempotent> all = easy4jIdempotentDao.findAll();
                            for (Easy4jKeyIdempotent keyIdempotent : all) {
                                if (keyIdempotent.getExpireDate().getTime() < System.currentTimeMillis()) {
                                    try {
                                        dbAccess.deleteByPrimaryKey(keyIdempotent, Easy4jKeyIdempotent.class);
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
            } catch (Exception e) {
                log.error("idempotent schedule error:", e);
            }

        });
        thread.setDaemon(true);
        thread.setName("delete-expired-idempotent-records-thread");
        thread.start();
    }

    @Override
    public boolean acquireLock(String key, int expireSeconds) {

        try {
            Easy4jKeyIdempotent easy4jKeyIdempotent = new Easy4jKeyIdempotent();
            easy4jKeyIdempotent.setIdeKey(key);
            Date date = new Date();
            long time = date.getTime() + (expireSeconds * 1000L);
            Date da = new Date(time);
            easy4jKeyIdempotent.setExpireDate(da);
            dbAccess.saveOne(easy4jKeyIdempotent, Easy4jKeyIdempotent.class);
            cache.add(easy4jKeyIdempotent);
        } catch (DuplicateKeyException e) {
            return false;
        }
        return true;
    }

    @Override
    public void releaseLock(String key) {
        if (StrUtil.isEmpty(key)) return;
        Easy4jKeyIdempotent easy4jKeyIdempotent = new Easy4jKeyIdempotent();
        easy4jKeyIdempotent.setIdeKey(key);
        dbAccess.deleteByPrimaryKey(easy4jKeyIdempotent, Easy4jKeyIdempotent.class);
        cache.removeIf(e -> e.getIdeKey().equals(key));
    }
}
