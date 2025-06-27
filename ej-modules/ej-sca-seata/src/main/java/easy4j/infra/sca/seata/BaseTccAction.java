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
package easy4j.infra.sca.seata;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.lock.DbLock;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * BaseTccAction
 * Tcc继承父类
 *
 * @author bokun.li
 * @date 2025/6/27
 */
public class BaseTccAction {


    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Desc("日志记录")
    protected void logTx(BusinessActionContext context, String actionType) {
        String xid = context.getXid();
        long branchId = context.getBranchId();
        String actionName = context.getActionName();
        logger.info(actionType + "--> xid{} ,branchId{} ,actionName{} ", xid, branchId, actionName);
    }

    @Desc("简单的幂等锁")
    protected boolean lock(BusinessActionContext context) {
        Easy4jContext context1 = Easy4j.getContext();
        DbLock dbLock = context1.get(DbLock.class);

        String xid = context.getXid();
        long branchId = context.getBranchId();
        String actionName = context.getActionName();
        String format = String.format("xid:%s-branchId:%s-action:%s", xid, branchId, actionName);
        try {
            logger.info("lock--->" + format);
            dbLock.lock(format, 30, "tcc-lock");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Desc("释放幂等锁")
    protected void unLock(BusinessActionContext context) {
        Easy4jContext context1 = Easy4j.getContext();
        DbLock dbLock = context1.get(DbLock.class);

        String xid = context.getXid();
        long branchId = context.getBranchId();
        String actionName = context.getActionName();
        String format = String.format("xid:%s-branchId:%s-action:%s", xid, branchId, actionName);
        logger.info("unLock--->" + format);
        dbLock.unLock(format);
    }

    protected void putContext(BusinessActionContext context, String name, Object object) {
        Map<String, Object> actionContext = context.getActionContext();
        if (null == actionContext) {
            Map<String, Object> contextMap = Maps.newHashMap();
            context.setActionContext(contextMap);
        }
        if (ObjectUtil.isNotEmpty(object)) {
            Map<String, Object> actionContext1 = context.getActionContext();
            actionContext1.put(name, object);
        }
    }


}
