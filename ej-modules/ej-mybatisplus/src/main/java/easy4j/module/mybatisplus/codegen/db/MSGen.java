package easy4j.module.mybatisplus.codegen.db;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.PackageScanner;
import easy4j.infra.context.api.gen.JavaBaseMethod;
import easy4j.module.mybatisplus.codegen.GlobalGenConfig;
import lombok.Data;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MapperStruct Gen
 */
public class MSGen {

    private final GlobalGenConfig globalGenConfig;

    private final DbGenSetting dbGenSetting;

    private final List<EntityInfo> entityInfos;

    public MSGen(GlobalGenConfig globalGenConfig, DbGenSetting dbGenSetting_, List<EntityInfo> entityInfos_) {
        this.globalGenConfig = globalGenConfig;

        this.dbGenSetting = dbGenSetting_;
        this.entityInfos = entityInfos_;
    }

    public Map<String, Object> getParams() throws Exception {
        boolean genDto = dbGenSetting.isGenDto();
        boolean genEntity = dbGenSetting.isGenEntity();

        List<JavaBaseMethod> methods = ListTs.newArrayList();
        Set<String> importList = new HashSet<>();
        importList.add("easy4j.module.mapstruct.TransferMapper");
        importList.add("org.mapstruct.Mapper");
        importList.add("org.mapstruct.factory.Mappers");
        getOldMethods(importList, methods);
        String parentPackageName = globalGenConfig.getParentPackageName();
        List<ClassInfo> allClassInfo = new ArrayList<>();
        String abPath = String.join(File.separator, globalGenConfig.getProjectAbsolutePath(), "src", "main", "java", String.join(File.separator, parentPackageName.split("\\.")), globalGenConfig.getEntityPackageName());
        File file = new File(abPath);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    if (file1.isDirectory()) {
                        continue;
                    }
                    ClassInfo classInfo = new ClassInfo();
                    String name = file1.getName();
                    int i = name.lastIndexOf(".");
                    if (i > 0) {
                        String subName = name.substring(0, i);
                        classInfo.setName(parentPackageName + "." + globalGenConfig.getEntityPackageName() + "." + subName);
                        classInfo.setSimpleName(subName);
                        allClassInfo.add(classInfo);
                    }

                }
            }
        }

        for (ClassInfo clazz : allClassInfo) {
            String currentClassName = clazz.getSimpleName();
            if (entityInfos != null) {
                if (entityInfos.stream().noneMatch(e -> StrUtil.equals(e.getSchema(), currentClassName))) {
                    continue;
                }
            }
            ClassInfo[] value = new ClassInfo[0];
            if (genDto) {
                String s = currentClassName + "Dto";
                ClassInfo classInfo = new ClassInfo();
                classInfo.setSimpleName(s);
                classInfo.setName(parentPackageName + "." + globalGenConfig.getDtoPackageName() + "." + s);
                value = ArrayUtil.append(value, classInfo);
            }
            if (genEntity) {
                value = ArrayUtil.append(value, clazz);
            }
            importList.add(clazz.getName());
            for (ClassInfo toClass : value) {
                if (null == toClass) continue;
                importList.add(toClass.getName());

                String simpleName = toClass.getSimpleName();

                JavaBaseMethod javaBaseMethod = new JavaBaseMethod();
                javaBaseMethod.setReturnTypeName(simpleName);
                javaBaseMethod.setMethodName("to" + StrUtil.upperFirst(simpleName));
                javaBaseMethod.setParams(currentClassName + " " + StrUtil.lowerFirst(currentClassName));

                if (!methods.contains(javaBaseMethod)) {
                    methods.add(javaBaseMethod);
                }

                // flip
                JavaBaseMethod javaBaseMethod2 = new JavaBaseMethod();
                javaBaseMethod2.setReturnTypeName(currentClassName);
                javaBaseMethod2.setMethodName("to" + StrUtil.upperFirst(currentClassName));
                javaBaseMethod2.setParams(simpleName + " " + StrUtil.lowerFirst(simpleName));
                if (!methods.contains(javaBaseMethod2)) {
                    methods.add(javaBaseMethod2);
                }
            }
        }
        if (CollUtil.isEmpty(methods)) {
            return null;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("parentPackageName", globalGenConfig.getParentPackageName());
        params.put("mapperStructPackageName", globalGenConfig.getMapperStructPackageName());
        params.put("importList", importList);
        params.put("mapperStructClassSimpleName", globalGenConfig.getMapperStructClassSimpleName());
        params.put("methodList", methods);
        return params;
    }

    private void getOldMethods(Set<String> importList, List<JavaBaseMethod> methods) {
        String parentPackageName = globalGenConfig.getParentPackageName();
        String mapperStructClassSimpleName = globalGenConfig.getMapperStructClassSimpleName();
        String projectAbsolutePath = globalGenConfig.getProjectAbsolutePath();
        String filePath = String.join(File.separator, projectAbsolutePath, "src", "main", "java", String.join(File.separator, parentPackageName.split("\\.")), globalGenConfig.getMapperStructPackageName(), mapperStructClassSimpleName + ".java");
        // 静态文件解析
        File file = new File(filePath);
        if (file.exists()) {
            List<String> list = FileReader.create(file).readLines();
            for (String s : list) {
                s = s.trim();
                if (StrUtil.isBlank(s)) continue;
                if (s.startsWith("import") && s.endsWith(";")) {
                    int i = s.indexOf("import");
                    String substring = s.substring(i + "import".length() + 1, s.length() - 1);
                    importList.add(substring.trim());
                }
                if (
                        s.endsWith(";") &&
                                !s.startsWith("package") &&
                                !s.startsWith("import") &&
                                !s.contains("Mappers.getMapper(MapperStruct.class)") &&
                                !s.startsWith("//") &&
                                !s.startsWith("*") &&
                                !s.startsWith("default") &&
                                !s.startsWith("static") &&
                                !s.startsWith("/*")
                ) {
                    // SysKeyIdempotentDto toTest333(SysKeyIdempotent sysKeyIdempotent);
                    int i1 = s.indexOf(" ");
                    int i2 = s.indexOf("(");
                    int i3 = s.indexOf(")");
                    String methodName = StrUtil.trim(StrUtil.sub(s, i1, i2));      // toTest333
                    String returnTypeName = StrUtil.trim(StrUtil.sub(s, 0, i1));   // SysKeyIdempotentDto
                    String params = StrUtil.trim(StrUtil.sub(s, i2 + 1, i3)); // SysKeyIdempotent sysKeyIdempotent
                    if (StrUtil.hasBlank(returnTypeName, methodName, params)) {
                        continue;
                    }
                    JavaBaseMethod javaBaseMethod = new JavaBaseMethod();
                    javaBaseMethod.setMethodName(methodName);
                    javaBaseMethod.setParams(params);
                    javaBaseMethod.setReturnTypeName(returnTypeName);
                    if (!methods.contains(javaBaseMethod)) {
                        methods.add(javaBaseMethod);
                    }
                }
            }
        }

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
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            // 递归处理数组元素的类型
            String componentTypeName = getTypeName(arrayType.getGenericComponentType(), fieldImportList);
            return componentTypeName + "[]";
        } else {
            String typeName = type.getTypeName();
            checkIsNeedImport(typeName, fieldImportList);
            return getLastDotName(typeName);
        }
    }

    protected void checkIsNeedImport(String name, Set<String> fieldImportList) {
        boolean contains = ListTs.asList("int", "byte", "short", "long", "boolean", "float", "double", "char").contains(name);

        if (!StrUtil.startWith(name, "java.lang") && !contains) {

            Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                fieldImportList.add(name);
            }
        }
    }

    public static String getLastDotName(String name) {
        List<String> list = ListTs.asList(name.split("\\."));
        if (list.size() > 1) {
            return list.get(list.size() - 1);
        } else {
            return name;
        }
    }


    @Data
    public static class ClassInfo {
        private String name;
        private String simpleName;

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            ClassInfo classInfo = (ClassInfo) object;
            return Objects.equals(name, classInfo.name) && Objects.equals(simpleName, classInfo.simpleName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, simpleName);
        }
    }
}

