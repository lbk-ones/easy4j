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
package easy4j.module.jpa.gen;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.context.api.gen.AbstractCodeGen;
import easy4j.infra.knife4j.ControllerModule;
import easy4j.module.jpa.gen.domain.GenField;
import easy4j.infra.common.utils.ListTs;
import easy4j.module.jpa.Comment;
import easy4j.module.jpa.base.BaseDto;
import easy4j.module.jpa.base.BaseService;
import easy4j.module.jpa.gen.annotations.GenDomainName;
import easy4j.module.jpa.gen.domain.*;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jodd.util.StringPool;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.io.ClassPathResource;
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
import java.util.stream.Collectors;

/**
 * 与 jpa 和 easy4j 深度耦合 根据 jpa entity 生成 service、ServiceImpl、dto、Cotnroller
 *
 * @author likunkun
 * @create 2018-06-06 15:07
 */
@Desc("与 jpa 和 easy4j 深度耦合 根据 jpa entity 生成 service、ServiceImpl、dto、Cotnroller 代码 生成完成之后可以进行简单的增删改查")
public class JpaGen extends AbstractCodeGen<ConfigJpaGen> {

    private JpaGen() {

    }

    public static JpaGen build(ConfigJpaGen configJpaGen) {
        JpaGen jpaGen = new JpaGen();
        jpaGen.setConfigGen(configJpaGen);
        return jpaGen;
    }

    /**
     * 扫描dao的实体类
     * 生成 dto dao层接口 业务接口 业务实现接口
     *
     * @throws Exception
     */
    protected void genTemplate() throws Exception {
        ConfigJpaGen configJpaGen = this.getConfigGen();
        String baseUrl = configJpaGen.getJavaBaseUrl();
        String workPath = configJpaGen.getMainClassPackage();
        String classPathTmpl = configJpaGen.getTmplClassPath();
        String resourceBaseUrl = baseUrl + File.separator + "resources";
        ClassPathResource classPathResource = new ClassPathResource(classPathTmpl);

        URL url = classPathResource.getURL();
        File file = new File(url.getPath());

//        String realTmplPath = resourceBaseUrl+File.separator + classPathTmpl;
//        checkDir(realTmplPath, "");
        String scanpackage = StrUtil.blankToDefault(configJpaGen.getScanPackageName(), "domain");
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setTemplateLoader(new ClassTemplateLoader(JpaGen.class, StringPool.SLASH + classPathTmpl));
        //cfg.setDirectoryForTemplateLoading(file);
        String domainBasePackage = workPath + "." + scanpackage;
        String interfacePackageName = workPath + ".service";
        String interfaceImplPackageName = workPath + ".service.impl";
        String daoPackageName = workPath + ".dao";
        String dtoPackageName = workPath + ".dto";
        String controllerPackageName = workPath + ".controller";
        List<Class<?>> classes = scanClasses(domainBasePackage);

        if (CollUtil.isEmpty(classes)) {
            throw new EasyException(domainBasePackage + "下没有找到需要生成的Entity实体");
        }


        String javaBaseUrl = baseUrl + File.separator + "java";
        String s = checkDirReSolvePackageName(javaBaseUrl, interfacePackageName);
        String s2 = checkDirReSolvePackageName(javaBaseUrl, interfaceImplPackageName);
        String s3 = checkDirReSolvePackageName(javaBaseUrl, daoPackageName);
        String s4 = checkDirReSolvePackageName(javaBaseUrl, dtoPackageName);
        String s5 = checkDirReSolvePackageName(javaBaseUrl, controllerPackageName);


        // gen interface
        genInterface(cfg, configJpaGen, classes, javaBaseUrl, interfacePackageName, s);

        // gen impl
        genImpl(cfg, configJpaGen, classes, javaBaseUrl, interfaceImplPackageName, interfacePackageName, s2);

        // gen dao
        genDao(cfg, configJpaGen, classes, javaBaseUrl, daoPackageName, s3);

        // gen dto
        genDto(cfg, configJpaGen, classes, javaBaseUrl, dtoPackageName, s4);

        // gen controller
        genController(cfg, configJpaGen, classes, javaBaseUrl, controllerPackageName, workPath, s5);

    }

