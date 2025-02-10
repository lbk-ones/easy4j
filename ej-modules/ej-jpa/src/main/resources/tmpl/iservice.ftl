package ${packageName};
<#list importList as imp>
import ${imp};
</#list>
import java.util.List;
import java.util.Map;

<#list lineList as line>
${line}
</#list>
<#list annotationList as anno>
@${anno}
</#list>
public interface ${interfaceName} {

    Map<String,Object> get${domainName}List(${domainName}Dto ${firstLowDomainName}Dto);

    List<${domainName}Dto> save${domainName}(List<${domainName}Dto> ${firstLowDomainName}Dtos);

    List<${domainName}Dto> update${domainName}(List<${domainName}Dto> ${firstLowDomainName}Dtos);

    List<String> delete${domainName}(List<${domainName}Dto> ${firstLowDomainName}Dtos);

    List<String> enableOrDisabled(List<${domainName}Dto> ${firstLowDomainName}Dtos);

    List<${domainName}Dto> get${domainName}ByIds(List<String> ${firstLowDomainName}Ids);
}