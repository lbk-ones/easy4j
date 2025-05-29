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
package easy4j.module.logback;

import ch.qos.logback.classic.PatternLayout;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.starter.AbstractEnvironmentForEj;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.SysConstant;
import jodd.util.SystemUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * 这种配置方式太简单了 并不能满足一些奇奇怪怪的配置 但是好在方便
 * # 日志打印级别
 * logging.level.root=INFO
 * logging.level.com.example=DEBUG
 * <p>
 * # 日志打印格式
 * logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
 * logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
 * <p>
 * # 日志保存文件位置
 * logging.file.name=logs/app.log
 * <p>
 * # 日志滚动策略
 * logging.logback.rollingpolicy.max-file-size=200MB
 * logging.logback.rollingpolicy.max-history=2
 * logging.logback.rollingpolicy.file-name-pattern=logs/app.%d{yyyy-MM-dd}.%i.log
 */
//@Order(value = Integer.MIN_VALUE)
public class LogbackEnvironment extends AbstractEnvironmentForEj {

    public static final String LOG_BACK_NAME = "ej-log-back-environment";

    @Override
    public String getName() {
        return LOG_BACK_NAME;
    }

    /**
     * 覆盖日志存储位置 不使用 xml的方式去搞 简单点
     *
     * @return
     */
    @Override
    public Properties getProperties() {
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        boolean simpleLinkTracking = ejSysProperties.isSimpleLinkTracking();
        String traceIdTemplate = "%" + SysConstant.TRACE_ID_NAME;
        if (simpleLinkTracking) {
            traceIdTemplate = "%X{" + SysConstant.TRACE_ID_NAME + "}";
        }
        Properties properties = new Properties();
        properties.setProperty("logging.level.root", "INFO");
        String property = getProperty(SysConstant.SPRING_SERVER_NAME);
        properties.setProperty("logging.pattern.console", "%d{yyyy-MM-dd HH:mm:ss.SSS} %clr(%-5level) %clr(${PID:- }){magenta} [" + property + "] [%thread] %clr(%-37.37logger{36}){cyan} " + traceIdTemplate + "- %msg%n");
        properties.setProperty("logging.pattern.file", "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level ${PID:- } [" + property + "] [%thread] %-37.37logger{36} " + traceIdTemplate + "- %msg%n");
        String hostName = SystemUtil.info().getHostName();
        String path = "";
        if (cn.hutool.system.SystemUtil.getOsInfo().isWindows()) {
            path = "logs/";
        } else {
            path = "/app/logs/";
        }
        properties.setProperty("logging.file.name", path + hostName + ".log");
        properties.setProperty("logging.logback.rollingpolicy.max-file-size", "200MB");
        properties.setProperty("logging.logback.rollingpolicy.file-name-pattern", path + hostName + ".%d{yyyy-MM-dd}.%i.log");

        return properties;
    }

    private void defaultTraceId() {

        try {
            this.getClass().getClassLoader().loadClass("easy4j.module.jaeger.spring.logback.JaegerTraceIdConvert");
        } catch (ClassNotFoundException e) {
            PatternLayout.DEFAULT_CONVERTER_MAP.put("traceId", TranceIdConverter.class.getName());
        }

    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        defaultTraceId();
    }
}
