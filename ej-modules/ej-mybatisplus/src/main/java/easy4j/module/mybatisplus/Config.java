/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.mybatisplus;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisPlusVersion;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.MybatisMapWrapperFactory;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.module.mybatisplus.audit.AutoAuditHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

import java.util.List;

/**
 * MYBATIS PLUS 配置
 *
 * @author bokun.li
 * @date 2023/11/18
 */
@Configuration
@AutoConfigureBefore(value = {MybatisPlusAutoConfiguration.class})
@Slf4j
public class Config implements EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


//    @Bean
//    public MapperScannerConfigurer mapperScannerRegistrar() {
//        MapperScannerConfigurer mapperScannerRegistrar = new MapperScannerConfigurer();
//        mapperScannerRegistrar.setBasePackage("");
//        return mapperScannerRegistrar;
//    }

    /**
     * mybatis 的几个默认配置
     * 1、枚举扫描            xxx.enums
     * 2、xml扫描
     * 3、typeHandler处理器  xxx.domains
     * 4、mapper接口扫描
     *
     * @param dataSource
     * @param globalConfig
     * @return
     * @throws Exception
     */
    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, GlobalConfig globalConfig) throws Exception {
        log.info(SysLog.compact("开始配置MYBATIS_PLUS"));
        String dataType = Easy4j.getType();
        DbType dbType = DbType.getDbType(dataType);
        String db = dbType.getDb();
        log.info(SysLog.compact("判定数据库为,{}", db));

        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        // 数据源
        sqlSessionFactory.setDataSource(dataSource);

        String enumPath = Easy4j.mainClassPath + SysConstant.DOT + SysConstant.ENUMS;
        // 枚举扫描
        sqlSessionFactory.setTypeAliasesPackage(enumPath);
        String xmlLocation = "classpath*:/mappers/" + db + "/**/*.xml";
        log.info(SysLog.compact("xml文件扫描路径,{}", xmlLocation));
        // xml扫描
        sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(xmlLocation));

        // 扫描 typeHandler
        String domainPath = Easy4j.mainClassPath + SysConstant.DOT + SysConstant.DOMAINS;
        log.info(SysLog.compact("实体domain路径,{}", domainPath));
        sqlSessionFactory.setTypeHandlersPackage(domainPath);
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        // 驼峰转下划线
        configuration.setMapUnderscoreToCamelCase(true);
        // 拦截器
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
//        new PaginationInnerInterceptor()
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        log.info(SysLog.compact("分页插件已经配置"));
        // 乐观锁插件
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        log.info(SysLog.compact("乐观锁插件已经配置"));
        // 阻止恶意的全表更新删除
        mybatisPlusInterceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        log.info(SysLog.compact("阻止恶意的全表更新删除"));
        sqlSessionFactory.setPlugins(mybatisPlusInterceptor);

        // map 下划线转驼峰
        configuration.setObjectWrapperFactory(new MybatisMapWrapperFactory());
        log.info(SysLog.compact("mybatis-plus 开启下划线转驼峰"));
        sqlSessionFactory.setConfiguration(configuration);
        // 自动填充插件
        globalConfig.setMetaObjectHandler(new AutoAuditHandler());
        log.info(SysLog.compact("自定义自动审计"));
        globalConfig.setBanner(false);
        sqlSessionFactory.setGlobalConfig(globalConfig);
        log.info(SysLog.compact("MYBATIS_PLUS 初始化完毕【" + MybatisPlusVersion.getVersion() + "】"));
        return sqlSessionFactory.getObject();
    }

    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig conf = new GlobalConfig();
        //conf.setDbConfig(new GlobalConfig.DbConfig().setColumnFormat("`%s`"));
        DefaultSqlInjector logicSqlInjector = new DefaultSqlInjector() {
            /**
             * 注入自定义全局方法
             */
            @Override
            public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
                List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
                // 不要逻辑删除字段, 不要乐观锁字段, 不要填充策略是 UPDATE 的字段
                methodList.add(new InsertBatchSomeColumn(t -> !t.isLogicDelete() && !t.isVersion() && t.getFieldFill() != FieldFill.UPDATE));
                // 不要填充策略是 INSERT 的字段, 不要字段名是 column4 的字段
                methodList.add(new AlwaysUpdateSomeColumnById(t -> t.getFieldFill() != FieldFill.INSERT && !t.getProperty().equals("column4")));
                return methodList;
            }
        };
        conf.setSqlInjector(logicSqlInjector);
        return conf;
    }

//    private String getQuoteStringByDbType(DbType dbType) {
//        switch (dbType) {
//            case MYSQL:
//            case SQL_SERVER:
//                return "`";  // MySQL和SQL Server使用反引号
//            case POSTGRE_SQL:
//            case ORACLE:
//            case DB2:
//                return "\"";  // PostgreSQL、Oracle、DB2使用双引号
//            default:
//                return "`";   // 默认使用反引号
//        }
//    }


    /**
     * 添加插件
     */
    /*@Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info(SysLog.compact("开始配置MYBATIS_PLUS"));
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        String property = environment.getRequiredProperty("spring.datasource.url");
        log.info(SysLog.compact("检测到数据源地址,{}",property));
        String dataType = SqlType.getDataTypeByUrl(property);
        DbType dbType = DbType.getDbType(dataType);
        String db = dbType.getDb();
        log.info(SysLog.compact("判定数据库为,{}",db));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));//如果配置多个插件,切记分页最后添加
        log.info(SysLog.compact("分页插件已经配置"));
        //interceptor.addInnerInterceptor(new PaginationInnerInterceptor()); 如果有多数据源可以不配具体类型 否则都建议配上具体的DbType
        // 针对 update 和 delete 语句 作用: 阻止恶意的全表更新删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        log.info(SysLog.compact("阻止恶意更新和删除"));
        return interceptor;
    }

    @Bean
    public AutoAudit autoAdjust(){
        log.info(SysLog.compact("开启自动审计 DEMO见 AuditDemo"));
        log.info(SysLog.compact("MYBATIS_PLUS配置结束"));
        return new AutoAudit();
    }*/
    @Bean
    public IdGenner idGenner() {
        return new IdGenner();
    }

}
