package easy4j.infra.dbaccess.dynamic.dll.op.meta;

import lombok.Data;

/**
 * 数据库表列元数据实体类
 * 对应数据库元数据查询（如 DatabaseMetaData.getColumns()）返回的列信息
 */
@Data
public class DatabaseColumnMetadata {

    /**
     * 表目录（可能为null）
     * 说明：不同数据库对“目录”定义不同，PostgreSQL中通常对应数据库名，MySQL中可理解为数据库实例下的逻辑分组
     */
    private String tableCatalog;

    /**
     * 表模式（可能为null）
     * 说明：通常对应数据库中的“ schema ”，用于分组管理表（如PostgreSQL的public、MySQL的数据库名）
     */
    private String tableSchem;

    /**
     * 表名
     * 说明：当前列所属的数据库表名称（原始表名，未做大小写转换）
     */
    private String tableName;

    /**
     * 列名
     * 说明：当前列的数据库原始名称（未做下划线/驼峰转换）
     */
    private String columnName;

    /**
     * SQL类型（对应java.sql.Types的枚举值）
     * 说明：如 Types.VARCHAR(12)、Types.INTEGER(4)，表示列的标准SQL数据类型编码
     */
    private int dataType;

    /**
     * 数据源依赖的类型名称
     * 说明：数据库特定的类型名（如PostgreSQL的jsonb、MySQL的datetime）；若为UDT（用户自定义类型），则是全限定名
     */
    private String typeName;

    /**
     * 列大小
     * 说明：不同类型含义不同——字符串类型表示最大长度（如VARCHAR(50)的50），数值类型表示总位数（如DECIMAL(10,2)的10）
     */
    private int columnSize;

    /**
     * 缓冲区长度（未使用）
     * 说明：历史预留字段，当前数据库元数据查询中无实际意义，值通常为null或默认值
     */
    private Integer bufferLength;

    /**
     * 小数位数
     * 说明：仅对数值类型有效（如DECIMAL(10,2)的2）；对非数值类型（如字符串、日期）返回null
     */
    private Integer decimalDigits;

    /**
     * 精度基数（通常为10或2）
     * 说明：10表示十进制（如整数、小数），2表示二进制（如二进制数据、布尔值）
     */
    private int numPrecRadix;

    /**
     * 是否允许为null（枚举值）
     * 说明：对应三个固定值——columnNoNulls(0)：不允许null；columnNullable(1)：允许null；columnNullableUnknown(2)：未知
     */
    private int nullable;

    /**
     * 列注释（可能为null）
     * 说明：数据库表列的备注信息，用于描述列的业务含义（如“用户手机号，唯一标识”）
     */
    private String remarks;

    /**
     * 列默认值（可能为null）
     * 说明：列的默认值字符串，若默认值为字符串类型，会包含单引号（如默认值为'未知'，则该字段值为"'未知'"）
     */
    private String columnDef;

    /**
     * SQL数据类型（未使用）
     * 说明：历史预留字段，与dataType功能重复，当前无实际使用场景
     */
    private Integer sqlDataType;

    /**
     * SQL日期时间子类型（未使用）
     * 说明：历史预留字段，用于区分日期时间的细分类型（如DATE/TIME/TIMESTAMP），当前由typeName字段替代
     */
    private Integer sqlDatetimeSub;

    /**
     * 字符类型的最大字节数
     * 说明：仅对字符类型（如VARCHAR、CHAR）有效，根据字符集计算（如UTF-8编码下，VARCHAR(10)的最大字节数为30）
     */
    private Integer charOctetLength;

    /**
     * 列在表中的位置（从1开始）
     * 说明：表示列在表定义中的顺序，如第一列值为1，第二列值为2，依次类推
     */
    private int ordinalPosition;

    /**
     * 是否允许为null（ISO标准规则，字符串形式）
     * 说明：YES：允许null；NO：不允许null；空字符串：未知（与nullable字段含义对应，但格式不同）
     */
    private String isNullable;

    /**
     * 引用属性的作用域目录（可能为null）
     * 说明：仅当dataType为REF类型（引用类型）时有效，表示引用字段所在表的目录；非REF类型返回null
     */
    private String scopeCatalog;

    /**
     * 引用属性的作用域模式（可能为null）
     * 说明：仅当dataType为REF类型时有效，表示引用字段所在表的schema；非REF类型返回null
     */
    private String scopeSchema;

    /**
     * 引用属性的作用域表名（可能为null）
     * 说明：仅当dataType为REF类型时有效，表示引用字段所在的表名；非REF类型返回null
     */
    private String scopeTable;

    /**
     * 源数据类型（可能为null）
     * 说明：仅当dataType为DISTINCT（ distinct类型）或用户自定义REF类型时有效，值为对应源类型的java.sql.Types编码；其他类型返回null
     */
    private Short sourceDataType;

    /**
     * 是否为自增列（字符串形式）
     * 说明：YES：自增列（如MySQL的AUTO_INCREMENT、PostgreSQL的IDENTITY）；NO：非自增列；空字符串：无法确定
     */
    private String isAutoincrement;

    /**
     * 是否为生成列（字符串形式）
     * 说明：YES：生成列（如PostgreSQL的GENERATED ALWAYS AS计算列）；NO：非生成列；空字符串：无法确定
     */
    private String isGeneratedcolumn;
}