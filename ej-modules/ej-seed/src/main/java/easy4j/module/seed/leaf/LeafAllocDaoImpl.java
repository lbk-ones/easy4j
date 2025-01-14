package easy4j.module.seed.leaf;


import cn.hutool.core.collection.CollectionUtil;
import easy4j.module.base.enums.DbType;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SqlType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeafAllocDaoImpl implements LeafAllocDao, InitializingBean {

    public JdbcTemplate jdbcTemplate;
    private boolean isOracle;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DbType dbType = SqlType.getDbType();
        isOracle = ListTs.asList(DbType.ORACLE,DbType.ORACLE_12C).contains(dbType.getDb());
    }

    private LeafAllocDomain getByBizTag(String bizTag){
        String format = String.format("SELECT * FROM LEAF_ALLOC WHERE BIZ_TAG = %s", bizTag);
        return jdbcTemplate.queryForObject(format,LeafAllocDomain.class);
    }


    public List<String> getAllTags() {
        List<LeafAllocDomain> list = jdbcTemplate.queryForList("SELECT * FROM LEAF_ALLOC WHERE 1=1", LeafAllocDomain.class);
        List<String> tags = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            list.forEach(l -> tags.add(l.getBIZ_TAG()));
        }
        return tags;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LeafAllocDomain updateMaxIdAndGetLeafAlloc(String bizTag) {
        LeafAllocDomain domain = getByBizTag(bizTag);
        domain.setMAX_ID(domain.getMAX_ID() + domain.getSTEP());
        if(isOracle){
            jdbcTemplate.update("UPDATE LEAF_ALLOC SET UPDATE_TIME = SYSDATE, MAX_ID = TO_NUMBER(MAX_ID) + TO_NUMBER(STEP) WHERE BIZ_TAG = ?",domain.getBIZ_TAG());
        }else{
            jdbcTemplate.update("UPDATE LEAF_ALLOC SET UPDATE_TIME = SYSDATE, MAX_ID = MAX_ID + STEP WHERE BIZ_TAG = ?",domain.getBIZ_TAG());
        }
        return getByBizTag(bizTag);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LeafAllocDomain updateMaxIdByCustomStepAndGetLeafAlloc(LeafAllocDomain domain) {
        jdbcTemplate.update("UPDATE LEAF_ALLOC SET UPDATE_TIME = ?, MAX_ID =  MAX_ID + CONVERT(?, UNSIGNED) WHERE BIZ_TAG = ?",new Date(System.currentTimeMillis()),domain.getSTEP(),domain.getBIZ_TAG());
        return this.getByBizTag(domain.getBIZ_TAG());
    }


}
