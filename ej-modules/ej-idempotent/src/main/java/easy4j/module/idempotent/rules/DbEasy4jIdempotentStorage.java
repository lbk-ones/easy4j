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
import easy4j.infra.context.api.idempotent.Easy4jIdempotentStorage;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.context.Easy4jContextFactory;
import easy4j.infra.context.api.lock.DbLock;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.DBAccessFactory;
import easy4j.module.idempotent.rules.datajdbc.Easy4jKeyIdempotent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

import javax.sql.DataSource;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * storage-type db
 */
@Component("dbIdempotentStorage")
@Slf4j
public class DbEasy4jIdempotentStorage implements Easy4jIdempotentStorage, InitializingBean {

    public static final String DB_EASY4J_IDEMPOTENT_KEY = "db-easy4j-idempotent-key";

    private DBAccess dbAccess;


//    @Autowired
//    Easy4jIdempotentDao easy4jIdempotentDao;

    private static final List<Easy4jKeyIdempotent> cache = ListTs.newCopyOnWriteArrayList();


    @Override
    public void afterPropertiesSet() throws Exception {
        DBAccessFactory.INIT_DB_FILE_PATH.add("db/idempotent");
        dbAccess = DBAccessFactory.getDBAccess(SpringUtil.getBean(DataSource.class));
//        schedule();
    }

    // 初始延迟十秒
//    @Scheduled(fixedRate = 1000 * 60 * 5, initialDelay = 1000 * 10)
    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void scheduleIdempotentDb() {
        DbLock dbLock = Easy4jContextFactory.sysLock();
        try {
            dbLock.lock(DB_EASY4J_IDEMPOTENT_KEY, 5, "web-idempotent-db-clear-lock");
        } catch (Exception e) {
            return;
        }
        try {
            if (!cache.isEmpty()) {
                Iterator<Easy4jKeyIdempotent> iterator = cache.iterator();
                while (iterator.hasNext()) {
                    Easy4jKeyIdempotent keyIdempotent = iterator.next();
                    if (keyIdempotent.getExpireDate().getTime() < System.currentTimeMillis()) {
                        try {
                            dbAccess.deleteByPrimaryKey(keyIdempotent, Easy4jKeyIdempotent.class);
                            iterator.remove();
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
        } finally {
            dbLock.unLock(DB_EASY4J_IDEMPOTENT_KEY);
        }

    }

    @Override
    public boolean acquireLock(String key, int expireSeconds, HttpServletRequest request) {

        try {
            if (StrUtil.isBlank(key)) {
                return true;
            }
            Easy4jKeyIdempotent easy4jKeyIdempotent = new Easy4jKeyIdempotent();
            easy4jKeyIdempotent.setIdeKey(key);
            Date date = new Date();
            long time = date.getTime() + (expireSeconds * 1000L);
            Date da = new Date(time);
            easy4jKeyIdempotent.setExpireDate(da);
            dbAccess.saveOne(easy4jKeyIdempotent, Easy4jKeyIdempotent.class);
            cache.add(easy4jKeyIdempotent);
            request.setAttribute(IS_LOCK, "1");
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
