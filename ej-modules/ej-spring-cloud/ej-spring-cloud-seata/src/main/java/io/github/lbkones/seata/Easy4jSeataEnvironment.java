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
package io.github.lbkones.seata;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import easy4j.infra.base.properties.NacosPropetiesParse;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025/6/24
 */
public class Easy4jSeataEnvironment extends AbstractEasy4jEnvironment {
    public static final String SEATA_CONFIG_ENV_NAME = "seata-easy4j.properties";

    @Override
    public String getName() {
        return SEATA_CONFIG_ENV_NAME;
    }

    @Override
    public Properties getProperties() {
        try{
            ClassPathResource resource = new ClassPathResource("seata-git.properties");
            try (InputStream is = resource.getInputStream()) {
                Properties properties1 = new Properties();
                properties1.load(is);

                System.out.println(SysLog.compact("current seata client version is "+properties1.getProperty("git.build.version")));
            }
        } catch (Exception ignored) {
        }
        //String txGroup = getEnvProperty(SysConstant.EASY4J_SEATA_TX_GROUP);
        String clusterName = getEnvProperty(SysConstant.EASY4J_SEATA_NACOS_CLUSTER);
        String serverName = getEnvProperty(SysConstant.EASY4J_SERVER_NAME);
        Properties properties = new Properties();
        properties.setProperty("seata.application-id", serverName);
        properties.setProperty("seata.tcc.fence.log-table-name", "sys_tcc_fence_log");
        properties.setProperty("seata.transport.thread-factory.boss-thread-size", String.valueOf(Runtime.getRuntime().availableProcessors()));
       // properties.setProperty("seata.service.vgroup-mapping." + txServiceGroup, clusterName);
        // linux use epoll modal
        if (SystemUtil.getOsInfo().isLinux()) {
            properties.setProperty("seata.transport.server", "native");
        }
        boolean easyDev = getEnvProperty(SysConstant.EASY4J_DEV, boolean.class);
        if (easyDev) {
            properties.setProperty("seata.log.exception-rate", "100");
        } else {
            properties.setProperty("seata.log.exception-rate", "10");
        }

        if (isSca()) {
            NacosPropetiesParse build = NacosPropetiesParse.build(this.getConfigEnvironment(), false);
            properties.setProperty("seata.registry.type", "nacos");
            properties.setProperty("seata.registry.nacos.cluster", clusterName);
            properties.setProperty("seata.registry.nacos.group", build.getNacosGroup());
            properties.setProperty("seata.registry.nacos.server-addr", build.getNacosUrl());
            properties.setProperty("seata.registry.nacos.username", build.getNacosUsername());
            properties.setProperty("seata.registry.nacos.password", build.getNacosPassword());
            properties.setProperty("seata.registry.nacos.namespace", build.getNacosNamespace());
            properties.setProperty("seata.registry.nacos.application", "seata-server");
        }
        return properties;
    }

    @Override
    public boolean isSkip() {
        return !getEnvProperty(SysConstant.EASY4J_SEATA_ENABLE, boolean.class);
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
//        String normalDbUrl = getNormalDbUrl();
//        String url = getUrl(normalDbUrl);
//        String username = getUsername(normalDbUrl);
//        String password = getPassword(normalDbUrl);
//        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(url);
//        try (HikariDataSource hikariDataSource = new HikariDataSource()) {
//            hikariDataSource.setJdbcUrl(url);
//            hikariDataSource.setUsername(username);
//            hikariDataSource.setPassword(password);
//            hikariDataSource.setDriverClassName(driverClassNameByUrl);
//            DBAccessFactory.INIT_DB_FILE_PATH.add("db/fence");
//            DBAccessFactory.getDBAccess(hikariDataSource);
//        }
    }
}
