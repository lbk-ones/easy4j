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
package easy4j.module.mapstruct;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.plugin.gen.AbstractCodeGen;
import easy4j.module.base.plugin.gen.JavaBaseMethod;
import easy4j.module.base.utils.ListTs;
import freemarker.template.Template;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GenMapStruct
 *
 * @author bokun.li
 * @date 2025-05
 */
public final class GenMapStruct extends AbstractCodeGen<GenMapStructConfig> {

    private GenMapStruct(){}

    public GenMapStruct(GenMapStructConfig config){
        synchronized (GenMapStruct.class){
            super.setConfigGen(config);
        }
    }


    /**
     * scanPackageName 扫描包名
     * currentMapperStructInterfaceClass 已经生成的class
     *
     * @throws Exception
     */
    @Override
    protected void genTemplate() throws Exception {

        GenMapStructConfig configGen = this.getConfigGen();

        Template mapstruct = getTemplate("mapstruct.ftl");

        List<JavaBaseMethod> methods = ListTs.newArrayList();
        Set<String> importList = new HashSet<>();

        getOldMethods(configGen, importList, methods);

        List<Class<?>> classes = scanClasses(configGen.getScanPackageName());

        for (Class<?> clazz : classes) {
            String currentClassName = clazz.getSimpleName();

            GenMapperStruct annotation = clazz.getAnnotation(GenMapperStruct.class);

            if(null == annotation){
                continue;
            }
            importList.add(clazz.getName());
            Class<?>[] value = annotation.value();

            for (Class<?> aClass : value) {

                if(null == aClass){
                    continue;
                }
                importList.add(aClass.getName());

                String simpleName = aClass.getSimpleName();

                JavaBaseMethod javaBaseMethod = new JavaBaseMethod();
                javaBaseMethod.setReturnTypeName(simpleName);
                javaBaseMethod.setMethodName("to"+ StrUtil.upperFirst(simpleName));
                javaBaseMethod.setParams(currentClassName+" "+StrUtil.lowerFirst(currentClassName));

                if(!methods.contains(javaBaseMethod)){
                    methods.add(javaBaseMethod);
                }

                // flip
                JavaBaseMethod javaBaseMethod2 = new JavaBaseMethod();
                javaBaseMethod2.setReturnTypeName(currentClassName);
                javaBaseMethod2.setMethodName("to"+ StrUtil.upperFirst(currentClassName));
                javaBaseMethod2.setParams(simpleName+" "+StrUtil.lowerFirst(simpleName));
                if(!methods.contains(javaBaseMethod2)){
                    methods.add(javaBaseMethod2);
                }
            }
        }
        if(CollUtil.isEmpty(methods)){
            return;
        }
        GenMapStructParams genMapStructParams = new GenMapStructParams();
        genMapStructParams.setMethodList(methods);
        genMapStructParams.setImportList(ListTs.newArrayList(importList.iterator()));
        genMapStructParams.setCurrentPackageName(configGen.getOutPackageName());

        String outAbsoluteUrl = configGen.getOutAbsoluteUrl();
        String outPackageName = configGen.getOutPackageName();
        String s1 = resolveUrl(outAbsoluteUrl, String.join("/", outPackageName.split("\\.")));
        checkDirReSolvePackageName(outAbsoluteUrl,outPackageName);
        String s = resolveUrl(s1, configGen.getMapperStructInterfaceName() + ".java");
        File file = new File(s);
        if (file.exists()) {
            boolean delete = file.delete();
            if (!delete) {
                throw new RuntimeException("delete old file error");
            }
        }
        writeFile(mapstruct, genMapStructParams, s);
    }

    private void getOldMethods(GenMapStructConfig configGen, Set<String> importList, List<JavaBaseMethod> methods) {
        Class<?> currentMapperStructInterfaceClass = configGen.getCurrentMapperStructInterfaceClass();
        if(null != currentMapperStructInterfaceClass){
            Method[] methodsList = ReflectUtil.getMethods(currentMapperStructInterfaceClass);
            for (Method method : methodsList) {
                // 不允许出现 default方法
                if (method.isDefault()) {
                    continue;
                }
                JavaBaseMethod javaBaseMethod = new JavaBaseMethod();
                String name = method.getName();
                javaBaseMethod.setMethodName(name);
                Type genericReturnType = method.getGenericReturnType();
                String typeName = getTypeName(genericReturnType, importList);
                javaBaseMethod.setReturnTypeName(getLastDotName(typeName));
                Parameter[] parameters = method.getParameters();
                List<String> paramsStr = ListTs.newArrayList();
                for (Parameter parameter : parameters) {
                    Type parameterizedType = parameter.getParameterizedType();
                    String name1 = parameter.getName();
                    paramsStr.add(getLastDotName(getTypeName(parameterizedType, importList))+" "+name1);
                }
                String join = String.join(",", paramsStr);
                javaBaseMethod.setParams(join);
                if (!methods.contains(javaBaseMethod)) {
                    methods.add(javaBaseMethod);
                }
            }

        }
    }


}
