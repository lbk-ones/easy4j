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
package easy4j.infra.base.properties;


import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.minio.EasyMinio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


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

    }

    @Bean
    public EasyMinio easyMinio(){
        String property = Easy4j.getProperty(SysConstant.EASY4J_MINIO_URL);
        String accessKey = Easy4j.getProperty(SysConstant.EASY4J_MINIO_ACCESS_KEY);
        String secretKey = Easy4j.getProperty(SysConstant.EASY4J_MINIO_SECRET_KEY);
        if(StrUtil.isNotBlank(property) && StrUtil.isNotBlank(accessKey) && StrUtil.isNotBlank(secretKey)){
            return new EasyMinio(property,accessKey,secretKey);
        }
        return null;
    }
}
