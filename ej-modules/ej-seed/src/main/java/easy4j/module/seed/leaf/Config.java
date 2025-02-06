package easy4j.module.seed.leaf;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.starter.EnvironmentHolder;
import easy4j.module.base.utils.SqlFileExecute;
import easy4j.module.base.utils.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;



@Configuration
public class Config implements InitializingBean {
    public JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public Logger logger = LoggerFactory.getLogger(Config.class);

    @Bean
    public LeafGenIdService leafGenIdService(){
        return new SegmentLeafGenIdServiceImpl();
    }

    @Bean
    public LeafAllocDao leafAllocDao(){
        return new LeafAllocDaoImpl();
    }

    @Bean
    public StarterRunner starterRunner(){
        return new StarterRunner();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info(SysLog.compact("LEAF 分布式主键开始初始化"));
//        EnvironmentHolder.dbType
        String dbType = EnvironmentHolder.getDbType().toLowerCase();

        String classPathSqlName = "";
        if(StrUtil.equals(dbType,"h2")){
            classPathSqlName = "h2.sql";
        }else if(StrUtil.contains(dbType,"sqlserver")){
            classPathSqlName = "sqlserver.sql";
        }else if(StrUtil.equals(dbType,"postgresql")){
            classPathSqlName = "postgresql.sql";
        }else if(StrUtil.equals(dbType,"mysql")){
            classPathSqlName = "mysql.sql";
        }else if(StrUtil.equals(dbType,"oracle")){
            classPathSqlName = "oracle.sql";
        }
        if(StrUtil.isNotBlank(classPathSqlName)){
            try{
                SqlFileExecute.executeSqlFile(jdbcTemplate,classPathSqlName);
            }catch (Exception ignored){

            }
        }
    }


}
