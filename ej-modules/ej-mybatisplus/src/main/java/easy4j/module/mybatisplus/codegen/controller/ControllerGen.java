package easy4j.module.mybatisplus.codegen.controller;

import easy4j.module.mybatisplus.codegen.AbstractGen;
import easy4j.module.mybatisplus.codegen.ObjectValue;
import easy4j.module.mybatisplus.codegen.servlet.PreviewRes;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;

@EqualsAndHashCode(callSuper = true)
@Data
public class ControllerGen extends AbstractGen {



    public String getFilePath(){
        String[] split = this.getParentPackageName().split("\\.");
        String collect = String.join(File.separator, split);
        return this.getProjectAbsolutePath()+"/src/main/java"+File.separator+ collect+File.separator+getControllerPackageName()+File.separator+this.getDomainName()+"Controller.java";
    }

    @Override
    public String gen(boolean isPreview, boolean isServer, ObjectValue objectValue) {
        notNull(this.getDomainName(),"domainName");
        String filePath = this.getFilePath();
        String res = loadTemplate(filePath, "temp", "ControllerGen.ftl", this, isPreview);
        PreviewRes previewRes = new PreviewRes();
        PreviewRes.PInfo pInfo = new PreviewRes.PInfo("Controller");
        String fName = this.getDomainName() + "Controller.java";
        pInfo.add(fName,res);
        previewRes.add(pInfo);
        objectValue.setObject(previewRes);
        return res;
    }




}
