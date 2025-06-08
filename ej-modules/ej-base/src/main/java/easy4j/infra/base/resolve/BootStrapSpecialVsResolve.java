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

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SysConstant;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 引导阶段特殊参数对照
 */
public class BootStrapSpecialVsResolve extends MapStringObjectAbstractResolve {
    @Override
    public Map<String, Object> handler(Map<String, Object> mapProperties, String p) {
        Set<String> setCopy = new HashSet<>(mapProperties.keySet());
        RedisPropertiesResolve redisPropertiesResolve = new RedisPropertiesResolve();
        redisPropertiesResolve.handler(mapProperties, mapProperties);
        // transform
        for (String key : setCopy) {
            Object o = mapProperties.get(key);
            switch (key) {
                case SysConstant.EASY4J_SERVER_PORT:
                    try {
                        Integer port = Integer.parseInt(o.toString());
                        mapProperties.put(SysConstant.SPRING_SERVER_PORT, port);
                    } catch (Exception e) {
                        throw new InvalidParameterException("invalid port:" + o);
                    }
                    break;
                case SysConstant.EASY4J_BOOT_ADMIN_SERVER_URL:
                    String string = StrUtil.toStringOrNull(o);
                    BootAdminPropertiesResolve.get().handler(mapProperties, string);
                    break;
                case SysConstant.EASY4J_SERVER_NAME:
                    mapProperties.put(SysConstant.SPRING_SERVER_NAME, Convert.toStr(o));
                    break;
//                case SysConstant.EASY4J_SCA_NACOS_URL:
//                    NacosUrlResolve nacosUrlResolve = new NacosUrlResolve();
//                    nacosUrlResolve.handlerMap(mapProperties, Convert.toStr(o));
//                    break;
                case SysConstant.DB_URL_STR_NEW:
                    DataSourceUrlResolve dataSourceUrlResolve = new DataSourceUrlResolve();
                    dataSourceUrlResolve.handler(mapProperties, Convert.toStr(o));
                    break;
                default:
                    break;
            }
        }
        return mapProperties;
    }
}
