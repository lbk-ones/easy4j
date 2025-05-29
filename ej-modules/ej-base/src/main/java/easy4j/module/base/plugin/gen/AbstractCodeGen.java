/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.base.plugin.gen;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.utils.ListTs;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jodd.util.StringPool;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AbstractCodeGen
 *
 * @author bokun.li
 * @date 2025-05
 */
@Setter
@Getter
public abstract class AbstractCodeGen<T extends BaseConfigCodeGen> implements CodeGen {

    T configGen;

    protected Configuration configuration;


    protected abstract void genTemplate() throws Exception;

    public void gen() throws Exception{
        checkConfigGen();
        configuration = loadTemplate();
        genTemplate();
    }

    private void checkConfigGen() {

        if (configGen == null) {
            throw new RuntimeException("config is null");
        }
        if (configGen.getNoCheck()) {
            return;
        }

        String outAbsoluteUrl = configGen.getOutAbsoluteUrl();
        if (StrUtil.isBlank(outAbsoluteUrl)) {
            throw new RuntimeException("outAbsoluteUrl is null");
        }
        String tmplClassPath = configGen.getTmplClassPath();
        if (StrUtil.isBlank(tmplClassPath)) {
            throw new RuntimeException("tmplClassPath is null");
        }
        if (StrUtil.isBlank(configGen.getInputAbsoluteUrl())) {
            throw new RuntimeException("inputAbsoluteUrl is null");
        }

        if (StrUtil.isBlank(configGen.getOutPackageName())) {
            throw new RuntimeException("outPackageName is null");
        }

    }

    ;



    protected String checkDirReSolvePackageName(String baseUrl, String packageName) {
        String[] split = packageName.split("\\.");
        String join = String.join(File.separator, split);
        File file = new File(baseUrl,join);
        if (!file.exists()) {
            boolean mkdir = file.mkdir();
            if (!mkdir) {
                throw new RuntimeException("mkdir fail");
            }
        }
        return baseUrl;

    }

    // 加载template
    private Configuration loadTemplate(){
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setTemplateLoader(new ClassTemplateLoader(this.getClass(), StringPool.SLASH+configGen.getTmplClassPath()));
        return cfg;
    }

    protected Template getTemplate(String templateName){
        try {
            return configuration.getTemplate(templateName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 写入文件
    protected void writeFile(Template template,Object params, String absoluteFilePath){
        try (Writer out = new FileWriter(absoluteFilePath)) {
            template.process(params, out);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }


    protected List<Class<?>> scanClasses(String basePackage) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        // 创建资源模式解析器
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        // 创建元数据读取工厂
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        // 构建类路径资源模式
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                basePackage.replace('.', '/') + "/**/*.class";
        // 获取指定包下的所有类资源
        Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                // 创建元数据读取器
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                // 获取类名
                String className = metadataReader.getClassMetadata().getClassName();
                // 加载类
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    private final static Pattern pattern = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$]*(?:\\.[a-zA-Z_$][a-zA-Z0-9_$]*)*$");

    protected String getTypeName(Type type, Set<String> fieldImportList) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            // 获取原始类型，如 List
            Type rawType = parameterizedType.getRawType();
            // 获取泛型类型参数
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            StringBuilder sb = new StringBuilder();
            String typeName1 = rawType.getTypeName();
            String lastDotName = getLastDotName(typeName1);
            checkIsNeedImport(typeName1, fieldImportList);
            sb.append(lastDotName);
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                Type actualTypeArgument = actualTypeArguments[i];
                checkIsNeedImport(actualTypeArgument.getTypeName(), fieldImportList);
                String typeName = getTypeName(actualTypeArgument, fieldImportList);
                // 递归处理泛型参数
                sb.append(getLastDotName(typeName));
            }
            sb.append(">");
            return sb.toString();
        }else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            // 递归处理数组元素的类型
            String componentTypeName = getTypeName(arrayType.getGenericComponentType(),fieldImportList);
            return componentTypeName + "[]";
        }else {
            String typeName = type.getTypeName();
            checkIsNeedImport(typeName, fieldImportList);
            return getLastDotName(typeName);
        }
    }

    protected void checkIsNeedImport(String name, Set<String> fieldImportList) {
        boolean contains = ListTs.asList(
                "int",
                "byte",
                "short",
                "long",
                "boolean",
                "float",
                "double",
                "char"
        ).contains(name);

        if(!StrUtil.startWith(name,"java.lang") && !contains){

            Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                fieldImportList.add(name);
            }
        }
    }

    public static String getLastDotName(String name){
        List<String> list = ListTs.asList(name.split("\\."));
        if (list.size()>1) {
            return list.get(list.size()-1);
        }else{
            return name;
        }
    }

    public static String resolveUrl(String baseUrl,String suffixUrl){
        File file = new File(baseUrl, suffixUrl);
        return file.getAbsolutePath();
    }

    public static String combineUrlAndPackageName(String url,String packageName){
        String[] split = packageName.split("\\.");
        String join = String.join("/", split);
        return resolveUrl(url, join);


    }

}
