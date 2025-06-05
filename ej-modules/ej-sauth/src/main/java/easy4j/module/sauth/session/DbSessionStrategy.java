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

import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.sauth.domain.SecuritySession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import javax.sql.DataSource;

/**
 * DbSessionStrategy
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
@ConditionalOnBean(DataSource.class)
public class DbSessionStrategy extends AbstractSessionStrategy implements InitializingBean {

    private static DBAccess dbAccess;

    @Override
    public void afterPropertiesSet() throws Exception {
        dbAccess = DBAccessFactory.getDBAccess(SpringUtil.getBean(DataSource.class), true, true);
    }

    @Override
    public SecuritySession getSession(String token) {

        SecuritySession securitySession = new SecuritySession();
        securitySession.setShaToken(token);
        Dict dict = Dict.create()
                .set(LambdaUtil.getFieldName(SecuritySession::getShaToken), token);
        return dbAccess.selectOneByMap(dict, SecuritySession.class);
    }

    @Override
    public SecuritySession saveSession(SecuritySession securitySession) {
        super.saveSession(securitySession);
        int i = dbAccess.saveOne(securitySession, SecuritySession.class);
        if (i > 0) {
            return securitySession;
        }
        return null;
    }

    @Override
    public void deleteSession(String token) {
        Dict dict = Dict.create().set(LambdaUtil.getFieldName(SecuritySession::getShaToken), token);
        dbAccess.deleteByMap(dict, SecuritySession.class);
    }

    @Override
    public SecuritySession getSessionByUserName(String userName) {
        return super.getSessionByUserName(userName);
    }
}
