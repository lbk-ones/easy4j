package easy4j.module.mybatisplus.codegen.controller;

import easy4j.module.mybatisplus.codegen.AbstractGen;
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
    public String gen(boolean isPreview, boolean isServer) {
        notNull(this.getDomainName(),"domainName");
        String filePath = this.getFilePath();
        return loadTemplate(filePath, "temp","ControllerGen.ftl", this, isPreview);
    }




}
