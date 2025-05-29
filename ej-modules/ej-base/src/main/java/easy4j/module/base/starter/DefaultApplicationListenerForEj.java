package easy4j.module.base.starter;

import easy4j.module.base.utils.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Duration;

/**
 * DefaultApplicationListenerForEj
 *
 * @author bokun.li
 * @date 2025-05
 */
public class DefaultApplicationListenerForEj implements ApplicationListenerForEj {

    @Override
    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        long seconds = timeTaken.getSeconds();
        logger.info(SysLog.compact("系统启动耗时---{}", String.valueOf(seconds)+"s"));
    }

    @Override
    public void construct(SpringApplication application, String[] args) {
        Class<?> mainApplicationClass = application.getMainApplicationClass();
        Easy4j.mainClass = mainApplicationClass;
        Easy4j.mainClassPath = mainApplicationClass.getPackage().getName();
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.error(SysLog.compact("系统启动失败"));
    }
}