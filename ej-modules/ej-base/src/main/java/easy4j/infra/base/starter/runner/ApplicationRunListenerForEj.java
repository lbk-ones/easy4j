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
package easy4j.infra.base.starter.runner;

//import easy4j.infra.common.utils.ServiceLoaderUtils;

import easy4j.infra.common.utils.ServiceLoaderUtils;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.Duration;
import java.util.List;

/**
 * 启动流程监听
 *
 * @author bokun.li
 * @date 2023/10/30
 */
public class ApplicationRunListenerForEj implements SpringApplicationRunListener, Ordered {
    List<ApplicationListenerForEj> applicationListenerForEj = ServiceLoaderUtils.load(ApplicationListenerForEj.class);

    public ApplicationRunListenerForEj(SpringApplication application, String[] args) {
        //System.setProperty("log4j.skipJansi", "false");
        for (ApplicationListenerForEj applicationListener : applicationListenerForEj) {
            applicationListener.construct(application, args);
        }
    }

    /**
     * 开始启动
     *
     * @param bootstrapContext the bootstrap context
     */
    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        for (ApplicationListenerForEj applicationListener : applicationListenerForEj) {
            applicationListener.starting();
        }
    }

    /**
     * 环境准备结束回调 在这里注入环境
     *
     * @param bootstrapContext the bootstrap context
     * @param environment      the environment
     */
    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        for (ApplicationListenerForEj applicationListener : applicationListenerForEj) {
            applicationListener.environmentPrepared(environment);
        }
    }

    /**
     * 开始准备上下文容器
     *
     * @param context the application context
     */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        for (ApplicationListenerForEj applicationListener : applicationListenerForEj) {
            applicationListener.contextPrepared(context);
        }
    }

    /**
     * 上下文容器准备结束
     *
     * @param context the application context
     */
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        for (ApplicationListenerForEj applicationListener : applicationListenerForEj) {
            applicationListener.contextPrepared(context);
        }
    }

    /**
     * 应用启动结束前的回调
     *
     * @param context the application context
     */
    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        for (ApplicationListenerForEj applicationListener : applicationListenerForEj) {
            applicationListener.started(context, timeTaken);
        }
    }

    /**
     * 应用启动结束
     *
     * @param context the application context
     */
    @Override
    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {

        for (ApplicationListenerForEj applicationListener : applicationListenerForEj) {
            applicationListener.ready(context, timeTaken);
        }
    }

    /**
     * 启动失败
     *
     * @param context   the application context or {@code null} if a failure occurred before
     *                  the context was created
     * @param exception the failure
     */
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        for (ApplicationListenerForEj applicationListener : applicationListenerForEj) {
            applicationListener.failed(context, exception);
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
