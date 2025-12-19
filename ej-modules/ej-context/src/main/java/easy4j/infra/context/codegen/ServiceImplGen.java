package easy4j.infra.context.codegen;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceImplGen extends AbstractGen {

    public String serviceImplPackageName = "service.impl";

    public String getFilePath() {
        String parentPackagePath = parsePackage(this.parentPackageName);
        return joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                , parentPackagePath
                , parsePackage(serviceImplPackageName)
                , this.domainName + "ServiceImpl.java");
    }

    public String gen() {
        if(StrUtil.isBlank(this.getEntityName())){
            return "entityName is not be null";
        }
        if(StrUtil.isBlank(this.getServiceImplPackageName())){
            return "iServiceImplPackageName is not be null";
        }

        String filePath = this.getFilePath();
        return loadTemplate(filePath, "temp", "ServiceImplGen.ftl", this);
    }


}
