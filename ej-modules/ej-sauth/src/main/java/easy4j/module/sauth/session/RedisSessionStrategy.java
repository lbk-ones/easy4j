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
package easy4j.module.sauth.session;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.module.sauth.domain.SecuritySession;
import javax.annotation.Resource;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
/**
 * RedisSessionStrategy
 *
 * @author bokun.li
 * @date 2025-05
 */
public class RedisSessionStrategy extends AbstractSessionStrategy {

    @Resource(name = SysConstant.REDIS_CACHE_MANAGER)
    CacheManager cacheManager;

    @Override
    public SecuritySession getSession(String token) {
        Cache cache = cacheManager.getCache(SysConstant.PARAM_PREFIX);
        assert cache != null;
        Cache.ValueWrapper valueWrapper = cache.get(token);
        if (valueWrapper == null) {
            return null;
        }
        Object o = valueWrapper.get();
        if (o == null) return null;
        return JacksonUtil.toObject(JacksonUtil.toJson(o), SecuritySession.class);
    }

    @Override
    public SecuritySession saveSession(SecuritySession securitySession) {
        if (null == securitySession) {
            Easy4j.error(SysLog.compact("securitySession is null"));
            return null;
        }
        String shaToken = securitySession.getShaToken();
        Cache cache = cacheManager.getCache(SysConstant.PARAM_PREFIX);
        assert cache != null;
        cache.put(shaToken, securitySession);
        return securitySession;
    }

    @Override
    public void deleteSession(String token) {
        Cache cache = cacheManager.getCache(SysConstant.PARAM_PREFIX);
        assert cache != null;
        cache.evict(token);
    }


}
