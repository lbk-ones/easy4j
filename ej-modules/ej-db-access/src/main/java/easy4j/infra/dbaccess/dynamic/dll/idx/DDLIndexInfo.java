package easy4j.infra.dbaccess.dynamic.dll.idx;

import easy4j.infra.common.annotations.Desc;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DDLIndexInfo {

    /**
     * schema
     */
    private String schema;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 索引的名称，可以不填，不填会按默认规则生成
     */
    private String name;
    /**
     * 索引类型名称
     */
    private String indexTypeName;
    /**
     * 索引名称前缀
     */
    private String indexNamePrefix;
    /**
     * 索引的键
     * 如果是特殊的键比如千缀索引 就写成这样`name`(20)
     */
    @Desc("必填")
    private String[] keys;
    /**
     * 索引的类型
     */
    private IndexType type;

    /**
     * 特殊索引解析的时候进行参数传参
     */
    private String[] args;

    /**
     * 注解信息
     */
    private DDLIndex ddlIndex;
}
