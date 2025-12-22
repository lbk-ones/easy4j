
package easy4j.module.mybatisplus.codegen;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class GenDto extends GlobalGenConfig {

    // 实体名称 驼峰 帕斯卡命名发 必须是大写
    String domainName;

    // 中文描述
    String cnDesc;

    // 返回的dto名称
    String returnDtoName;


    // 数据库实体类名称
    String entityName;


    public GenDto setAuthor(String author) {
        if (author != null) {
            this.author = author;
        }
        return this;
    }

    public GenDto setUrlPrefix(String urlPrefix) {
        if (StrUtil.endWith(urlPrefix, SP.SLASH)) {
            urlPrefix = StrUtil.replaceLast(urlPrefix, SP.SLASH, "");
        }
        this.urlPrefix = urlPrefix;
        return this;
    }

    public GenDto setDomainName(String domainName) {
        this.domainName = StrUtil.upperFirst(domainName);
        return this;
    }

    @Override
    public GenDto setParentPackageName(String parentPackageName) {
        super.setParentPackageName(parentPackageName);
        return this;
    }

    @Override
    public GenDto setDeleteIfExists(boolean deleteIfExists) {
        super.setDeleteIfExists(deleteIfExists);
        return this;
    }

    @Override
    public GenDto setHeaderDesc(String headerDesc) {
        super.setHeaderDesc(headerDesc);
        return this;
    }

    @Override
    public GenDto setForceDelete(boolean forceDelete) {
        super.setForceDelete(forceDelete);
        return this;
    }

    @Override
    public GenDto setProjectAbsolutePath(String projectAbsolutePath) {
        super.setProjectAbsolutePath(projectAbsolutePath);
        return this;
    }

    @Override
    public GenDto setEntityPackageName(String entityPackageName) {
        super.setEntityPackageName(entityPackageName);
        return this;
    }

    @Override
    public GenDto setControllerPackageName(String controllerPackageName) {
        super.setControllerPackageName(controllerPackageName);
        return this;
    }

    @Override
    public GenDto setControllerReqPackageName(String controllerReqPackageName) {
        super.setControllerReqPackageName(controllerReqPackageName);
        return this;
    }

    @Override
    public GenDto setDtoPackageName(String dtoPackageName) {
        super.setDtoPackageName(dtoPackageName);
        return this;
    }

    @Override
    public GenDto setMapperPackageName(String mapperPackageName) {
        super.setMapperPackageName(mapperPackageName);
        return this;
    }

    @Override
    public GenDto setServiceInterfacePackageName(String serviceInterfacePackageName) {
        super.setServiceInterfacePackageName(serviceInterfacePackageName);
        return this;
    }

    @Override
    public GenDto setServiceImplPackageName(String serviceImplPackageName) {
        super.setServiceImplPackageName(serviceImplPackageName);
        return this;
    }
}
