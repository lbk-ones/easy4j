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
package easy4j.module.base.plugin.lock;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.plugin.dbaccess.domain.SysLock;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.utils.BusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import java.util.Date;

/**
 * 很简单的数据库锁
 * 过期之后下一次上锁会自动解锁 然后再次锁定
 *
 * @author bokun.li
 * @date 2025/5/29
 */
@Slf4j
public class Easy4jSysLock {

    private final static DBAccess dbAccess = DBAccessFactory.getDBAccess(JdbcHelper.getDataSource());

    public static void lock(String id, int minutes, String remark) {
        lockWith(id, minutes, remark);
    }

    private static void lockWith(String resourceId, int minutes, String remark) {

        if (StrUtil.isBlank(resourceId) || minutes < 0 || StrUtil.isNotBlank(remark)) {
            log.info("The resource does not meet the locking conditions " + resourceId + ":" + minutes + ":" + remark);
            return;
        }
        SysLock easy4jSysLock = new SysLock();
        easy4jSysLock.setId(resourceId);
        Date date1 = new Date();
        easy4jSysLock.setCreateDate(date1);
        easy4jSysLock.setExpireDate(DateUtil.offsetMinute(date1, minutes));
        easy4jSysLock.setRemark(remark);
        try {
            dbAccess.saveOne(easy4jSysLock, SysLock.class);
            log.info("The resource has been locked successfully:" + resourceId + ":" + remark);
        } catch (DuplicateKeyException e) {
            log.error("Failed to lock the resource:" + resourceId + ":" + remark);
            SysLock objectByPrimaryKey = dbAccess.getObjectByPrimaryKey(resourceId, SysLock.class);
            Date expireDate = objectByPrimaryKey.getExpireDate();
            // expireDate == null always lock
            if (expireDate != null && new Date().after(expireDate)) {
                unLock(resourceId);
                Date date = new Date();
                easy4jSysLock.setCreateDate(date);
                easy4jSysLock.setExpireDate(DateUtil.offsetMinute(date, 5));
                // lock again
                try {
                    dbAccess.saveOne(easy4jSysLock, SysLock.class);
                    log.info("The resource has been locked successfully:" + resourceId + ":" + remark);
                    return;
                } catch (Exception e1) {
                    log.error("Failed to lock the resource:" + resourceId + ":" + remark);
                    throw EasyException.wrap(BusCode.A00039, resourceId, objectByPrimaryKey.getRemark());
                }
            }
            throw EasyException.wrap(BusCode.A00039, resourceId, objectByPrimaryKey.getRemark());
        }
    }

    public static void unLock(String id) {
        if (StrUtil.isBlank(id)) {
            return;
        }
        dbAccess.deleteByPrimaryKey(id, SysLock.class);
    }


}
