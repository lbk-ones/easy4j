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
package easy4j.infra.gateway;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Config
 * 网关相关配置 整合 sentinel
 *
 * @author bokun.li
 * @date 2025-06-15
 */
@Configuration
public class Config {

    @Autowired
    private RouteLocator routeLocator;

    /**
     * 获取所有routeId
     *
     * @author bokun.li
     * @date 2025-06-15
     */
    public List<String> getAllRouteIds() {
        // 同步获取所有路由
        List<Route> routes = new ArrayList<>();
        routeLocator.getRoutes().subscribe(routes::add);

        // 提取routeId
        return routes.stream()
                .map(Route::getId)
                .collect(Collectors.toList());
    }


    /**
     * 检查指定routeId是否存在
     *
     * @author bokun.li
     * @date 2025-06-15
     */
    public boolean existsRouteId(String routeId) {
        return getAllRouteIds().contains(routeId);
    }


    /**
     * sentinel 全局过滤器
     *
     * @author bokun.li
     * @date 2025-06-15
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * 限流规则
     *
     * @author bokun.li
     * @date 2025-06-15
     */
    @PostConstruct
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        List<String> allRouteIds = this.getAllRouteIds();

        int qps = Easy4j.getProperty(Easy4j.getEjSysPropertyName(EjSysProperties::getScaGatewayFlowQps), int.class);
        // 为每个路由 ID 配置流控规则
        for (String routeId : allRouteIds) {
            Easy4j.info(SysLog.compact("流控资源******" + routeId + "******qps:" + qps));
            GatewayFlowRule rule = new GatewayFlowRule();
            rule.setResource(routeId);  // 使用路由 ID 作为资源名
            rule.setCount(qps);         // 限流阈值
            rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            rules.add(rule);
        }
        if (CollUtil.isNotEmpty(rules)) {
            GatewayRuleManager.loadRules(rules);
        }
    }


    /**
     * 限流规则
     *
     * @author bokun.li
     * @date 2025-06-15
     */
    @PostConstruct
    public void initBlockHandlers() {
        CustomBlockRequestHandler customBlockRequestHandler = new CustomBlockRequestHandler();
        GatewayCallbackManager.setBlockHandler(customBlockRequestHandler);
    }

    /**
     * 网关跨域
     *
     * @author bokun.li
     * @date 2025-06-15
     */
    @Bean
    public CorsWebFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOriginPattern("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    @Bean
    public GlobalWebFluxExceptionHandler globalWebFluxExceptionHandler(){
        return new GlobalWebFluxExceptionHandler();
    }
}
