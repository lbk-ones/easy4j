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
package easy4j.module.base.resolve;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.utils.SysConstant;

import java.util.Map;
import java.util.Properties;

/**
 * 处理连接加密码
 */
public class DataSourceUrlResolve extends PropertiesResolve {

    @Override
    public Properties handler(Properties properties, String p) {
        properties.setProperty(SysConstant.DB_URL_STR, getUrl(p));
        String username = getUsername(p);
        if (StrUtil.isNotBlank(username)) {
            properties.setProperty(SysConstant.DB_USER_NAME, username);
        }
        String password = getPassword(p);
        if (StrUtil.isNotBlank(password)) {
            properties.setProperty(SysConstant.DB_USER_PASSWORD, password);
        }
        return properties;
    }

    public void handlerMap(Map<String, Object> properties, String p) {
        properties.put(SysConstant.DB_URL_STR, getUrl(p));
        String username = getUsername(p);
        if (StrUtil.isNotBlank(username)) {
            properties.put(SysConstant.DB_USER_NAME, username);
        }
        String password = getPassword(p);
        if (StrUtil.isNotBlank(password)) {
            properties.put(SysConstant.DB_USER_PASSWORD, password);
        }
    }


}
