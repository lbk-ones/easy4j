package easy4j.infra.xxljob;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import easy4j.infra.base.resolve.StandAbstractEasy4jResolve;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.module.ModuleBoolean;
import easy4j.infra.common.module.ModuleNotBlank;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xxl-job 配置
 *
 * @author bokun.li
 * @date 2025/7/1
 */
@ModuleBoolean(SysConstant.EASY4J_XXLJOB_ENABLE)
@ModuleNotBlank(value = {SysConstant.EASY4J_XXLJOB_ADMIN_URL, SysConstant.EASY4J_SERVER_NAME})
@Configuration(proxyBeanMethods = false)
public class Config extends StandAbstractEasy4jResolve {
    private final Logger logger = LoggerFactory.getLogger(Config.class);


    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {

        logger.info(SysLog.compact(">>>>>>>>>>> xxl-job config init."));
        String logPath = getLogPath() + SP.SLASH + "job";

        String adminAddresses = Easy4j.getProperty(SysConstant.EASY4J_XXLJOB_ADMIN_URL);
        String serverName = Easy4j.getProperty(SysConstant.EASY4J_SERVER_NAME);
        String accessToken = Easy4j.getProperty(SysConstant.EASY4J_XXLJOB_ACCESS_TOKEN);
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        /**
         调度中心部署根地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。执行器将会使用该地址进行"执行器心跳注册"和"任务结果回调"；为空则关闭自动注册；
         xxl.job.admin.addresses=http://127.0.0.1:8080/xxl-job-admin
         */
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        // 执行器AppName [选填]：执行器心跳注册分组依据；为空则关闭自动注册
        xxlJobSpringExecutor.setAppname(serverName);
        //xxlJobSpringExecutor.setAddress("");
        // ip 可以不设置
        // xxlJobSpringExecutor.setIp(ip);
        // 端口不设置则自动选择
        // xxlJobSpringExecutor.setPort(port);
        // 调度中心通讯TOKEN [选填]：非空时启用；
        xxlJobSpringExecutor.setAccessToken(accessToken);
        // 调度中心通讯超时时间[选填]，单位秒；默认3s；
        xxlJobSpringExecutor.setTimeout(3);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(7);

        return xxlJobSpringExecutor;
    }


}
