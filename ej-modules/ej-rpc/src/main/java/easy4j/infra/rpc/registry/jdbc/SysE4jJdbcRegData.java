package easy4j.infra.rpc.registry.jdbc;

import lombok.Data;

import java.util.Date;

/**
 * 表名 SYS_E4J_JDBC_REG_DATA
 */
@Data
public class SysE4jJdbcRegData {

    /**
     * 主键 一定自增
     */
    private Long id;

    /**
     * key
     */
    private String dataKey;

    /**
     * value
     */
    private String dataValue;

    /**
     * 数据类型 临时节点 0，存储节点 1
     */
    private String dataType;

    /**
     * create date
     */
    private Date createDate;

    /**
     * 更新时间
     */
    private Date lastUpdateDate;

}
