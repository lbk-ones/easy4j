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
package easy4j.module.base.starter;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.log.DbLog;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.SysLog;
import jodd.util.StringPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.CommandLineRunner;

import java.io.InputStream;

/**
 * 启动结束
 *
 * @author bokun.li
 * @date 2023/10/30
 */
public class ApplicationRuner implements InitializingBean, CommandLineRunner, DisposableBean {


    private final Logger logger = LoggerFactory.getLogger(EasyStarterImport.class);


    @Override
    public void run(String... args) throws Exception {
        SysLog.settingLog();

//        Easy4j.getContext().set(DbLog.class, DbLog.getDbLog());

        try {
            // dont delete the line code
            Class<?> aClass = this.getClass().getClassLoader().loadClass("com.alibaba.druid.pool.DruidDataSource");
            String port = Easy4j.getProperty(SysConstant.SERVER_PORT_STR);
            String userName = Easy4j.getProperty(SysConstant.DRUID_USER_NAME);
            String pwd = Easy4j.getProperty(SysConstant.DRUID_USER_PWD);
            logger.info(SysLog.compact("DRUID 监控地址 http://127.0.0.1:" + port + "/druid/login.html 用户名:" + userName + " 密码" + pwd));

            String dbUrl = Easy4j.getProperty(SysConstant.DB_URL_STR);
            String h2Enabled = Easy4j.getProperty(SysConstant.SPRING_H2_CONSOLE_ENABLED);
            String h2Path = Easy4j.getProperty(SysConstant.SPRING_H2_CONSOLE_PATH);
            String h2u = Easy4j.getProperty(SysConstant.DB_USER_NAME);
            String h2p = Easy4j.getProperty(SysConstant.DB_USER_PASSWORD);
            if (StrUtil.equals(h2Enabled, "true")) {
                logger.info(SysLog.compact("h2 数据库管理地址 http://127.0.0.1:" + port + h2Path + "用户名:" + h2u + ";密码:" + h2p + ";数据库地址:" + dbUrl));
            }

        } catch (Exception ignored) {

        }
        // doc print
        if (StringPool.TRUE.equals(Easy4j.getProperty(SysConstant.KNIFE4J_ENABLE))) {
            logger.info(SysLog.compact("接口文档所在地址 http://127.0.0.1:" + Easy4j.getProperty(SysConstant.SERVER_PORT_STR) + "/doc.html"));

            if (StringPool.TRUE.equals(Easy4j.getProperty(SysConstant.KNIFE4J_BASIC_ENABLE))) {
                logger.info(SysLog.compact("接口文档用户名:" + Easy4j.getProperty(SysConstant.KNIFE4J_BASIC_USERNAME) + ";密码:" + Easy4j.getProperty(SysConstant.KNIFE4J_BASIC_PASSWORD)));
            }
        }

        // println i18n
        try {
            logger.info(SysLog.compact("println i18n:"));
            InputStream resourceAsStream = ApplicationRuner.class.getResourceAsStream("/i18n/sys_zh_CN.properties");
            String s1 = IoUtil.readUtf8(resourceAsStream);
            String[] split = s1.split("\r\n");
            for (String string : split) {
                logger.info(SysLog.compact(string));
            }

        } catch (Exception e) {

        }


    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void destroy() throws Exception {
        logger.info(SysLog.compact("系统正在关闭"));
    }
}
