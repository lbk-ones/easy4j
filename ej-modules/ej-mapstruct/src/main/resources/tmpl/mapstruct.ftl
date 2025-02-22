package ${currentPackageName};
<#list importList as imp>
import ${imp};
</#list>
import easy4j.module.mapstruct.TransferMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
<#list lineList as line>
${line}
</#list>
<#list annotationList as anno>
@${anno}
</#list>
@Mapper(uses = {TransferMapper.class})
public interface ${mapperStructInterfaceName} {
    ${mapperStructInterfaceName} instance = Mappers.getMapper(${mapperStructInterfaceName}.class);

<#list methodList as method>
    ${method.returnTypeName} ${method.methodName}(${method.params});
</#list>


}