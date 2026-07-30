package easy4j.infra.dbaccess.orm;

import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 全局配置，无默认值，默认值使用 AccessConfig
 *
 * @since 2.1.4
 */
@Data
@ConfigurationProperties(prefix = SpringOrmProperties.ORM_PREFIX)
public class SpringOrmProperties {

    public static final String ORM_PREFIX = SysConstant.PARAM_PREFIX + SP.DOT + "orm";

    /**
     * 是否加入spring当前事务,默认 true
     */
    private Boolean inTransaction;

    /**
     * 是否打印sql 默认true
     */
    private Boolean printSqlIs;

    /**
     * 默认只打印慢sql,如果需要打印全部sql，请改为false 默认true
     */
    private Boolean onlyPrintSlowSql;

    /**
     * 慢sql的定义时间
     */
    private Long slowSqlTime;

    /**
     * 字段名称是否转下划线
     */
    private Boolean fieldNameToUnderline;

    /**
     *  oracle的字段是否自动大写 默认自动大写
     */
    private Boolean oracleAutoUpperCase;

    /**
     * db2的字段是否自动大写 默认自动大写
     */
    private Boolean db2AutoUpperCase;

    /**
     * postgresql的字段是否自动小写 默认自动小写
     */
    private Boolean pgAutoLowerCase;

    /**
     * h2的字段是否自动大写 默认自动小写
     */
    private Boolean h2AutoUpperCase;

    // oracle 写入策略 默认第一种
    // 1: 循环单条写入（带主键回写）
    // 2: insert into value () select x1,x2 from dual union all select x1,x2 from dual 这种写法不带回写
    private Integer oracleWriteStrategy;

    /**
     * 要启动的plugin名称集合
     */
    private List<String> plugins;

    /**
     * 忽略tenant插件的的表名集合
     */
    private List<String> ignoreTenantIdTables;

    /**
     * 忽略tenant插件的的表前缀集合
     */
    private List<String> ignoreTenantIdTablePrefix;

    /**
     * 全局tenant字段的名称
     */
    private String globalTenantIdName;

}
