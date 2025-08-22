package easy4j.infra.dbaccess.dynamic.dll.op.meta;

import lombok.Data;

/**
 * 数据库主键元数据实体类
 * 存储数据库表主键的相关信息
 */
@Data
public class PrimaryKeyMetadata {

    /**
     * 表目录（可能为null）
     * 说明：数据库表所在的目录，不同数据库对目录的定义可能不同
     */
    private String tableCat;

    /**
     * 表模式（可能为null）
     * 说明：数据库表所在的模式（schema），用于表的逻辑分组
     */
    private String tableSchem;

    /**
     * 表名
     * 说明：当前主键所属的表的名称
     */
    private String tableName;

    /**
     * 列名
     * 说明：作为主键的列的名称
     */
    private String columnName;

    /**
     * 主键中的序列号
     * 说明：表示该列在主键中的位置，1表示主键的第一列，2表示主键的第二列，以此类推
     */
    private short keySeq;

    /**
     * 主键名称（可能为null）
     * 说明：数据库中定义的主键约束的名称
     */
    private String pkName;
}
