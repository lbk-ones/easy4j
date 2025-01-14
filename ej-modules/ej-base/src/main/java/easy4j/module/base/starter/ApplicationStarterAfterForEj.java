package easy4j.module.base.starter;

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
