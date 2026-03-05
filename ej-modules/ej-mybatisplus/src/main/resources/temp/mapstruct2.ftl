package ${parentPackageName}.${mapperStructPackageName};
<#list importList as imp>
import ${imp};
</#list>

@Mapper(uses = {TransferMapper.class})
public interface ${mapperStructClassSimpleName} {
    ${mapperStructClassSimpleName} instance = Mappers.getMapper(${mapperStructClassSimpleName}.class);

<#list methodList as method>
    ${method.returnTypeName} ${method.methodName}(${method.params});
</#list>


}