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
 * 处理连接加密码
 */
public class NacosUrlResolve extends ObjectStringAbstractResolve {

    @Override
    public Object handler(Object properties, String p) {
        String url1 = getUrl(p);
        if (StrUtil.isBlank(url1)) {
            throw new RuntimeException("nacos url format is error !" + url1);
        }
        setSpringProperty(properties, SysConstant.EASY4J_SCA_NACOS_URL, url1);
        setSpringProperty(properties, SysConstant.EASY4J_SCA_NACOS_USERNAME, getUsername(p));
        setSpringProperty(properties, SysConstant.EASY4J_SCA_NACOS_PASSWORD, getPassword(p));
        return properties;
    }
}
