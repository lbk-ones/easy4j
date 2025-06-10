package easy4j.module.lock.db;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.context.AutoRegisterContext;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.lock.DbLock;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.DBAccessFactory;
import easy4j.infra.dbaccess.domain.SysLock;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import java.util.Date;

@Slf4j
public class DbLockImpl implements DbLock, AutoRegisterContext {

    private final static DBAccess dbAccess = DBAccessFactory.getDBAccess(JdbcHelper.getDataSource(), false, false);

    @Override
    public void lock(String key, Integer expire) {
        lock(key, expire, "none remark");
    }

    public void lock(String key, int minutes, String remark) {
        lockWith(key, minutes, remark);
    }

    private void lockWith(String resourceId, int minutes, String remark) {

        if (StrUtil.isBlank(resourceId) || minutes < 0 || StrUtil.isBlank(remark)) {
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
            log.debug("The resource has been locked successfully:" + resourceId + ":" + remark);
        } catch (DuplicateKeyException e) {
            SysLock objectByPrimaryKey = dbAccess.selectByPrimaryKey(resourceId, SysLock.class);
            Date expireDate = objectByPrimaryKey.getExpireDate();
            // expireDate == null always lock
            if (expireDate != null && new Date().after(expireDate)) {
                log.error("Failed to lock the resource:" + resourceId + ":" + remark + ":" + "but the old lock is expired");
                unLock(resourceId);
                Date date = new Date();
                easy4jSysLock.setCreateDate(date);
                easy4jSysLock.setExpireDate(DateUtil.offsetMinute(date, 5));
                // lock again
                try {
                    dbAccess.saveOne(easy4jSysLock, SysLock.class);
                    log.debug("The resource has been locked successfully:" + resourceId + ":" + remark);
                    return;
                } catch (Exception e1) {
                    log.error("Failed to lock the resource:" + resourceId + ":" + remark);
                    throw EasyException.wrap(BusCode.A00039, resourceId, objectByPrimaryKey.getRemark());
                }
            }
            throw EasyException.wrap(BusCode.A00039, resourceId, objectByPrimaryKey.getRemark());
        }
    }

    public void unLock(String key) {
        if (StrUtil.isBlank(key)) {
            return;
        }
        SysLock sysLock = new SysLock();
        sysLock.setId(key);
        dbAccess.deleteByPrimaryKey(sysLock, SysLock.class);
    }

    @Override
    public void registerToContext(Easy4jContext easy4jContext) {
        DbLock dbLock = SpringUtil.getBean(DbLock.class);
        easy4jContext.register(dbLock);
    }
}
