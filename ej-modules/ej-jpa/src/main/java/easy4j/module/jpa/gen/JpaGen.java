package easy4j.module.jpa.gen;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.annotations.Desc;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.utils.ListTs;
import easy4j.module.jpa.Comment;
import easy4j.module.jpa.base.BaseDto;
import easy4j.module.jpa.base.BaseService;
import easy4j.module.jpa.gen.domain.*;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jodd.util.StringPool;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 与 jpa 和 easy4j 深度耦合 根据 jpa entity 生成 service、ServiceImpl、dto、Cotnroller
 * @author likunkun
 * @create 2018-06-06 15:07
 */
@Desc("与 jpa 和 easy4j 深度耦合 根据 jpa entity 生成 service、ServiceImpl、dto、Cotnroller 代码 生成完成之后可以进行简单的增删改查")
public class JpaGen {

    /**
     * 扫描dao的实体类
     * 生成 dto dao层接口 业务接口 业务实现接口
     * @param configJpaGen
     * @throws Exception
     */
    public static void Gen(ConfigJpaGen configJpaGen) throws Exception{
        String baseUrl = configJpaGen.getJavaBaseUrl();
        String workPath = configJpaGen.getMainClassPackage();
        String classPathTmpl = configJpaGen.getClassPathTmpl();
        String resourceBaseUrl = baseUrl + File.separator + "resources";
        ClassPathResource classPathResource = new ClassPathResource(classPathTmpl);

        URL url = classPathResource.getURL();
        File file = new File(url.getPath());

//        String realTmplPath = resourceBaseUrl+File.separator + classPathTmpl;
//        checkDir(realTmplPath, "");
        String scanpackage = StrUtil.blankToDefault(configJpaGen.getScanPackage(),"domain");
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setTemplateLoader(new ClassTemplateLoader(JpaGen.class, StringPool.SLASH+classPathTmpl));
        //cfg.setDirectoryForTemplateLoading(file);
        String domainBasePackage = workPath+"."+scanpackage;
        String interfacePackageName = workPath+".service";
        String interfaceImplPackageName = workPath+".service.impl";
        String daoPackageName = workPath+".dao";
        String dtoPackageName = workPath+".dto";
        String controllerPackageName = workPath+".controller";
        List<Class<?>> classes = scanClasses(domainBasePackage);

        if(CollUtil.isEmpty(classes)){
            throw new EasyException(domainBasePackage+"下没有找到需要生成的Entity实体");
        }


        String javaBaseUrl = baseUrl+ File.separator+"java";
        String s = checkDir(javaBaseUrl, interfacePackageName);
        String s2 = checkDir(javaBaseUrl, interfaceImplPackageName);
        String s3 = checkDir(javaBaseUrl, daoPackageName);
        String s4 = checkDir(javaBaseUrl, dtoPackageName);
        String s5 = checkDir(javaBaseUrl, controllerPackageName);



        // gen interface
        genInterface(cfg, configJpaGen,classes, javaBaseUrl, interfacePackageName, s);

        // gen impl
        genImpl(cfg, configJpaGen, classes, javaBaseUrl, interfaceImplPackageName, interfacePackageName, s2);

        // gen dao
        genDao(cfg, classes, javaBaseUrl, daoPackageName, s3);

        // gen dto
        genDto(cfg, configJpaGen, classes, javaBaseUrl, dtoPackageName, s4);

        // gen controller
        genController(cfg, classes, javaBaseUrl, controllerPackageName, workPath, s5);

    }

    private static void genController(Configuration cfg, List<Class<?>> classes, String javaBaseUrl, String controllerPackageName, String workPath, String s5) throws IOException, TemplateException {
        Template template3 = cfg.getTemplate("controller.ftl");
        for (Class<?> aClass : classes) {
            String p = aClass.getName();
            String simpleName = aClass.getSimpleName();
            GenJpaController genInterface = new GenJpaController();
            genInterface.setBaseUrl(javaBaseUrl);
            genInterface.setPackageName(controllerPackageName);
            genInterface.setFirstLowDomainName(StrUtil.lowerFirst(simpleName));
            genInterface.setDomainName(simpleName);
            genInterface.setControllerName(simpleName+"Controller");

            List<String> importList = ListTs.newArrayList();
            importList.add(workPath +StringPool.DOT+"dto"+StringPool.DOT+simpleName+"Dto");
            importList.add(workPath +StringPool.DOT+"service"+StringPool.DOT+"I"+simpleName+"Service");
            genInterface.setImportList(importList);


            genInterface.setLineList(
                    ListTs.asList(
                            "/**",
                            " * by easy4j-gen auto generate",
                            " */"
                    ));

            String s1 = s5 + File.separator + genInterface.getControllerName() + ".java";
            File file = new File(s1);
            if (file.exists()) {
                continue;
            }
            System.out.println("gen controller:"+s1);
            try (Writer out = new FileWriter(s1)) {
                template3.process(genInterface, out);
            }
        }
    }

