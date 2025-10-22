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
package easy4j.infra.knife4j;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysConstant;
import jodd.util.StringPool;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 文档参数赋值
 * <br/>
 * 文档动态分组根据@ControllerModule注解
 *
 * @author bokun
 */
public class DocEnvironment extends AbstractEasy4jEnvironment {
    @Override
    public String getName() {
        return "knife4j-config-environment";
    }

    @Override
    public Properties getProperties() {

        Properties properties = new Properties();

        properties.setProperty(SysConstant.KNIFE4J_ENABLE, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_LANGUAGE, "zh-CN");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_SWAGGER_MODELS, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_DOCUMENT_MANAGE, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_SWAGGER_MODEL_NAME, "实体类列表");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_VERSION, "false");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_RELOAD_CACHE_PARAMETER, "false");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_AFTER_SCRIPT, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_FILTER_MULTIPART_API_METHOD_TYPE, "POST");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_FILTER_MULTIPART_APIS, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_REQUEST_CACHE, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_HOST, "false");
        String envProperty = getEnvProperty(SysConstant.EASY4J_SERVER_PORT);
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_HOST_TEXT, "localhost:"+envProperty);
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_HOME_CUSTOM, "false");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_HOME_CUSTOM_LOCATION, "classpath:markdown/home.md");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_SEARCH, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_FOOTER, "false");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_FOOTER_CUSTOM, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_FOOTER_CUSTOM_CONTENT, "Apache License 2.0 | Copyright 2025");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_DYNAMIC_PARAMETER, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_DEBUG, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_OPEN_API, "true");
        properties.setProperty(SysConstant.KNIFE4J_SETTING_ENABLE_GROUP, "true");
        properties.setProperty(SysConstant.KNIFE4J_CORS, "false");
        properties.setProperty(SysConstant.KNIFE4J_PRODUCTION, "false");
        properties.setProperty(SysConstant.KNIFE4J_BASIC_ENABLE, "true");
        properties.setProperty(SysConstant.KNIFE4J_BASIC_USERNAME, "easy4j");
        properties.setProperty(SysConstant.KNIFE4J_BASIC_PASSWORD, "easy123");

        Set<Class<?>> classes = new HashSet<>();
//        Set<Class<?>> classes = scanPackageByAnnotation(Easy4j.mainClass.getPackage().getName(), ControllerModule.class);
        Boolean aggregations = getEnvProperty(SysConstant.EASY4J_NACOS_AGGREGATION, boolean.class);
        if(!aggregations){
            classes = scanPackageByAnnotation(Easy4j.mainClass.getPackage().getName(), ControllerModule.class);
        }

        Set<Class<?>> restControllers = scanPackageByAnnotation(Easy4j.mainClass.getPackage().getName(), Controller.class);
        Map<String, Object> controllerMap = new HashMap<>();
        for (Class<?> restController : restControllers) {
            RequestMapping annotation = restController.getAnnotation(RequestMapping.class);
            if (annotation != null) {
                String[] value = annotation.value();
                String[] path = annotation.path();
                List<String> paths = ListTs.concat(value, path);
                paths.forEach(p -> {
                    if (p.startsWith(StringPool.SLASH)) {
                        String regex = "^/+";
                        String substring = p.replaceAll(regex, "");
                        if (StrUtil.isNotBlank(substring)) {
                            controllerMap.put(substring, "true");
                        }
                    } else {
                        controllerMap.put(p, "true");
                    }
                });
            }
        }
        int i = 0;
        for (Class<?> aClass : classes) {
            ControllerModule annotation = aClass.getAnnotation(ControllerModule.class);
            String name = annotation.name();
            if (StrUtil.isNotBlank(name) && name.startsWith(StringPool.SLASH)) {
                String regex = "^/+";
                name = name.replaceAll(regex, "");
            }
            if (StrUtil.isBlank(name)) {
                break;
            }
            String description = annotation.description();
            controllerMap.remove(name);
            properties.setProperty("springdoc.group-configs[" + i + "].group", description);
            properties.setProperty("springdoc.group-configs[" + i + "].pathsToMatch[0]", "/" + name + "/**");
            properties.setProperty("springdoc.group-configs[" + i + "].displayName", description);

            properties.setProperty("knife4j.documents[" + i + "].group", description);
            properties.setProperty("knife4j.documents[" + i + "].name", "其他文档");
            properties.setProperty("knife4j.documents[" + i + "].locations", "classpath:markdown/*");
            i++;
        }

        if (CollUtil.isEmpty(classes)) {
            properties.setProperty("knife4j.documents[" + i + "].group", "default");
            properties.setProperty("knife4j.documents[" + i + "].name", "其他文档");
            properties.setProperty("knife4j.documents[" + i + "].locations", "classpath:markdown/*");
        } else {
            Set<String> strings = controllerMap.keySet();
            if (CollUtil.isNotEmpty(strings)) {
                properties.setProperty("springdoc.group-configs[" + i + "].group", "default");
                int nameInt = 0;
                for (String s : strings) {
                    properties.setProperty("springdoc.group-configs[" + i + "].pathsToMatch[" + nameInt + "]", "/" + s + "/**");
                    nameInt++;
                }
                properties.setProperty("springdoc.group-configs[" + i + "].displayName", "default");
                properties.setProperty("knife4j.documents[" + i + "].group", "default");
                properties.setProperty("knife4j.documents[" + i + "].name", "其他文档");
                properties.setProperty("knife4j.documents[" + i + "].locations", "classpath:markdown/*");
            }
        }


        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {

    }

    public static Set<Class<?>> scanPackageByAnnotation(
            String packageName, final Class<? extends Annotation> annotationClass) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
        Set<Class<?>> classes = new HashSet<>();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        for (BeanDefinition beanDefinition : scanner.findCandidateComponents(packageName)) {
            try {
                Class<?> clazz = contextClassLoader.loadClass(beanDefinition.getBeanClassName());
                classes.add(clazz);
            } catch (ClassNotFoundException ignore) {

            }
        }
        return classes;
    }
}
