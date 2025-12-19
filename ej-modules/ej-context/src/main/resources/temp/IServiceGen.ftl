package ${parentPackageName}.${serviceInterfacePackageName};

import com.baomidou.mybatisplus.extension.service.IService;
import ${parentPackageName}.controller.req.${domainName}ControllerReq;
import ${parentPackageName}.domains.${entityName};
import ${parentPackageName}.dto.${entityName}Dto;
import easy4j.module.mybatisplus.base.EasyPageRes;
import java.util.List;
/**
 * ${headerDesc}
 * <p/>
 * @author ${author}
 * @since ${.now}
 */
public interface I${domainName}Service extends IService<${entityName}> {
    EasyPageRes pageQuery${domainName}(${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReq);

    List<${entityName}Dto> getAllEnableNotDelete();

    List<${entityName}Dto> save${domainName}(${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReq);

    List<${entityName}Dto> get${domainName}ByIds(List<String> ids);

    List<${entityName}Dto> publish${domainName}s(${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReq);

    List<${entityName}Dto> delete${domainName}s(List<String> ids);

    List<${entityName}Dto> batchUpdate${domainName}(${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReqs);

    List<${entityName}Dto> copy${domainName}(${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReqs);

    List<${entityName}Dto> enableOrDisable${domainName}(${domainName}ControllerReq ${(domainName?substring(0,1))?lower_case + (domainName?substring(1))}ControllerReqs);
}
