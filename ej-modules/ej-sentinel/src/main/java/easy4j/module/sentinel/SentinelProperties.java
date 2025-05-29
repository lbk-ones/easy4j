package easy4j.module.sentinel;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SentinelProperties
 *
 * @author bokun.li
 * @date 2025-05
 */
@ConfigurationProperties(prefix = "sentinel")
public class SentinelProperties {
    private String appName = "default-app";
    private String dashboardServer = "localhost:8080";
    private boolean eager = false; // 是否立即连接 Dashboard

    // Getters and Setters
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public String getDashboardServer() { return dashboardServer; }
    public void setDashboardServer(String dashboardServer) { this.dashboardServer = dashboardServer; }
    public boolean isEager() { return eager; }
    public void setEager(boolean eager) { this.eager = eager; }
}