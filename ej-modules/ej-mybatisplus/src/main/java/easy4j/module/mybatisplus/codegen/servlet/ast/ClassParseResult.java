package easy4j.module.mybatisplus.codegen.servlet.ast;

import lombok.Data;
import java.util.List;

/**
 * 类解析结果封装
 */
@Data
public class ClassParseResult {
    private String className;
    /** 表名（@Table注解的name属性） */
    private String tableName;
    /** Schema中文描述（@Schema注解的description属性） */
    private String schemaDesc;
    /** 主键字段名（@TableId标注的字段） */
    private String tableIdFieldName;
    /** 所有字段信息 */
    private List<ClassField> fields;
    /** 所有Controller的api */
    private List<String> allApiUrl;
}

