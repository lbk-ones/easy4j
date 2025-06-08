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
package easy4j.infra.base.resolve;


import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SysConstant;

/**
 * 处理SpringBootAdmin 的配置
 * 本地启动加载配置
 * 远程配置获取启动
 */
public class BootAdminPropertiesResolve extends ObjectStringAbstractResolve {

    public static BootAdminPropertiesResolve get() {
        return new BootAdminPropertiesResolve();
    }

    @Override
    public Object handler(Object t, String url) {
        if (StrUtil.isNotBlank(url)) {
            String username = getUsername(url);
            String url1 = getUrl(url);
            String password = getPassword(url);
            setSpringProperty(t, SysConstant.SPRING_BOOT_ADMIN_ENABLE, "true");
            setSpringProperty(t, SysConstant.SPRING_BOOT_ADMIN_URL, url1);
            setSpringProperty(t, SysConstant.SPRING_BOOT_ADMIN_USERNAME, username);
            setSpringProperty(t, SysConstant.SPRING_BOOT_ADMIN_PASSWORD, password);
            setSpringProperty(t, SysConstant.SPRING_BOOT_ADMIN_HOST_TYPE, "ip");
            setSpringProperty(t, SysConstant.SPRING_BOOT_ADMIN_CONNECTION_TIMEOUT, "5000");
            setSpringProperty(t, SysConstant.SPRING_BOOT_ADMIN_READ_TIMEOUT, "5000");
        }

        return t;

    }


}