    private static void genInterface(Configuration cfg, ConfigJpaGen configJpaGen, List<Class<?>> classes, String javaBaseUrl, String interfacePackageName, String s) throws IOException, TemplateException {
        String workPath = configJpaGen.getMainClassPackage();
        Template template = cfg.getTemplate("iservice.ftl");
        for (Class<?> aClass : classes) {
            String p = aClass.getName();
            String simpleName = aClass.getSimpleName();
            GenInterface genInterface = new GenInterface();
            genInterface.setBaseUrl(javaBaseUrl);
            genInterface.setPackageName(interfacePackageName);
            genInterface.setInterfaceName("I" + simpleName + "Service");
            genInterface.setDomainName(simpleName);
            genInterface.setFirstLowDomainName(StrUtil.lowerFirst(simpleName));

            List<String> importList = ListTs.newArrayList();
            importList.add(workPath +StringPool.DOT+"dto"+StringPool.DOT+simpleName+"Dto");
            genInterface.setImportList(importList);

            genInterface.setLineList(
                    ListTs.asList(
                        "/**",
                        " * by easy4j-gen auto generate",
                        " */"
            ));

            String s1 = s + File.separator + genInterface.getInterfaceName() + ".java";
            File file = new File(s1);
            if (file.exists()) {
                continue;
            }
            System.out.println("gen interface:"+s1);
            try (Writer out = new FileWriter(s1)) {
                template.process(genInterface, out);
            }
        }
    }

    private static void genImpl(Configuration cfg, ConfigJpaGen configJpaGen, List<Class<?>> classes, String javaBaseUrl, String interfaceImplPackageName, String interfacePackageName, String s2) throws IOException, TemplateException {
        String workPath = configJpaGen.getMainClassPackage();
        Template template2 = cfg.getTemplate("iserviceimpl.ftl");
        for (Class<?> aClass : classes) {
            String p = aClass.getName();
            String simpleName = aClass.getSimpleName();
            GenInterfaceImpl genInterface = new GenInterfaceImpl();
            genInterface.setBaseUrl(javaBaseUrl);
            genInterface.setPackageName(interfaceImplPackageName);
            genInterface.setInterfaceName("I" + simpleName + "Service");
            genInterface.setInterfaceImplName("I" + simpleName + "ServiceImpl");
            genInterface.setDomainName(simpleName);
            genInterface.setFirstLowDomainName(StrUtil.lowerFirst(simpleName));

            String importInterface = interfacePackageName +"."+genInterface.getInterfaceName();



            genInterface.setLineList(
                    ListTs.asList(
                            "/**",
                            " * by easy4j-gen auto generate",
                            " */"
                    ));

            List<String> annotationList = ListTs.newArrayList();
            annotationList.add(Service.class.getSimpleName());
            genInterface.setAnnotationList(annotationList);

            List<String> importList = ListTs.newArrayList();
            importList.add(Service.class.getName());
//            importList.add(importInterface);
            importList.add(BaseService.class.getName());


            importList.add(workPath +StringPool.DOT+"dto"+StringPool.DOT+simpleName+"Dto");
            importList.add(workPath +StringPool.DOT+"service"+StringPool.DOT+"I"+simpleName+"Service");
            importList.add(workPath +StringPool.DOT+"domain"+StringPool.DOT+simpleName);
            importList.add(workPath +StringPool.DOT+"dao"+StringPool.DOT+simpleName+"Dao");

            genInterface.setImportList(importList);
            String s1 = s2 + File.separator + genInterface.getInterfaceImplName() + ".java";
            File file = new File(s1);
            if (file.exists()) {
                continue;
            }
            System.out.println("gen impl:"+s1);

            try (Writer out = new FileWriter(s1)) {
                template2.process(genInterface, out);
            }
        }
    }

