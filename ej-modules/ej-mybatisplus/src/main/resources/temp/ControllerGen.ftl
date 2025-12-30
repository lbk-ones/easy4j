package ${parentPackageName}.controller;
import cn.hutool.core.util.EscapeUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import ${parentPackageName}.${controllerReqPackageName}.${domainName}ControllerReq;
import ${parentPackageName}.${dtoPackageName}.${returnDtoName};
import ${parentPackageName}.${serviceInterfacePackageName}.I${domainName}Service;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.knife4j.ControllerModule;
import easy4j.infra.knife4j.GlobalApiResponses;
import easy4j.infra.knife4j.GlobalXAccessToken;
import easy4j.infra.log.RequestLog;
import easy4j.module.idempotent.WebIdempotent;
import easy4j.module.mybatisplus.base.EasyPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * ${headerDesc}
 * <p/>
 * @author ${author}
 * @since ${.now}
 */
@RestController
@RequestMapping(${domainName}Controller.${domainName?upper_case}_URL)
@ControllerModule(name = ${domainName}Controller.${domainName?upper_case}_URL, description = "${cnDesc}")// api文档分组的可以没有
@Tag(name = "${cnDesc}", description = "${cnDesc}相关查询和操作，不需要的接口不用管")
public class ${domainName}Controller {

    public static final String ${domainName?upper_case}_URL = "${urlPrefix}/${domainName?lower_case}";

    @Resource
    I${domainName}Service i${domainName}Service;

    @Operation(summary = "${cnDesc}分页查询", description = "${cnDesc}分页查询，不需要该功能则不理会")
    @PostMapping("pageQuery${domainName}")
    @SentinelResource(value = ${domainName?upper_case}_URL +"-pageQuery${domainName}")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为map",
            content = @Content(
                    schema = @Schema(
                            implementation = EasyPageRes.class
                    )
            )
    )
    public EasyResult<EasyPageRes> pageQuery${domainName}(${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReq){
        return EasyResult.ok(i${domainName}Service.pageQuery${domainName}(${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReq));
    }

    @Operation(summary = "查询所有已启用的${cnDesc}", description = "查询所有已启用的${cnDesc}，不需要该功能则不理会")
    @GetMapping("getAllEnableNotDelete")
    @SentinelResource(value = ${domainName?upper_case}_URL +"-getAllEnableNotDelete")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为集合",
            content = @Content(
                    schema = @Schema(
                            implementation = ${returnDtoName}.class
                    )
            )
    )
    public EasyResult<List<${returnDtoName}>> getAllEnableNotDelete(){
        return EasyResult.ok(i${domainName}Service.getAllEnableNotDelete());
    }

    @Operation(summary = "${cnDesc}保存", description = "新增或批量新增${cnDesc}，不需要该功能则不理会")
    @RequestLog
    @WebIdempotent
    @PostMapping("save${domainName}")
    @SentinelResource(value = ${domainName?upper_case}_URL +"-save${domainName}")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为集合",
            content = @Content(
                    schema = @Schema(
                            implementation = ${returnDtoName}.class
                    )
            )
    )
    public EasyResult<List<${returnDtoName}>> save${domainName}(@RequestBody ${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReq) {
        return EasyResult.ok(i${domainName}Service.save${domainName}(${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReq));
    }


    @Operation(summary = "${cnDesc}批量查询", description = "根据主键查询或批量查询${cnDesc}(批量查询用,分割)，不需要该功能则不理会")
    @GetMapping("get${domainName}ById/{id}")
    @SentinelResource(value = ${domainName?upper_case}_URL +"-get${domainName}ById")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为集合",
            content = @Content(
                    schema = @Schema(
                            implementation = ${returnDtoName}.class
                    )
            )
    )
    public EasyResult<List<${returnDtoName}>> get${domainName}ByIds(@PathVariable(name = "id") String ids) {
        return EasyResult.ok(i${domainName}Service.get${domainName}ByIds(new ArrayList<>(ListTs.asList(ids.split(EscapeUtil.escape(SP.COMMA))))));
    }

    @Operation(summary = "${cnDesc}发布", description = "批量发布${cnDesc}（发布后生效，不可随意修改）/ 可批量发布，返回集合，不需要发布则不理会")
    @RequestLog
    @WebIdempotent
    @PostMapping("publish${domainName}s")
    @SentinelResource(value = ${domainName?upper_case}_URL +"-publish${domainName}s")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为集合",
            content = @Content(
                    schema = @Schema(
                            implementation = ${returnDtoName}.class
                    )
            )
    )
    public EasyResult<List<${returnDtoName}>> publish${domainName}s(@RequestBody ${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReq) {
        return EasyResult.ok(i${domainName}Service.publish${domainName}s(${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReq));
    }

    @Operation(summary = "${cnDesc}批量删除", description = "批量删除${cnDesc}，用英文逗号分割，不需要该功能则不理会")
    @RequestLog
    @WebIdempotent
    @DeleteMapping("delete${domainName}/{id}")
    @SentinelResource(value = ${domainName?upper_case}_URL +"-delete${domainName}s")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为集合",
            content = @Content(
                    schema = @Schema(
                            implementation = ${returnDtoName}.class
                    )
            )
    )
    public EasyResult<List<${returnDtoName}>> delete${domainName}s(@PathVariable(name = "id") String ids) {
        return EasyResult.ok(i${domainName}Service.delete${domainName}s(new ArrayList<>(ListTs.asList(ids.split(EscapeUtil.escape(SP.COMMA))))));
    }


    @Operation(summary = "${cnDesc}编辑", description = "根据主键编辑或者批量编辑${cnDesc}，不需要该功能则不理会")
    @RequestLog
    @WebIdempotent
    @PutMapping("update${domainName}")
    @SentinelResource(value = ${domainName?upper_case}_URL +"-update${domainName}")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为集合",
            content = @Content(
                    schema = @Schema(
                            implementation = ${returnDtoName}.class
                    )
            )
    )
    public EasyResult<List<${returnDtoName}>> update${domainName}(@RequestBody ${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReqs) {
        return EasyResult.ok(i${domainName}Service.batchUpdate${domainName}(${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReqs));
    }


    @Operation(summary = "${cnDesc}复制", description = "复制已有${cnDesc}生成新的草稿流程(传入集合可批量操作)，不需要该功能则不理会")
    @RequestLog
    @WebIdempotent
    @PostMapping("copy${domainName}")
    @SentinelResource(value = ${domainName?upper_case}_URL +"-copy${domainName}")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为集合",
            content = @Content(
                    schema = @Schema(
                            implementation = ${returnDtoName}.class
                    )
            )
    )
    public EasyResult<List<${returnDtoName}>> copy${domainName}(@RequestBody ${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReqs) {
        return EasyResult.ok(i${domainName}Service.copy${domainName}(${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReqs));
    }

    @Operation(summary = "${cnDesc}启用或禁用", description = "批量进行启用或者禁用已有的${cnDesc}(传入集合可批量操作)，不需要该功能则不理会")
    @RequestLog
    @WebIdempotent
    @PostMapping("enableOrDisable${domainName}")
    @SentinelResource(value = ${domainName?upper_case}_URL +"-enableOrDisable${domainName}")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为集合",
            content = @Content(
                    schema = @Schema(
                            implementation = ${returnDtoName}.class
                    )
            )
    )
    public EasyResult<List<${returnDtoName}>> enableOrDisable${domainName}(@RequestBody ${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReqs) {
        return EasyResult.ok(i${domainName}Service.enableOrDisable${domainName}(${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReqs));
    }
}
