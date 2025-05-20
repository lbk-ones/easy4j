package easy4j.module.seed.leaf;


import cn.hutool.extra.spring.SpringUtil;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.utils.ListTs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Service
public class LeafAllocDaoImpl implements LeafAllocDao, InitializingBean {

    public static final String LEAF_PATH = "db/leaf";
    public static final String SNOWIP_PATH = "db/snowip";

    private DBAccess dbaccess;

    public JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DBAccessFactory.INIT_DB_FILE_PATH.add(LEAF_PATH);
        DBAccessFactory.INIT_DB_FILE_PATH.add(SNOWIP_PATH);
        dbaccess = DBAccessFactory.getDBAccess(SpringUtil.getBean(DataSource.class));
    }

    private LeafAllocDomain getByBizTag(String bizTag) {
        LeafAllocDomain leafAllocDomain = new LeafAllocDomain();
        leafAllocDomain.setBIZ_TAG(bizTag);
        try {
            return dbaccess.getObjectByPrimaryKey(leafAllocDomain, LeafAllocDomain.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    public List<String> getAllTags() {
        try {
            return ListTs.mapListStr(dbaccess.getAll(LeafAllocDomain.class), LeafAllocDomain::getBIZ_TAG);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LeafAllocDomain updateMaxIdAndGetLeafAlloc(String bizTag) {
        LeafAllocDomain domain = getByBizTag(bizTag);
        domain.setMAX_ID(domain.getMAX_ID() + domain.getSTEP());
        try {
            return dbaccess.updateByPrimaryKeySelective(domain, LeafAllocDomain.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LeafAllocDomain updateMaxIdByCustomStepAndGetLeafAlloc(LeafAllocDomain domain) {

        LeafAllocDomain leafAllocDomain = new LeafAllocDomain();
        leafAllocDomain.setBIZ_TAG(domain.getBIZ_TAG());
        long maxId = domain.getSTEP() + domain.getMAX_ID();
        leafAllocDomain.setMAX_ID(maxId);
        leafAllocDomain.setUPDATE_TIME(new Date());
        try {
            return dbaccess.updateByPrimaryKeySelective(domain, LeafAllocDomain.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
