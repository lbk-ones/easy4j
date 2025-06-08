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
package easy4j.infra.dbaccess.proxy;

//import com.zaxxer.hikari.HikariDataSource;
//import com.zaxxer.hikari.HikariPoolMXBean;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JdbcDbAccess
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public class JdkProxyHandler implements InvocationHandler {
    private final Object target; // 目标对象

    private final DataSource dataSource;

    public JdkProxyHandler(Object target, DataSource dataSource) {
        this.target = target;
        this.dataSource = dataSource;
    }

    // 创建代理对象
    public Object createProxy() {
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                this
        );
    }

    // 方法拦截逻辑
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        printDataSoureInfo(method, "before");
        // 调用目标方法
        try {
            // 调用目标方法
            Object invoke = method.invoke(target, args);
            printDataSoureInfo(method, "after");
            return invoke;
        } catch (InvocationTargetException e) {
            throw unwrapReflectionException(e); // 抛出原始异常
        } catch (Exception e) {
            // 处理其他异常（如代理框架本身的异常）
            System.out.println("代理框架异常: " + e.getMessage());
            throw e;
        }
    }

    private Throwable unwrapReflectionException(Throwable throwable) {
        if (throwable instanceof InvocationTargetException) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                return unwrapReflectionException(cause);
            }
        }
        return throwable;
    }

    void printDataSoureInfo(Method method, String type) {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource dataSource1 = (HikariDataSource) dataSource;
            HikariPoolMXBean hikariPoolMXBean = dataSource1.getHikariPoolMXBean();
            int idleConnections = hikariPoolMXBean.getIdleConnections();
            //log.info(method.getName() + "----idleConnections---->" + type + "---" + idleConnections);
        }
    }
}