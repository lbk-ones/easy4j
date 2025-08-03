package easy4j.infra.dbaccess.dynamic.dll.idx;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据库索引类型枚举类
 * 包含MySQL、Oracle、SQL Server、PostgreSQL、H2、DB2支持的索引类型及描述
 *
 * @author bokun.li
 * @date 2025-08-03
 */
@Getter
public enum IndexType {
    // B-Tree索引：所有数据库的默认索引类型，支持大多数查询场景
    BTREE("", "all", "*", "最基础的索引类型，基于B-Tree数据结构，支持等值查询、范围查询、排序和分组操作，适用于大多数数据类型（整数、字符串、日期等）"),

    // 唯一索引：确保索引列值的唯一性
    UNIQUE("UNIQUE", "all", "*", "确保索引列的值唯一（允许NULL值，但NULL值只能出现一次），既用于数据完整性约束，也能提升查询性能"),

    // 复合索引：多字段组合的索引
    COMPOSITE("", "all", "*", "由多个字段组合创建的索引，遵循最左前缀原则，适用于查询条件包含多个字段的场景"),

    // 函数索引：基于函数或表达式的索引
    FUNCTION_BASED("", "all", "*", "基于字段的函数计算结果或表达式创建的索引，用于优化包含函数/表达式的查询条件"),

    // 空间索引：用于地理空间数据类型
    SPATIAL("SPATIAL", "all", "*", "专门用于地理空间数据类型（如POINT、GEOMETRY），支持空间关系查询（如包含、相交、距离计算等）"),
    // ===================== MySQL =====================,
    MYSQL_BTREE("", "mysql", "*", "默认索引类型，适用于大多数场景（等值查询、范围查询、排序等），支持多种数据类型"),
    MYSQL_HASH("HASH", "mysql", "Memory engine only", "仅Memory引擎支持，适用于精确等值查询，不支持范围查询和排序"),
    MYSQL_RTREE("RTREE", "mysql", "*", "用于空间数据类型（如GEOMETRY），支持空间关系查询（如包含、相交）"),
    MYSQL_FULLTEXT("FULLTEXT", "mysql", "*", "用于全文搜索，支持TEXT字段的关键词匹配，InnoDB和MyISAM均支持"),
    MYSQL_FUNCTIONAL("", "mysql", "8.0+", "基于字段的函数/表达式创建的索引，优化包含函数的查询"),
    MYSQL_PREFIX("PREFIX", "mysql", "*", "对字符串的前N个字符创建索引，节省空间，适用于长字符串"),
    MYSQL_UNIQUE("UNIQUE", "mysql", "*", "确保索引列值唯一（允许NULL，但NULL仅能出现一次）"),
    MYSQL_COMPOSITE("COMPOSITE", "mysql", "*", "多字段组合的索引，遵循最左前缀原则"),

    // ===================== Oracle =====================
    ORACLE_BTREE("", "oracle", "*", "默认类型，适用于高基数列（值唯一率高），支持等值、范围查询"),
    ORACLE_BITMAP("BITMAP", "oracle", "*", "适用于低基数列（如性别、状态），通过位图存储，适合多列组合查询"),
    ORACLE_HASH("HASH", "oracle", "*", "仅用于等值查询，不支持范围查询和排序，需手动创建"),
    ORACLE_FUNCTION_BASED("FUNCTION_BASED", "oracle", "*", "基于函数或表达式的索引，优化含函数的查询"),
    ORACLE_PARTITIONED("PARTITIONED", "oracle", "*", "与分区表配合，索引按分区拆分，提升大表查询效率"),
    ORACLE_REVERSE_KEY("REVERSE_KEY", "oracle", "*", "反转索引键值存储，解决高并发插入时的索引热点问题"),
    ORACLE_SPATIAL("SPATIAL", "oracle", "*", "用于地理空间数据（如SDO_GEOMETRY），支持空间关系查询"),
    ORACLE_TEXT("TEXT", "oracle", "*", "用于全文搜索，支持文档、大文本的关键词检索"),
    ORACLE_UNIQUE("UNIQUE", "oracle", "*", "确保列值唯一，与主键约束类似但可多列"),

    // ===================== SQL Server =====================
    SQLSERVER_CLUSTERED("CLUSTERED", "sqlserver", "*", "聚簇索引，决定数据物理存储顺序，一个表仅能有一个"),
    SQLSERVER_NONCLUSTERED("NONCLUSTERED", "sqlserver", "*", "非聚簇索引，独立于数据存储，一个表可多个"),
    SQLSERVER_UNIQUE("UNIQUE", "sqlserver", "*", "确保列值唯一，可与主键约束配合使用"),
    SQLSERVER_FILTERED("FILTERED", "sqlserver", "*", "仅对满足条件的部分数据创建索引，节省空间"),
    SQLSERVER_COLUMNSTORE("COLUMNSTORE", "sqlserver", "2012+", "面向列存储的索引，适用于数据仓库场景，提升聚合查询效率"),
    SQLSERVER_SPATIAL("SPATIAL", "sqlserver", "*", "用于地理空间数据（如GEOMETRY、GEOGRAPHY类型）"),
    SQLSERVER_FULLTEXT("FULLTEXT", "sqlserver", "*", "用于全文搜索，支持文档、大文本的关键词匹配"),
    SQLSERVER_XML("XML", "sqlserver", "*", "针对XML类型字段创建，优化XML数据的查询"),
    SQLSERVER_HASH("HASH", "sqlserver", "2014+", "适用于内存优化表的等值查询"),

