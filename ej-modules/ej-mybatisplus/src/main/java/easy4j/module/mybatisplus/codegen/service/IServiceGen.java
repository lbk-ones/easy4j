package easy4j.module.mybatisplus.codegen.service;

import easy4j.module.mybatisplus.codegen.AbstractGen;
import easy4j.module.mybatisplus.codegen.ObjectValue;
import easy4j.module.mybatisplus.codegen.servlet.PreviewRes;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    public String gen(boolean isPreview, boolean isServer, ObjectValue objectValue) {
        notNull(this.getEntityName(),"entityName");
        notNull(this.getServiceInterfacePackageName(),"serviceInterfacePackageName");
        String filePath = this.getFilePath();
        String res = loadTemplate(filePath, "temp", "IServiceGen.ftl", this, isPreview);
        PreviewRes previewRes = new PreviewRes();
        PreviewRes.PInfo pInfo = new PreviewRes.PInfo("Service");
        String fName = "I"+this.getDomainName() + "Service.java";
        pInfo.add(fName,res);
        previewRes.add(pInfo);
        objectValue.setObject(previewRes);
        return res;
    }


}
