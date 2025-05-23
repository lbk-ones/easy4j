package easy4j.module.h2;


import easy4j.module.base.annotations.Desc;
import easy4j.module.base.starter.AbstractEnvironmentForEj;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.base.starter.Easy4JStarterNd;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.SysConstant;
import org.h2.Driver;
import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Objects;
import java.util.Properties;
@Order(value = 14)
public class H2Environment extends AbstractEnvironmentForEj {

    public static final String H2_SERVER_NAME = "EASY4j_H2_ENV_NAME";
    @Override
    public String getName() {
        return H2_SERVER_NAME;
    }

    @Desc("jdbc:h2:file:/path/to/your/h2db;DB_CLOSE_ON_EXIT=false 写入文件")
    @Override
    public Properties getProperties() {
        String dbType = getDbType();
        if("other".equals(dbType)){
            Properties properties = new Properties();
            Class<?> mainClass = Easy4j.mainClass;
            boolean enabledH2 = false;
            String h2Url = "";
            if(Objects.nonNull(mainClass)){
                Easy4JStarter annotation = mainClass.getAnnotation(Easy4JStarter.class);
                if(Objects.nonNull(annotation)){
                    enabledH2 = annotation.enableH2();
                    h2Url = annotation.h2Url();
                }else{
                    Easy4JStarterNd annotation2 = mainClass.getAnnotation(Easy4JStarterNd.class);
                    if(Objects.nonNull(annotation2)){
                        enabledH2 = annotation2.enableH2();
                        h2Url = annotation2.h2Url();
                    }
                }

            }
            // jdbc:h2:file:/path/to/your/h2db;DB_CLOSE_ON_EXIT=false
            // jdbc:h2:mem:testdb
            if(enabledH2){
                properties.setProperty(SysConstant.DB_URL_STR,h2Url);
                String name = Driver.class.getName();
                properties.setProperty(SysConstant.DB_URL_DRIVER_CLASS_NAME,name);
                properties.setProperty(SysConstant.DB_USER_NAME,"sa");
                properties.setProperty(SysConstant.DB_USER_PASSWORD,"password");
                properties.setProperty(SysConstant.SPRING_H2_CONSOLE_ENABLED,"true");
                properties.setProperty(SysConstant.SPRING_H2_CONSOLE_PATH,"/h2-console");
                return properties;
            }
        }
        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
