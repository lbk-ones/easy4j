package easy4j.module.seed.leaf;


import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.utils.ListTs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
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

    public static final String LEAF_PATH = "db/leaf";
    public static final String SNOWIP_PATH = "db/snowip";

    private DBAccess dbaccess;

    @Autowired
    DataSource dataSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        DBAccessFactory.INIT_DB_FILE_PATH.add(LEAF_PATH);
        DBAccessFactory.INIT_DB_FILE_PATH.add(SNOWIP_PATH);
        dbaccess = DBAccessFactory.getDBAccess(dataSource);
    }

    private LeafAllocDomain getByBizTag(String bizTag) {
        LeafAllocDomain leafAllocDomain = new LeafAllocDomain();
        leafAllocDomain.setBIZ_TAG(bizTag);
        return dbaccess.getObjectByPrimaryKey(leafAllocDomain, LeafAllocDomain.class);

    }


    public List<String> getAllTags() {
        return ListTs.mapListStr(dbaccess.getAll(LeafAllocDomain.class), LeafAllocDomain::getBIZ_TAG);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LeafAllocDomain updateMaxIdAndGetLeafAlloc(String bizTag) {
        LeafAllocDomain domain = getByBizTag(bizTag);
        domain.setMAX_ID(domain.getMAX_ID() + domain.getSTEP());
        return dbaccess.updateByPrimaryKeySelective(domain, LeafAllocDomain.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LeafAllocDomain updateMaxIdByCustomStepAndGetLeafAlloc(LeafAllocDomain domain) {

        LeafAllocDomain leafAllocDomain = new LeafAllocDomain();
        leafAllocDomain.setBIZ_TAG(domain.getBIZ_TAG());
        long maxId = domain.getSTEP() + domain.getMAX_ID();
        leafAllocDomain.setMAX_ID(maxId);
        leafAllocDomain.setUPDATE_TIME(new Date());
        return dbaccess.updateByPrimaryKeySelective(domain, LeafAllocDomain.class);
    }


}