package easy4j.module.mybatisplus.codegen;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.module.mybatisplus.codegen.controller.ControllerGen;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jodd.util.StringPool;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractGen extends GenDto implements CodeGen {

    public String getHeaderDesc() {
        return StrUtil.blankToDefault(headerDesc, cnDesc);
    }

    public void cnDesc(String cnDesc) {
        this.cnDesc = cnDesc;
        this.headerDesc = cnDesc + this.getClass().getSimpleName();
    }

    public final String SRC_MAIN_JAVA = "src/main/java";
    public final String SRC_MAIN_RESOURCE = "src/main/resources";

    @Override
    public void clear() {
        forceDelete = true;
    }

    public abstract String getFilePath();

    // 加载template
    public String loadTemplate(String absoluteFilePath, String templatePath, String templateName, Object params, boolean isPreview) {
        if(!isPreview){
            File file = new File(absoluteFilePath);
            if (file.exists()) {
                if (!deleteIfExists && !forceDelete) {
                    return "skip the file because the file is exists::::" + absoluteFilePath;
                } else {
                    boolean delete = file.delete();
                    if (!delete || forceDelete) {
                        if (!delete) {
                            return "the file delete error";
                        } else {
                            return "clear the file" + absoluteFilePath;
                        }
                    }
                }
            }
            File file1 = new File(file.getParent());
            if (!file1.exists()) {
                if (!file1.mkdirs()) throw new RuntimeException(file1.getPath() + " dir create error!!");
            }
        }

        Class<?> aClass = params.getClass();
        Field[] fields = ReflectUtil.getFields(aClass);
        Map<String, Object> params_ = Maps.newHashMap();
        for (Field field : fields) {
            String name = field.getName();
            Object fieldValue = ReflectUtil.getFieldValue(params, field);
            params_.putIfAbsent(name, fieldValue);
        }
        //notNull(this.getDomainName(), "domainName");
        notNull(this.getParentPackageName(), "parentPackageName");

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setTemplateLoader(new ClassTemplateLoader(ControllerGen.class, StringPool.SLASH + templatePath));

        if(isPreview){
            try (Writer writer = new StringWriter()) {
                // 加载模板
                Template template = cfg.getTemplate(templateName);
                // 渲染模板到字符流
                template.process(params_, writer);
                // 转换为字符串并返回
                return writer.toString();
            } catch (TemplateException e) {
                throw new RuntimeException("模板渲染失败：" + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("模板加载/IO 异常：" + e.getMessage(), e);
            }
        }else{
            try (Writer out = new FileWriter(absoluteFilePath)) {
                Template template = cfg.getTemplate(templateName);
                template.process(params_, out);
                out.flush();
            } catch (IOException | TemplateException e) {
                throw new RuntimeException(e);
            }
            return "gen successful 【"+StrUtil.replaceLast(templateName,"Gen.ftl","")+"】--> " + absoluteFilePath;
        }
    }

    public String parsePackage(String packageName) {
        if (StrUtil.isEmpty(packageName)) return "";
        String[] split = packageName.split("\\.");
        return String.join(File.separator, split);
    }

    public String joinPath(String... paths) {
        String[] strings = new String[paths.length];
        int i = 0;
        for (String path : paths) {
            if (path.startsWith(File.separator)) {
                strings[i] = StrUtil.replaceFirst(path, File.separator, "");
            } else {
                strings[i] = path;
            }
            i++;
        }
        return String.join(File.separator, strings);
    }

    public void notNull(Object args, String name) {
        if (ObjectUtil.isEmpty(args)) {
            throw new IllegalArgumentException("the " + name + " is not null");
        }
    }
}