    private static void genDao(Configuration cfg, List<Class<?>> classes, String javaBaseUrl, String daoPackageName, String s3) throws IOException, TemplateException {
        Template template3 = cfg.getTemplate("dao.ftl");
        for (Class<?> aClass : classes) {
            String p = aClass.getName();
            String simpleName = aClass.getSimpleName();
            GenJpaDao genInterface = new GenJpaDao();
            genInterface.setBaseUrl(javaBaseUrl);
            genInterface.setPackageName(daoPackageName);
            genInterface.setDaoClassName( simpleName + "Dao");
            genInterface.setDomainName(simpleName);


            List<String> importList = ListTs.newArrayList();
            importList.add(Repository.class.getName());
            importList.add(aClass.getName());
            importList.add(JpaRepository.class.getName());
            importList.add(JpaSpecificationExecutor.class.getName());

            List<String> annotationList = ListTs.newArrayList();
            annotationList.add(Repository.class.getSimpleName());
            genInterface.setAnnotationList(annotationList);

            genInterface.setImportList(importList);

            genInterface.setLineList(
                    ListTs.asList(
                            "/**",
                            " * by easy4j-gen auto generate",
                            " */"
                    ));

            String s1 = s3 + File.separator + genInterface.getDaoClassName() + ".java";
            File file = new File(s1);
            if (file.exists()) {
                continue;
            }
            System.out.println("gen dao:"+s1);
            try (Writer out = new FileWriter(s1)) {
                template3.process(genInterface, out);
            }
        }
    }

    private static void genDto(Configuration cfg, ConfigJpaGen configJpaGen, List<Class<?>> classes, String javaBaseUrl, String dtoPackageName, String s4) throws IOException, TemplateException {
        Template template4 = cfg.getTemplate("dto.ftl");
        List<String> collect = Arrays.stream(ReflectUtil.getFields(BaseDto.class)).map(Field::getName).distinct().collect(Collectors.toList());
        for (Class<?> aClass : classes) {
            Field[] fields = ReflectUtil.getFields(aClass);
            Set<String> fieldImportList = new HashSet<>();
            List<GenField> fieldList = ListTs.newArrayList();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                String name1 = field.getName();
                if (collect.contains(name1)) {
                    continue;
                }
                GenField genField = new GenField();
                genField.setName(name1);
                Class<?> type = field.getType();

                // 处理泛型对象
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType || genericType instanceof GenericArrayType) {
                    String typeName = getTypeName(genericType, fieldImportList);
                    genField.setType(typeName);
                }else{
                    checkIsNeedImport(type.getTypeName(),fieldImportList);
                    String simpleName = getSimpleName(configJpaGen, type);
                    genField.setType(simpleName);
                }

                Comment annotation = field.getAnnotation(Comment.class);
                if(Objects.nonNull(annotation)){
                    String value = annotation.value();
                    genField.setFieldLine(ListTs.asList(
                            "// "+value
                    ));
                }
                fieldList.add(genField);
            }
            String p = aClass.getName();
            String simpleName = aClass.getSimpleName();
            GenDto genInterface = new GenDto();
            genInterface.setBaseUrl(javaBaseUrl);
            genInterface.setPackageName(dtoPackageName);
            genInterface.setDtoClassName( simpleName + "Dto");
            genInterface.setFieldList(fieldList);
            genInterface.setLineList(
                    ListTs.asList(
                            "/**",
                            " * by easy4j-gen auto generate",
                            " */"
                    ));
            fieldImportList.add(BaseDto.class.getName());
            fieldImportList.add(Data.class.getName());
            fieldImportList.add(EasyException.class.getName());
            fieldImportList.add(EqualsAndHashCode.class.getName());
            genInterface.setImportList(
                    ListTs.newArrayList(fieldImportList.iterator())
            );

            genInterface.setAnnotationList(ListTs.asList(
                    Data.class.getSimpleName(),
                    EqualsAndHashCode.class.getSimpleName()+"(callSuper = true)"
            ));

            String s1 = s4 + File.separator + genInterface.getDtoClassName() + ".java";
            File file = new File(s1);
            if (file.exists()) {
                continue;
            }
            System.out.println("gen dto:"+s1);
            try (Writer out = new FileWriter(s1)) {
                template4.process(genInterface, out);
            }
        }
    }

    private final static Pattern pattern = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$]*(?:\\.[a-zA-Z_$][a-zA-Z0-9_$]*)*$");

    private static String getTypeName(Type type, Set<String> fieldImportList) {
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
    private static void checkIsNeedImport(String name, Set<String> fieldImportList) {
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
    public static String getSimpleName(ConfigJpaGen configJpaGen, Class<?> typeClass){
        String simpleName = typeClass.getSimpleName();
        Boolean genDtoDateToString = configJpaGen.getGenDtoDateToString();
        if(genDtoDateToString && "Date".equals(simpleName)){
            return "String";
        }
        return simpleName;
    }

    public static String checkDir(String baseUrl, String packageName) {
        String[] split = packageName.split("\\.");
        String join = String.join(File.separator, split);
        baseUrl += File.separator + join;
        File file = new File(baseUrl);
        if (!file.exists()) {
            boolean mkdir = file.mkdir();
            if (!mkdir) {
                throw new RuntimeException("mkdir fail");
            }
        }
        return baseUrl;

    }

    public static List<Class<?>> scanClasses(String basePackage) throws IOException, ClassNotFoundException {
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



}
