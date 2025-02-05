package easy4j.module.base.starter;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.CommandLineRunner;

/**
 * 启动结束
 * @author bokun.li
 * @date 2023/10/30
 */
public class ApplicationStarterAfterForEj implements InitializingBean, CommandLineRunner, DisposableBean {


    private final Logger logger = LoggerFactory.getLogger(EasyStarterImport.class);


    @Override
    public void run(String... args) throws Exception {
        SysLog.settingLog();

        try{

            Class<?> aClass = this.getClass().getClassLoader().loadClass("com.alibaba.druid.pool.DruidDataSource");
            String port = EnvironmentHolder.getProperty(SysConstant.SERVER_PORT_STR);
            String userName = EnvironmentHolder.getProperty(SysConstant.DRUID_USER_NAME);
            String pwd = EnvironmentHolder.getProperty(SysConstant.DRUID_USER_PWD);
            logger.info(SysLog.compact("DRUID 监控地址 http://127.0.0.1:"+port+"/druid/login.html 用户名:"+userName+" 密码"+pwd));

            String dbUrl = EnvironmentHolder.getProperty(SysConstant.DB_URL_STR);
            String h2Enabled = EnvironmentHolder.getProperty(SysConstant.SPRING_H2_CONSOLE_ENABLED);
            String h2Path = EnvironmentHolder.getProperty(SysConstant.SPRING_H2_CONSOLE_PATH);
            String h2u = EnvironmentHolder.getProperty(SysConstant.DB_USER_NAME);
            String h2p = EnvironmentHolder.getProperty(SysConstant.DB_USER_PASSWORD);
            if(StrUtil.equals(h2Enabled,"true")){
                logger.info(SysLog.compact("h2 数据库管理地址 http://127.0.0.1:"+port+h2Path+"用户名:"+h2u+";密码:"+h2p+";数据库地址:"+dbUrl));
            }

        }catch (Exception ignored){

        }


    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void destroy() throws Exception {
        logger.info(SysLog.compact("系统正在关闭"));
    }
}