    private void genController(Configuration cfg, ConfigJpaGen configJpaGen, List<Class<?>> classes, String javaBaseUrl, String controllerPackageName, String workPath, String s5) throws IOException, TemplateException {
        Template template3 = cfg.getTemplate("controller.ftl");
        for (Class<?> aClass : classes) {
            String p = aClass.getName();
            String simpleName = aClass.getSimpleName();
            GenJpaController genInterface = new GenJpaController();
            genInterface.setBaseUrl(javaBaseUrl);
            genInterface.setPackageName(controllerPackageName);
            String s = StrUtil.lowerFirst(simpleName);
            genInterface.setFirstLowDomainName(s);
            genInterface.setDomainName(simpleName);
            GenDomainName annotation = aClass.getAnnotation(GenDomainName.class);
            String aValue = null;
            List<String> importList = ListTs.newArrayList();
            if (null != annotation) {
                aValue = annotation.value();
            }
            List<String> objects = ListTs.newArrayList();
            if (StrUtil.isBlank(aValue)) {
                aValue = simpleName;
            }
            importList.add(Tag.class.getName());
            objects.add(Tag.class.getSimpleName() + "(name=\"" + aValue + "\")");
            if (configJpaGen.getGroupControllerModule()) {
                objects.add(ControllerModule.class.getSimpleName() + "(name=\"" + s + "\",description=\"" + aValue + "\")");
                importList.add(ControllerModule.class.getName());
            }
            genInterface.setAnnotationList(objects);
            genInterface.setGenDomainName(aValue);
            genInterface.setControllerName(simpleName + "Controller");

            importList.add(workPath + StringPool.DOT + "dto" + StringPool.DOT + simpleName + "Dto");
            importList.add(workPath + StringPool.DOT + "service" + StringPool.DOT + "I" + simpleName + "Service");
            genInterface.setImportList(importList);


            if (configJpaGen.getGenNote()) {
                genInterface.setLineList(
                        ListTs.asList(
                                "/**",
                                " * by easy4j-gen auto generate",
                                " */"
                        ));
            }


            String s1 = combineUrlAndPackageName(s5, controllerPackageName) + File.separator + genInterface.getControllerName() + ".java";
            File file = new File(s1);
            if (file.exists()) {
                continue;
            }
            System.out.println("gen controller:" + s1);
            try (Writer out = new FileWriter(s1)) {
                template3.process(genInterface, out);
            }
        }
    }

