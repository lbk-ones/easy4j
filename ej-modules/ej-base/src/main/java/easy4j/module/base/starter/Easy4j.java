package easy4j.module.base.starter;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.enums.DbType;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.utils.SP;
import easy4j.module.base.utils.SqlType;
import easy4j.module.base.utils.SysConstant;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.Properties;

/**
 * 环境持有者
 * @author bokun.li
 * @date 2023/10/30
 */
public class Easy4j implements ApplicationContextAware {

    public static ApplicationContext applicationContext;


    public static Environment environment;

    public static SpringApplication springApplication;

    public static DbType dbType;


    public static String mainClassPath = "";
    public static Class<?> mainClass;


    @Getter
    private static volatile Properties extProperties;

    @Getter
    private static final EjSysProperties defaultEjSysProperties = new EjSysProperties();



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Easy4j.applicationContext = applicationContext;
    }

    public static String getProperty(String name){
        String property = environment.getProperty(name);
        if(StrUtil.isBlank(property)){
            initExtProperties();
            return extProperties.getProperty(name);
        }
        return property;
    }



    private static String getDbUrlByEnvironment(){

        String property = getProperty(SysConstant.DB_URL_STR);
        if(StrUtil.isBlank(property)){
            String url2 = getProperty(SysConstant.DB_URL_STR_NEW);
            if(StrUtil.isNotBlank(url2)){
                String[] split = url2.split(SP.AT);
                property = split[0];
            }
        }
        return property;

    }



    private static String getDbTypeByEnvironment(){
        String property = getDbUrlByEnvironment();
        String dataTypeByUrl = SqlType.getDataTypeByUrl(property);
        DbType dbType = DbType.getDbType(dataTypeByUrl);
        return dbType.getDb();
    }


    public static String getDbUrl(){
        return getDbUrlByEnvironment();
    }
    public static String getDbType(){
        return getDbTypeByEnvironment();
    }

    // load ext properties
    // 1、load bootstrap class annotation metadata
    public static void initExtProperties(){
        if (extProperties == null) {
            synchronized (Easy4j.class){
                if (extProperties == null) {
                    Properties properties2 = new Properties();
                    Class<?> mainClass = Easy4j.mainClass;
                    if(Objects.nonNull(mainClass)){
                        String serverName = null;
                        int serverPort = 0;
                        String ejDataSource = null;
                        Easy4JStarter annotation = mainClass.getAnnotation(Easy4JStarter.class);
                        Easy4JStarterNd annotation2 = mainClass.getAnnotation(Easy4JStarterNd.class);
                        if(Objects.nonNull(annotation)){
                            serverName = annotation.serverName();
                            serverPort = annotation.serverPort();
                            ejDataSource = annotation.ejDataSourceUrl();
                        }else if(Objects.nonNull(annotation2)){
                            serverName = annotation2.serverName();
                            serverPort = annotation2.serverPort();
                            ejDataSource = annotation2.ejDataSourceUrl();
                        }
                        if(StrUtil.isNotBlank(serverName)){
                            properties2.setProperty(SysConstant.SERVER_NAME, serverName);
                        }
                        if(StrUtil.isNotBlank(ejDataSource)){
                            properties2.setProperty(SysConstant.DB_URL_STR_NEW, ejDataSource);
                        }
                        if(serverPort >0){
                            properties2.setProperty(SysConstant.SERVER_PORT,String.valueOf(serverPort));
                        }
                    }else{
                        System.err.println("not found mainClass...please check");
                    }
                    extProperties = properties2;
                }
            }
        }
    }

    /**
     * 获取系统参数
     * @return
     */
    public static EjSysProperties getEjSysProperties(){
        Binder binder = Binder.get(environment);
        BindResult<EjSysProperties> easy4j = binder.bind(SysConstant.PARAM_PREFIX, EjSysProperties.class);
        try{
            return easy4j.get();
        }catch (Exception e){
            return defaultEjSysProperties;
        }
    }





}
