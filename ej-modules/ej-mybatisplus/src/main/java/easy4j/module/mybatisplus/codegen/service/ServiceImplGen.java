package easy4j.module.mybatisplus.codegen.service;

import easy4j.module.mybatisplus.codegen.AbstractGen;
import easy4j.module.mybatisplus.codegen.ObjectValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceImplGen extends AbstractGen {

    public String getFilePath() {
        String parentPackagePath = parsePackage(this.getParentPackageName());
        return joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                , parentPackagePath
                , parsePackage(getServiceImplPackageName())
                , this.getDomainName() + "ServiceImpl.java");
    }

    public String gen(boolean isPreview, boolean isServer, ObjectValue objectValue) {
        notNull(this.getEntityName(),"entityName");
        notNull(this.getServiceImplPackageName(),"iServiceImplPackageName");

        String filePath = this.getFilePath();
        return loadTemplate(filePath, "temp", "ServiceImplGen.ftl", this, false);
    }


}
