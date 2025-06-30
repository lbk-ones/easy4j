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
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.lock.DbLock;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextUtil;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * BaseTccAction
 * Tcc继承父类
 *
 * @author bokun.li
 * @date 2025/6/27
 */
public class BaseTccAction {

    public static final String NONE_XID = "none-xid";


    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Desc("日志记录")
    protected void logTx(BusinessActionContext context, String actionType) {
        if (null == context) return;
        Object actionContext = context.getActionContext(RootContext.KEY_XID);
        if (ObjectUtil.equals(actionContext, NONE_XID)) {
            return;
        }
        String xid = context.getXid();
        long branchId = context.getBranchId();
        String actionName = context.getActionName();
        logger.info(actionType + "--> xid:{} ,branchId:{} ,actionName:{} ", xid, branchId, actionName);
    }

    @Desc("简单的幂等锁")
    protected boolean lock(BusinessActionContext context) {
        if (null == context) return true;
        Object actionContext = context.getActionContext(RootContext.KEY_XID);
        if (ObjectUtil.equals(actionContext, NONE_XID)) {
            return true;
        }
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
        if (null == context) return;
        Object actionContext = context.getActionContext(RootContext.KEY_XID);
        if (ObjectUtil.equals(actionContext, NONE_XID)) {
            return;
        }
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
        if (null == context) return;
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

    /**
     * seata服务降级，如果seata服务不可用或者被降级 那么 可以直接在回调里面调用commit方法
     *
     * @author bokun.li
     * @date 2025-06-28
     */
    @Desc("seata服务降级，如果seata服务不可用或者被降级 那么 可以直接在回调里面调用commit方法")
    public void tccDegrade(NullConsumerCallback consumer) {

        BusinessActionContext context = BusinessActionContextUtil.getContext();
        // 如果不在全局事务里面 则可能是服务故障 或者全局降级
        if ((!SeataUtils.isInGlobalTransaction() || null == context) && consumer != null) {
            Class<? extends BaseTccAction> aClass = this.getClass();
            if (!aClass.isAnnotationPresent(LocalTCC.class)) {
                return;
            }
            logger.info("none enable seata server or degrade invoke!");
            Method[] methods = ReflectUtil.getMethods(aClass);
            String commitMethod = null;
            Map<String, Method> methodMap = Maps.newHashMap();
            for (Method method : methods) {
                String name = method.getName();
                methodMap.put(name, method);
                if (method.isAnnotationPresent(TwoPhaseBusinessAction.class)) {
                    TwoPhaseBusinessAction annotation = method.getAnnotation(TwoPhaseBusinessAction.class);
                    if (null != annotation) {
                        commitMethod = annotation.commitMethod();
                        break;
                    }
                }

            }
            Method method = methodMap.get(commitMethod);
            if (null != method) {
                if (method.isAnnotationPresent(Transactional.class)) {
                    if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionTemplate bean = SpringUtil.getBean(TransactionTemplate.class);
                        bean.execute(status -> {
                            try {
                                consumer.accept();
                            } catch (Throwable e) {
                                status.setRollbackOnly();
                            }
                            return null;
                        });
                    } else {
                        consumer.accept();
                    }
                } else {
                    consumer.accept();
                }
            }
        }
    }

    public interface NullConsumerCallback {

        void accept();

    }

    @Desc("降级，和正常执行的组合方法")
    public <T> T prepareCallback(Supplier<T> prepareCallback, NullConsumerCallback commitCallBack) {
        T t = prepareCallback.get();
        tccDegrade(commitCallBack);
        return t;
    }

    /**
     * 兼容获取 context
     * 不绑定到当前线程去
     *
     * @author bokun.li
     * @date 2025/6/30
     */
    public BusinessActionContext getOrCreateContext(BusinessActionContext context, Function<BusinessActionContext, BusinessActionContext> commitCallBack) {
        if (context == null) {
            BusinessActionContext businessActionContext = new BusinessActionContext();
            putContext(businessActionContext, RootContext.KEY_XID, NONE_XID);
            return commitCallBack.apply(businessActionContext);
        }
        return context;
    }


}
