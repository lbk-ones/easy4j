package easy4j.module.base.properties;


import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static easy4j.module.base.log.DefLog.*;


/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
@EnableConfigurationProperties({EjSysProperties.class})
@Slf4j
@AutoConfigureBefore({SpringApplicationAdminJmxAutoConfiguration.class})
public class Config implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollUtil.isNotEmpty(infoLine) && log.isInfoEnabled()) {
            for (String s : infoLine) {
                log.info(s);
            }
            infoLine.clear();
        }
        if (CollUtil.isNotEmpty(warnLine) && log.isWarnEnabled()) {
            for (String s : warnLine) {
                log.warn(s);
            }
            warnLine.clear();
        }
        if (CollUtil.isNotEmpty(errorLine) && log.isErrorEnabled()) {
            for (String s : errorLine) {
                log.error(s);
            }
            errorLine.clear();
        }

        if (CollUtil.isNotEmpty(debugLine) && log.isDebugEnabled()) {
            for (String s : debugLine) {
                log.debug(s);
            }
            debugLine.clear();
        }

        if (CollUtil.isNotEmpty(traceLine) && log.isTraceEnabled()) {
            for (String s : traceLine) {
                log.trace(s);
            }
            traceLine.clear();
        }
    }
}