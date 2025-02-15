package easy4j.module.jpa.gen;

import easy4j.module.base.annotations.Desc;
import lombok.Getter;
import org.springframework.util.Assert;

import java.io.File;


@Getter
public class ConfigJpaGen {

    @Desc("实体扫描路径 工作路径下面的相对路径 xxx.xxx.xxx 默认 domain包")
    private String scanPackage = "domain";
    @Desc("工作路径（通常是启动类所在包）如果 通过springMainClass()方法设置了springMainClass那么这个可以不用设置")
    private String mainClassPackage;
    @Desc("java绝对路径 到 xxx/src/main 这一级")
    private String javaBaseUrl = System.getProperty("user.dir")+ File.separator+"src"+File.separator+"main";
    @Desc("模板文件 填类路径下的相对路径 默认 tmpl")
    private String classPathTmpl = "tmpl";

    @Desc("是否生成 by easy4j-gen auto generate")
    private Boolean genNote = true;

    @Desc("生成Dto的时候是否将Date转为String")
    private Boolean genDtoDateToString = true;
    @Desc("是否按模块生成Api接口文档")
    private Boolean groupControllerModule = true;

    private Class<?> springMainClass;

    public ConfigJpaGen() {
    }
    public ConfigJpaGen scanPackage(String scanPackage) {
        this.scanPackage = scanPackage;
        return this;
    }
    public ConfigJpaGen mainClassPackage(String workPath) {
        this.mainClassPackage = workPath;
        return this;
    }


    public ConfigJpaGen javaBaseUrl(String javaBaseUrl) {
        this.javaBaseUrl = javaBaseUrl;
        return this;
    }
    public ConfigJpaGen genNote(boolean genNote) {
        this.genNote = genNote;
        return this;
    }
    public ConfigJpaGen groupControllerModule(boolean groupControllerModule) {
        this.groupControllerModule = groupControllerModule;
        return this;
    }

    public ConfigJpaGen classPathTmpl(String classPathTmpl) {
        this.classPathTmpl = classPathTmpl;
        return this;
    }
    public ConfigJpaGen genDtoDateToString(Boolean genDtoDateToString) {
        this.genDtoDateToString = genDtoDateToString;
        return this;
    }
    public ConfigJpaGen springMainClass(Class<?> springMainClass) {
        Assert.notNull(springMainClass,"springMainClass must not be null");
        this.springMainClass = springMainClass;
        this.mainClassPackage = springMainClass.getPackage().getName();
        return this;
    }

}
