package easy4j.module.sentinel;

import com.alibaba.csp.sentinel.init.InitExecutor;
import easy4j.module.sentinel.annotation.FlowDegradeAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(SentinelProperties.class)
public class SentinelAutoConfiguration {

    private final SentinelProperties sentinelProperties;

    public SentinelAutoConfiguration(SentinelProperties sentinelProperties) {
        this.sentinelProperties = sentinelProperties;
    }

    // 初始化 Sentinel
    @Bean
    @ConditionalOnMissingBean
    public void initSentinel() {
        // 初始化 Sentinel 核心
        InitExecutor.doInit();

        // 配置 Dashboard 地址（如果配置了）
//        if (sentinelProperties.getDashboardServer() != null) {
//            TransportConfig.setRuntimePort(sentinelProperties.getDashboardServer());
//        }
//        FlowRuleManager.loadRules();

        // 设置应用名称
//        System.setProperty("project.name", sentinelProperties.getAppName());
//
//        // 如果配置了 eager=true，立即连接 Dashboard
//        if (sentinelProperties.isEager()) {
//            // 这里可以添加连接 Dashboard 的逻辑（可选）
//        }
    }



    // 支持 @FlowDegrade 注解的切面
    @Bean
    @ConditionalOnMissingBean
    public FlowDegradeAspect sentinelResourceAspect() {
        return new FlowDegradeAspect();
    }
}