    private void genInterface(Configuration cfg, ConfigJpaGen configJpaGen, List<Class<?>> classes, String javaBaseUrl, String interfacePackageName, String s) throws IOException, TemplateException {
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
            importList.add(workPath + StringPool.DOT + "dto" + StringPool.DOT + simpleName + "Dto");
            genInterface.setImportList(importList);

            if (configJpaGen.getGenNote()) {
                genInterface.setLineList(
                        ListTs.asList(
                                "/**",
                                " * by easy4j-gen auto generate",
                                " */"
                        ));
            }

            String s1 = combineUrlAndPackageName(s, interfacePackageName) + File.separator + genInterface.getInterfaceName() + ".java";
            File file = new File(s1);
            if (file.exists()) {
                continue;
            }
            System.out.println("gen interface:" + s1);
            try (Writer out = new FileWriter(s1)) {
                template.process(genInterface, out);
            }
        }
    }

    private void genImpl(Configuration cfg, ConfigJpaGen configJpaGen, List<Class<?>> classes, String javaBaseUrl, String interfaceImplPackageName, String interfacePackageName, String s2) throws IOException, TemplateException {
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

            String importInterface = interfacePackageName + "." + genInterface.getInterfaceName();


            if (configJpaGen.getGenNote()) {
                genInterface.setLineList(
                        ListTs.asList(
                                "/**",
                                " * by easy4j-gen auto generate",
                                " */"
                        ));
            }


            List<String> annotationList = ListTs.newArrayList();
            annotationList.add(Service.class.getSimpleName());
            genInterface.setAnnotationList(annotationList);

            List<String> importList = ListTs.newArrayList();
            importList.add(Service.class.getName());
//            importList.add(importInterface);
            importList.add(BaseService.class.getName());


            importList.add(workPath + StringPool.DOT + "dto" + StringPool.DOT + simpleName + "Dto");
            importList.add(workPath + StringPool.DOT + "service" + StringPool.DOT + "I" + simpleName + "Service");
            importList.add(workPath + StringPool.DOT + "domain" + StringPool.DOT + simpleName);
            importList.add(workPath + StringPool.DOT + "dao" + StringPool.DOT + simpleName + "Dao");

            genInterface.setImportList(importList);
            String s1 = combineUrlAndPackageName(s2, interfaceImplPackageName) + File.separator + genInterface.getInterfaceImplName() + ".java";
            File file = new File(s1);
            if (file.exists()) {
                continue;
            }
            System.out.println("gen impl:" + s1);

            try (Writer out = new FileWriter(s1)) {
                template2.process(genInterface, out);
            }
        }
    }

    private void genDao(Configuration cfg, ConfigJpaGen configJpaGen, List<Class<?>> classes, String javaBaseUrl, String daoPackageName, String s3) throws IOException, TemplateException {
        Template template3 = cfg.getTemplate("dao.ftl");
        for (Class<?> aClass : classes) {
            String p = aClass.getName();
            String simpleName = aClass.getSimpleName();
            GenJpaDao genInterface = new GenJpaDao();
            genInterface.setBaseUrl(javaBaseUrl);
            genInterface.setPackageName(daoPackageName);
            genInterface.setDaoClassName(simpleName + "Dao");
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

            if (configJpaGen.getGenNote()) {
                genInterface.setLineList(
                        ListTs.asList(
                                "/**",
                                " * by easy4j-gen auto generate",
                                " */"
                        ));
            }

            String s1 = combineUrlAndPackageName(s3, daoPackageName) + File.separator + genInterface.getDaoClassName() + ".java";
            File file = new File(s1);
            if (file.exists()) {
                continue;
            }
            System.out.println("gen dao:" + s1);
            try (Writer out = new FileWriter(s1)) {
                template3.process(genInterface, out);
            }
        }
    }

    private void genDto(Configuration cfg, ConfigJpaGen configJpaGen, List<Class<?>> classes, String javaBaseUrl, String dtoPackageName, String s4) throws IOException, TemplateException {
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
                } else {
                    checkIsNeedImport(type.getTypeName(), fieldImportList);
                    String simpleName = getSimpleName(configJpaGen, type);
                    genField.setType(simpleName);
                }

                Comment annotation = field.getAnnotation(Comment.class);
                if (Objects.nonNull(annotation)) {
                    String value = annotation.value();
                    genField.setFieldLine(ListTs.asList(
                            "// " + value,
                            "@Schema(description = \"" + value + "\")"
                    ));
                }
                fieldList.add(genField);
            }
            fieldImportList.add(Schema.class.getName());
            String p = aClass.getName();
            String simpleName = aClass.getSimpleName();
            String simpnameDto = simpleName + "Dto";
            List<String> annotationList = ListTs.newArrayList();
            GenDto genInterface = new GenDto();
            GenDomainName annotation = aClass.getAnnotation(GenDomainName.class);
            String aValue = null;
            if (null != annotation) {
                aValue = annotation.value();
            }
            if (StrUtil.isBlank(aValue)) {
                aValue = simpleName;
            }
            annotationList.add(Schema.class.getSimpleName() + "(description=\"" + aValue + "\",name=\"" + simpnameDto + "\")");
            genInterface.setGenDomainName(aValue);

            genInterface.setBaseUrl(javaBaseUrl);
            genInterface.setPackageName(dtoPackageName);
            genInterface.setDtoClassName(simpnameDto);
            genInterface.setFieldList(fieldList);
            if (configJpaGen.getGenNote()) {
                genInterface.setLineList(
                        ListTs.asList(
                                "/**",
                                " * by easy4j-gen auto generate",
                                " */"
                        ));
            }

            fieldImportList.add(BaseDto.class.getName());
            fieldImportList.add(Data.class.getName());
            fieldImportList.add(EasyException.class.getName());
            fieldImportList.add(EqualsAndHashCode.class.getName());
            genInterface.setImportList(
                    ListTs.newArrayList(fieldImportList.iterator())
            );
            annotationList.add(Data.class.getSimpleName());
            annotationList.add(EqualsAndHashCode.class.getSimpleName() + "(callSuper = true)");
            genInterface.setAnnotationList(annotationList);

            String s1 = combineUrlAndPackageName(s4, dtoPackageName) + File.separator + genInterface.getDtoClassName() + ".java";
            File file = new File(s1);
            if (file.exists()) {
                continue;
            }
            System.out.println("gen dto:" + s1);
            try (Writer out = new FileWriter(s1)) {
                template4.process(genInterface, out);
            }
        }
    }


    public String getSimpleName(ConfigJpaGen configJpaGen, Class<?> typeClass) {
        String simpleName = typeClass.getSimpleName();
        Boolean genDtoDateToString = configJpaGen.getGenDtoDateToString();
        if (genDtoDateToString && "Date".equals(simpleName)) {
            return "String";
        }
        return simpleName;
    }


}
