package ${parentPackageName}.${entityPackageName};
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
<#list importList as imp>
import ${imp};
</#list>

/**
 * <p>
 * ${description}
 * </p>
 *
 * @author ${author}
 * @since ${.now}
 */
@Getter
@Setter
@ToString
@TableName("${tableName}")
@Schema(name = "${schema}", description = "${description}")
<#if hasExtend>
public class ${schema} extends AutoAudit implements Serializable  {
<#else>
public class ${schema} implements Serializable {
</#if>

    private static final long serialVersionUID = ${serialVersionId}L;

<#list fieldInfoList as field>

    <#if field?? && field.description??>
    /**
     * ${field.description!""}
     */
    @Schema(description = "${field.description!""}")
    </#if>
    <#if field.hasPrimaryKey && field.hasAutoincrement>
    @TableId(value = "${field.dbName!""}", type = IdType.AUTO)
    <#elseif field.hasPrimaryKey>
    @TableId("${field.dbName!""}")
    <#else>
    @TableField("${field.dbName!""}")
    </#if>
    private ${field.type!""} ${field.name!""};

</#list>

}
