package easy4j.infra.rpc.registry.jdbc;

import easy4j.infra.rpc.registry.jdbc.ddl.FieldInfo;
import lombok.Data;

import java.sql.JDBCType;
import java.util.Date;

/**
 * 表名 sys_e4j_jdbc_reg_data 注册表
 */
@Data
public class SysE4jJdbcRegData {

    /**
     * 主键 自增
     */
    private Long id;

    /**
     * 字段名称：data_key 是否索引：是 长度：2000 注释:键值key
     */
    private String dataKey;

    /**
     * 字段名称： data_value 字段类型： 超大字符串 注释:键值value
     */
    private String dataValue;

    /**
     * 字段名称：data_type 是否索引：是 长度：1  注释：数据类型 临时节点 0，存储节点 1
     */
    private String dataType;

    /**
     * 字段名称:create_date 字段类型：时间 注释：创建时间
     */
    private Date createDate;

    /**
     * 字段名称:last_update_date 字段类型：时间 注释：更新时间
     */
    private Date lastUpdateDate;

}
