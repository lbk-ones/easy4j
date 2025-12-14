package ${packageName};

<#list importList as imp>
import ${imp};
</#list>
import easy4j.module.base.header.EasyResult;
import easy4j.module.jpa.base.BaseController;
import easy4j.module.sentinel.annotation.FlowDegradeResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

<#list lineList as line>
${line}
</#list>
<#list annotationList as anno>
@${anno}
</#list>
@RestController
@RequestMapping("${firstLowDomainName}")
public class ${domainName}Controller extends BaseController {

    @Resource
    private I${domainName}Service i${domainName}Service;

    @Operation(summary = "获取${genDomainName}列表")
    @PostMapping("get${domainName}List")
    @FlowDegradeResource(value = "get${domainName}List")
    public EasyResult<Map<String,Object>> get${domainName}List(@RequestBody ${domainName}Dto ${firstLowDomainName}Dto){
        Date date = new Date();
        Map<String, Object> res = i${domainName}Service.get${domainName}List(${firstLowDomainName}Dto);
        return EasyResult.ok(date,res);
    }

    @Operation(summary = "保存${genDomainName}")
    @PostMapping("save${domainName}")
    @FlowDegradeResource(value = "save${domainName}")
    public EasyResult<List<${domainName}Dto>> save${domainName}(@RequestBody List<${domainName}Dto> ${firstLowDomainName}Dtos){
        Date beginDate = new Date();
        List<${domainName}Dto> resList = i${domainName}Service.save${domainName}(${firstLowDomainName}Dtos);
        return EasyResult.ok(beginDate, resList);
    }

    @Operation(summary = "修改${genDomainName}")
    @PostMapping("update${domainName}")
    @FlowDegradeResource(value = "update${domainName}")
    public EasyResult<List<${domainName}Dto>> update${domainName}(@RequestBody List<${domainName}Dto> ${firstLowDomainName}Dtos){
        Date beginDate = new Date();
        List<${domainName}Dto> resList = i${domainName}Service.update${domainName}(${firstLowDomainName}Dtos);
        return EasyResult.ok(beginDate, resList);
    }

    @Operation(summary = "删除${genDomainName}")
    @PostMapping("delete${domainName}")
    @FlowDegradeResource(value = "delete${domainName}")
    public EasyResult<List<String>> delete${domainName}(@RequestBody List<${domainName}Dto> ${firstLowDomainName}Dtos){
        Date beginDate = new Date();
        List<String> resList = i${domainName}Service.delete${domainName}(${firstLowDomainName}Dtos);
        return EasyResult.ok(beginDate, resList);
    }

    @Operation(summary = "启用和禁用${genDomainName}")
    @PostMapping("enableOrDisabled${domainName}")
    @FlowDegradeResource(value = "enableOrDisabled${domainName}")
    public EasyResult<List<String>> enableOrDisabled${domainName}(@RequestBody List<${domainName}Dto> ${firstLowDomainName}Dtos){
        Date beginDate = new Date();
        List<String> resList = i${domainName}Service.enableOrDisabled${domainName}(${firstLowDomainName}Dtos);
        return EasyResult.ok(beginDate, resList);
    }
    @Operation(summary = "根据主键集合获取${genDomainName}")
	@PostMapping("get${domainName}ByIds")
    @FlowDegradeResource(value = "get${domainName}ByIds")
    public EasyResult<List<${domainName}Dto>> get${domainName}ByIds(@RequestBody List<String> ${firstLowDomainName}Ids){
        Date beginDate = new Date();
        List<${domainName}Dto> resList = i${domainName}Service.get${domainName}ByIds(${firstLowDomainName}Ids);
        return EasyResult.ok(beginDate, resList);
    }
    
}
