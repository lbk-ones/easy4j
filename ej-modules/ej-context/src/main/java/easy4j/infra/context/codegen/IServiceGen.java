package easy4j.infra.context.codegen;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;

@EqualsAndHashCode(callSuper = true)
@Data
public class IServiceGen extends AbstractGen {

    public String serviceInterfacePackageName = "service";

    public String getFilePath() {
        String parentPackagePath = parsePackage(this.parentPackageName);
        return joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                , parentPackagePath
                , parsePackage(serviceInterfacePackageName)
                , "I"+this.domainName + "Service.java");
    }

    public String gen() {
        if(StrUtil.isBlank(this.getEntityName())){
            return "entityName is not be null";
        }
        if(StrUtil.isBlank(this.getServiceInterfacePackageName())){
            return "serviceInterfacePackageName is not be null";
        }
        String filePath = this.getFilePath();
        return loadTemplate(filePath, "temp", "IServiceGen.ftl", this);
    }


}
