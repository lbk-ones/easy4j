package easy4j.module.base.starter;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.enums.DbType;
import easy4j.module.base.utils.SP;
import easy4j.module.base.utils.SqlType;
import easy4j.module.base.utils.SysConstant;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
/**
 * 环境持有者
 * @author bokun.li
 * @date 2023/10/30
 */
public class EnvironmentHolder implements ApplicationContextAware {

    public static ApplicationContext applicationContext;


    public static Environment environment;

    public static SpringApplication springApplication;

    public static DbType dbType;


    public static String mainClassPath = "";
    public static Class<?> mainClass;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EnvironmentHolder.applicationContext = applicationContext;
    }

    public static String getProperty(String name){
        return environment.getProperty(name);
    }



    private static String getDbUrlByEnvironment(Environment en){
        Environment en2= en;
        if(en == null){
            en2 = environment;
        }
        String property = en2.getProperty(SysConstant.DB_URL_STR);

        if(StrUtil.isBlank(property)){
            String url2 = en2.getProperty(SysConstant.DB_URL_STR_NEW);
            if(StrUtil.isNotBlank(url2)){
                String[] split = url2.split(SP.AT);
                property = split[0];
            }
        }
        return property;

    }



    private static String getDbTypeByEnvironment(Environment en){
        String property = getDbUrlByEnvironment(en);
        String dataTypeByUrl = SqlType.getDataTypeByUrl(property);
        DbType dbType = DbType.getDbType(dataTypeByUrl);
        return dbType.getDb();
    }


    public static String getDbUrl(){
        return getDbUrlByEnvironment(null);
    }
    public static String getDbUrl(Environment en){
        return getDbUrlByEnvironment(en);
    }

    public static String getDbType(){
        return getDbTypeByEnvironment(null);
    }
    public static String getDbType(Environment en){
        return getDbTypeByEnvironment(en);
    }


}
