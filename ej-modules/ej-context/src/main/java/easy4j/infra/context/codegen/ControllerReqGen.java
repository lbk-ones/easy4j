package easy4j.infra.context.codegen;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jodd.util.StringPool;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;

@EqualsAndHashCode(callSuper = true)
@Data
public class ControllerReqGen extends AbstractGen{

    public String getFilePath(){
        String[] split = this.parentPackageName.split("\\.");
        String collect = String.join(File.separator, split);
        return this.getProjectAbsolutePath()+"/src/main/java"+File.separator+ collect+File.separator+"controller/req"+File.separator+this.domainName+"ControllerReq.java";
    }

    public String gen(){
        String filePath = this.getFilePath();
        return loadTemplate(filePath, "temp","ControllerReqGen.ftl", this);
    }


}
