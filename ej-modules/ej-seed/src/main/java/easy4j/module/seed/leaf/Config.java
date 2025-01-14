package easy4j.module.seed.leaf;

import easy4j.module.base.utils.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Config implements InitializingBean {
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
    }
}
