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
