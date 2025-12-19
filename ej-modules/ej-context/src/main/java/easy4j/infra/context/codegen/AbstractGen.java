package easy4j.infra.context.codegen;

import cn.hutool.core.util.StrUtil;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jodd.util.StringPool;
import lombok.Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
@Data
public abstract class AbstractGen implements CodeGen{

    // 实体类名称 驼峰 帕斯卡命名发
    String domainName;

    // 中文描述
    String cnDesc;

    // 父包名称
    String parentPackageName;

    // 项目所在绝对路径
    String projectAbsolutePath;

    // 返回的dto名称
    String returnDtoName;

    // url前缀
    String urlPrefix;

    // 数据库实体类名
    String entityName;

    // 存在则删除
    boolean deleteIfExists;

    // 类文件头注释
    String headerDesc = "no desc";

    // 作者
    String author = "bokun.li";

    // 只删除
    boolean forceDelete = false;

    public String getHeaderDesc() {
        return StrUtil.blankToDefault(headerDesc,cnDesc);
    }

    public void setCnDesc(String cnDesc) {
        this.cnDesc = cnDesc;
        this.headerDesc = cnDesc + this.getClass().getSimpleName();
    }

    public final String SRC_MAIN_JAVA = "src/main/java";

    @Override
    public void clear() {
        forceDelete = true;
    }

    public abstract String getFilePath();

    // 加载template
    public String loadTemplate(String absoluteFilePath, String templatePath, String templateName, Object params) {
        File file = new File(absoluteFilePath);
        if (file.exists()) {
            if (!deleteIfExists && !forceDelete) {
                return "skip the file because the file is exists::::"+absoluteFilePath;
            }else{
                boolean delete = file.delete();
                if(!delete || forceDelete){
                    if(!delete){
                        return "the file delete error";
                    }else{
                        return "clear the file"+absoluteFilePath;
                    }
                }
            }
        }

        if(StrUtil.isBlank(this.getDomainName())){
            return "domainName is not be null";
        }

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setTemplateLoader(new ClassTemplateLoader(ControllerGen.class, StringPool.SLASH + templatePath));
        try {
            Template template = cfg.getTemplate(templateName);
            try (Writer out = new FileWriter(absoluteFilePath)) {
                template.process(params, out);
            } catch (IOException | TemplateException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "gen successful --> "+absoluteFilePath;
    }

    public String parsePackage(String packageName){
        if(StrUtil.isEmpty(packageName)) return "";
        String[] split = packageName.split("\\.");
        return String.join(File.separator, split);
    }

    public String joinPath(String... paths){
        String[] strings = new String[paths.length];
        int i = 0;
        for (String path : paths) {
            if (path.startsWith(File.separator)) {
                strings[i] =  StrUtil.replaceFirst(path,File.separator,"");
            }else{
                strings[i] =  path;
            }
            i++;
        }
       return String.join(File.separator,strings);
    }
}
