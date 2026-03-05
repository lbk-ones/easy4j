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

import org.apache.seata.core.context.RootContext;
import org.apache.seata.core.exception.TransactionException;
import org.apache.seata.core.model.GlobalStatus;
import org.apache.seata.tm.api.GlobalTransactionContext;

/**
 * SeataUtils
 *
 * @author bokun.li
 * @date 2025/6/24
 */
public class SeataUtils {

    /**
     * 获取当前全局事务ID
     */
    public static String getCurrentXid() {
        return RootContext.getXID();
    }

    /**
     * 判断是否存在全局事务
     */
    public static boolean isInGlobalTransaction() {
        return RootContext.getXID() != null;
    }

    /**
     * 手动开启全局事务
     */
    public static void beginGlobalTransaction(String name, int timeout) throws Exception {
        GlobalTransactionContext.reload(RootContext.getXID()).begin(timeout, name);
    }

    /**
     * 手动提交全局事务
     */
    public static void commitGlobalTransaction() throws Exception {
        if (isInGlobalTransaction()) {
            GlobalTransactionContext.reload(getCurrentXid()).commit();
        }
    }

    /**
     * 手动回滚全局事务
     */
    public static void rollbackGlobalTransaction() throws Exception {
        if (isInGlobalTransaction()) {
            GlobalTransactionContext.reload(getCurrentXid()).rollback();
        }
    }

    public static GlobalStatus getStatus() {
        try {
            return GlobalTransactionContext.getCurrent().getStatus();
        } catch (TransactionException e) {
            throw new RuntimeException(e);
        }
    }
}