package easy4j.infra.sca.seata;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.dbaccess.SqlFileEnums;
import easy4j.infra.dbaccess.SqlFileSpi;

import java.util.List;

public class SqlFileSpiCollect implements SqlFileSpi {

    @Override
    public List<SqlFileEnums> collect() {
        Boolean property = Easy4j.getProperty(SysConstant.EASY4J_SEATA_ENABLE, Boolean.class);
        if(property!=null && property){
            return ListTs.asList(SqlFileEnums.DB_FENCE);
        }
        return List.of();
    }
}
