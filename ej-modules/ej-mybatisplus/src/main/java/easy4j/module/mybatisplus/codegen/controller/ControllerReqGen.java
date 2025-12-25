package easy4j.module.mybatisplus.codegen.controller;

import easy4j.module.mybatisplus.codegen.AbstractGen;
import easy4j.module.mybatisplus.codegen.ObjectValue;
import easy4j.module.mybatisplus.codegen.servlet.PreviewRes;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;

@EqualsAndHashCode(callSuper = true)
@Data
public class ControllerReqGen extends AbstractGen {

    public String getFilePath(){
        String[] split = this.getParentPackageName().split("\\.");
        String collect = String.join(File.separator, split);
        return this.getProjectAbsolutePath()+"/src/main/java"+File.separator+ collect+File.separator+parsePackage(getControllerReqPackageName())+File.separator+this.getDomainName()+"ControllerReq.java";
    }

    public String gen(boolean isPreview, boolean isServer, ObjectValue objectValue){
        notNull(this.getDomainName(),"domainName");
        String filePath = this.getFilePath();
        String res = loadTemplate(filePath, "temp", "ControllerReqGen.ftl", this, isPreview);
        PreviewRes previewRes = new PreviewRes();
        PreviewRes.PInfo pInfo = new PreviewRes.PInfo("ControllerReq");
        String fName = this.getDomainName() + "ControllerReq.java";
        pInfo.add(fName,res);
        previewRes.add(pInfo);
        objectValue.setObject(previewRes);
        return res;
    }


}
