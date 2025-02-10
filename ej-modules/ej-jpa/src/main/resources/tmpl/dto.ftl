package ${packageName};
<#list importList as imp>
import ${imp};
</#list>
<#list lineList as line>
${line}
</#list>
<#list annotationList as anno>
@${anno}
</#list>
public class ${dtoClassName} extends BaseDto {

<#list fieldList as field>
    <#list field.fieldLine as fline>
    ${fline}
    </#list>
    private ${field.type} ${field.name};

</#list>

    @Override
    public void toNewEntityValidate() throws EasyException {

    }
}