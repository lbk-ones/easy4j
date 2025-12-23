package ${parentPackageName}.${dtoPackageName};
<#if sameSchema>
<#else>
import io.swagger.v3.oas.annotations.media.Schema;
</#if>
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
    </#if>
<#if field.sameSchema>
    @io.swagger.v3.oas.annotations.media.Schema(description = "${field.description!""}")
<#else>
    @Schema(description = "${field.description!""}")
</#if>
    private ${field.type!""} ${field.name!""};

</#list>

}
