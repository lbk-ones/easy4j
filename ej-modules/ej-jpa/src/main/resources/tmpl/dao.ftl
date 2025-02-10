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
public interface ${daoClassName} extends JpaRepository<${domainName},String>, JpaSpecificationExecutor<${domainName}> {

}