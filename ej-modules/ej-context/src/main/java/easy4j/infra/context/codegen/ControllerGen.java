package easy4j.infra.context.codegen;

import cn.hutool.core.bean.BeanUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.File;


@EqualsAndHashCode(callSuper = true)
@Data
public class ControllerGen extends AbstractGen {



    public String getFilePath(){
        String[] split = this.parentPackageName.split("\\.");
        String collect = String.join(File.separator, split);
        return this.getProjectAbsolutePath()+"/src/main/java"+File.separator+ collect+File.separator+"controller"+File.separator+this.domainName+"Controller.java";
    }

    @Override
    public String gen() {
        String filePath = this.getFilePath();
        return loadTemplate(filePath, "temp","ControllerGen.ftl", this);
    }




}
