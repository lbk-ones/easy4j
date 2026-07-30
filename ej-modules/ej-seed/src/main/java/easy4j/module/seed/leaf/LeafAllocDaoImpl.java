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
package easy4j.module.seed.leaf;


import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.OrmInternal;
import easy4j.infra.dbaccess.SqlFileEnums;
import easy4j.infra.dbaccess.orm.IDBAccess;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * LeafAllocDaoImpl
 *
 * @author bokun.li
 * @date 2025-05
 */
@Service
public class LeafAllocDaoImpl implements LeafAllocDao, InitializingBean {

    private IDBAccess dbaccess;

    @Override
    public void afterPropertiesSet() throws Exception {
        OrmInternal.INIT_DB_FILE_PATH.add(SqlFileEnums.DB_LEAF);
        OrmInternal.INIT_DB_FILE_PATH.add(SqlFileEnums.DB_SNOW_IP);
        dbaccess = OrmInternal.getNoTransactionOrm();
    }

    private LeafAllocDomain getByBizTag(String bizTag) {
        LeafAllocDomain leafAllocDomain = new LeafAllocDomain();
        leafAllocDomain.setBizTag(bizTag);
        return dbaccess.queryById(leafAllocDomain, LeafAllocDomain.class);
    }


    public List<String> getAllTags() {
        return ListTs.tListToListString(dbaccess.queryAll(LeafAllocDomain.class), LeafAllocDomain::getBizTag);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LeafAllocDomain updateMaxIdAndGetLeafAlloc(String bizTag) {
        LeafAllocDomain domain = getByBizTag(bizTag);
        domain.setMaxId(domain.getMaxId() + domain.getStep());
        dbaccess.updateById(domain,true, LeafAllocDomain.class);
        return dbaccess.queryById(domain, LeafAllocDomain.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LeafAllocDomain updateMaxIdByCustomStepAndGetLeafAlloc(LeafAllocDomain domain) {

        LeafAllocDomain leafAllocDomain = new LeafAllocDomain();
        leafAllocDomain.setBizTag(domain.getBizTag());
        long maxId = domain.getStep() + domain.getMaxId();
        leafAllocDomain.setMaxId(maxId);
        leafAllocDomain.setUpdateTime(new Date());
        dbaccess.updateById(domain,true, LeafAllocDomain.class);
        return dbaccess.queryById(domain, LeafAllocDomain.class);
    }


}