    // ===================== PostgreSQL =====================
    POSTGRESQL_BTREE("", "postgresql", "*", "默认类型，支持大多数数据类型和查询场景（等值、范围、排序）"),
    POSTGRESQL_HASH("HASH", "postgresql", "10+", "适用于等值查询，PostgreSQL 10+后支持WAL日志，稳定性提升"),
    POSTGRESQL_GIST("GIST", "postgresql", "*", "通用搜索树，支持空间数据、全文搜索、数组等复杂类型"),
    POSTGRESQL_GIN("GIN", "postgresql", "*", "反转索引，适用于多值类型（如数组、JSONB）和全文搜索"),
    POSTGRESQL_SPGIST("SPGIST", "postgresql", "*", "空间分区GiST，适用于非平衡数据结构（如电话号码、IP地址）"),
    POSTGRESQL_BRIN("BRIN", "postgresql", "9.5+", "块范围索引，适用于大表且数据有序的场景（如时间序列表）"),
    POSTGRESQL_EXPRESSION("EXPRESSION", "postgresql", "*", "基于函数或表达式的索引"),
    POSTGRESQL_PARTIAL("PARTIAL", "postgresql", "*", "仅对表的部分行创建索引（如WHERE is_deleted = false）"),
    POSTGRESQL_UNIQUE("UNIQUE", "postgresql", "*", "确保列值唯一，支持部分唯一（结合Partial索引）"),

    // ===================== H2 =====================
    H2_BTREE("", "h2", "*", "默认类型，支持等值、范围查询和排序"),
    H2_HASH("HASH", "h2", "*", "适用于等值查询，不支持范围查询"),
    H2_UNIQUE("UNIQUE", "h2", "*", "确保列值唯一"),
    H2_COMPOSITE("COMPOSITE", "h2", "*", "多字段组合索引"),
    H2_FUNCTION("FUNCTION", "h2", "*", "支持基于函数的索引（如YEAR(date_col)）"),
    H2_SPATIAL("SPATIAL", "h2", "*", "支持简单的空间数据类型（如POINT）"),

    // ===================== DB2 =====================
    DB2_BTREE("", "db2", "*", "默认类型，支持大多数查询场景"),
    DB2_HASH("HASH", "db2", "*", "适用于等值查询，不支持范围查询和排序"),
    DB2_BITMAP("BITMAP", "db2", "*", "适用于低基数列，优化多列组合查询"),
    DB2_FUNCTION_BASED("FUNCTION_BASED", "db2", "*", "基于函数或表达式的索引"),
    DB2_PARTITIONED("PARTITIONED", "db2", "*", "与分区表配合，按分区拆分索引"),
    DB2_SPATIAL("SPATIAL", "db2", "*", "用于地理空间数据类型，支持空间查询"),
    DB2_XML("XML", "db2", "*", "针对XML类型字段，优化XML路径查询"),
    DB2_UNIQUE("UNIQUE", "db2", "*", "确保列值唯一，支持复合唯一约束");

    // getter方法
    // 索引名称
    private final String indexName;
    // 支持的数据库类型（全小写）
    private final String dbType;
    // 支持的数据库版本
    private final String dbVersion;
    // 索引描述
    private final String desc;

    IndexType(String indexName, String dbType, String dbVersion, String desc) {
        this.indexName = indexName;
        this.dbType = dbType;
        this.dbVersion = dbVersion;
        this.desc = desc;
    }

    // 获取指定数据库支持的所有索引类型
    public static List<IndexType> getByDbType(String dbType) {
        return Arrays.stream(values())
                .filter(type -> type.dbType.equalsIgnoreCase(dbType))
                .collect(Collectors.toList());
    }

    // 获取指定数据库和索引名称的索引类型
    public static Optional<IndexType> getByDbTypeAndIndexName(String dbType, String indexName) {
        return Arrays.stream(values())
                .filter(type -> type.dbType.equalsIgnoreCase(dbType) &&
                        type.indexName.equalsIgnoreCase(indexName))
                .findFirst();
    }

    public static IndexType getByName(String enumName) {

        IndexType[] values = IndexType.values();
        for (IndexType value : values) {
            String name = value.name();
            if (StrUtil.equals(name, enumName)) {
                return value;
            }
        }
        return null;

    }

}
