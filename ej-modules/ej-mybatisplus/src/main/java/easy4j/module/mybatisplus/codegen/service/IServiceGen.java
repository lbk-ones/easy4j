package easy4j.module.mybatisplus.codegen.service;

import cn.hutool.core.util.StrUtil;
import easy4j.module.mybatisplus.codegen.AbstractGen;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
public class IServiceGen extends AbstractGen {

    public String getFilePath() {
        String parentPackagePath = parsePackage(this.getParentPackageName());
        return joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                , parentPackagePath
                , parsePackage(getServiceInterfacePackageName())
                , "I"+this.getDomainName() + "Service.java");
    }

    public String gen() {
        notNull(this.getEntityName(),"entityName");
        notNull(this.getServiceInterfacePackageName(),"serviceInterfacePackageName");
        String filePath = this.getFilePath();
        return loadTemplate(filePath, "temp", "IServiceGen.ftl", this);
    }


}
