package easy4j.module.mybatisplus.codegen.servlet.ast;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.javaparser.JavaToken;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import easy4j.module.mybatisplus.audit.AutoAudit;
import easy4j.module.mybatisplus.audit.BaseAudit;
import easy4j.module.mybatisplus.base.AuditPageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Java类文件解析工具（解析DTO/Domain的字段、注解信息）
 */
@Slf4j
public class JavaClassParser {

    private static final Map<String, ClassParseResult> CACHE_MAP = Maps.newConcurrentMap();
    private static final Map<String, List<ClassApi>> API_MAP = Maps.newConcurrentMap();


    public static JavaClassParser INSTANCE = new JavaClassParser();

    private JavaClassParser() {
    }

    /**
     * 解析Java文件，提取类的字段、注解信息
     *
     * @param javaFile .java源文件对象
     * @return 解析结果
     * @throws IOException 读取文件/解析失败时抛出
     */
    public ClassParseResult parse(String javaFile) {
        return CACHE_MAP.computeIfAbsent(javaFile, (f) -> {
            try {
                // 1. 读取文件并解析为AST语法树
                CompilationUnit compilationUnit;
                try (FileInputStream fis = new FileInputStream(javaFile)) {
                    compilationUnit = StaticJavaParser.parse(fis);
                }
                ClassParseResult result = new ClassParseResult();
                List<ClassField> fieldList = new ArrayList<>();
                // 2. 遍历语法树，找到目标类（假设文件中只有一个类）
                compilationUnit.accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(ClassOrInterfaceDeclaration clazz, Void arg) {
                        super.visit(clazz, arg);
                        // 设置类名
                        result.setClassName(clazz.getNameAsString());

                        // 3. 解析类上的注解：@Table（表名）、@Schema（中文描述）
                        parseClassAnnotations(clazz, result);

                        // 4. 解析所有字段：名称、类型 + @TableId标注的主键
                        parseClassFields(clazz, result, fieldList);
                    }
                }, null);

                result.setFields(fieldList);

                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 解析 controller的 地址
    public List<ClassApi> parseApi(String fileName) {
        return API_MAP.computeIfAbsent(fileName, (f) -> {
            try {
                //  读取文件并解析为AST语法树
                CompilationUnit compilationUnit;
                try (FileInputStream fis = new FileInputStream(fileName)) {
                    compilationUnit = StaticJavaParser.parse(fis);
                }
                List<ClassApi> result = ListTs.newArrayList();
                // 遍历语法树，找到目标类（假设文件中只有一个类）
                compilationUnit.accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(ClassOrInterfaceDeclaration clazz, Void arg) {
                        super.visit(clazz, arg);
                        NodeList<AnnotationExpr> annotations = clazz.getAnnotations();
                        boolean isController = false;
                        String prefixUrl = null;
                        for (AnnotationExpr annotation : annotations) {
                            String annoName = annotation.getNameAsString();
                            if ("RequestMapping".equals(annoName)) {
                                if (prefixUrl == null) prefixUrl = handlerAnnotation(annotation, "value", 0);
                            }
                            if ("RestController".equals(annoName)) {
                                isController = true;
                            }
                            if ("Controller".equals(annoName)) {
                                isController = true;
                            }
                        }
                        if (!isController) {
                            log.info("detect is not controller so skip the file: "+fileName);
                            return;
                        }
                        if(StrUtil.isWrap(prefixUrl,"${","}")){
                            String s = StrUtil.unWrap(prefixUrl, "${", "}");
                            if(StrUtil.isNotBlank(s)) prefixUrl = StrUtil.trim(Easy4j.getProperty(s));
                        }else{
                            if (prefixUrl != null && prefixUrl.contains(".")) {
                                String s = ListTs.get(ListTs.asList(prefixUrl.split("\\.")), 1);
                                prefixUrl = clazz.getFieldByName(s)
                                        .flatMap(Node::getTokenRange)
                                        .map(TokenRange::getBegin)
                                        .map(e -> getStaticValue(e))
                                        .orElse(null);
                            }
                        }
                        List<MethodDeclaration> methods = clazz.getMethods();
                        for (MethodDeclaration method : methods) {
                            boolean isMap = false;
                            String path = null;
                            // 简单介绍
                            String summary = null;
                            // 描述
                            String description = null;
                            for (AnnotationExpr annotation : method.getAnnotations()) {
                                String annoName = annotation.getNameAsString();
                                if ("PostMapping".equals(annoName)) {
                                    if (path == null) path = handlerAnnotation(annotation, "value", 0);
                                    isMap = true;
                                }
                                if ("RequestMapping".equals(annoName)) {
                                    if (path == null) path = handlerAnnotation(annotation, "value", 0);
                                    isMap = true;
                                }
                                if ("GetMapping".equals(annoName)) {
                                    if (path == null) path = handlerAnnotation(annotation, "value", 0);
                                    isMap = true;
                                }
                                if ("PutMapping".equals(annoName)) {
                                    if (path == null) path = handlerAnnotation(annotation, "value", 0);
                                    isMap = true;
                                }
                                if ("DeleteMapping".equals(annoName)) {
                                    if (path == null) path = handlerAnnotation(annotation, "value", 0);
                                    isMap = true;
                                }
                                if ("Operation".equals(annoName)) {
                                    if (summary == null) summary = handlerAnnotation(annotation, "summary", 0);
                                    if (description == null)
                                        description = handlerAnnotation(annotation, "description", 0);
                                }
                            }
                            if (isMap) {
                                path = StrUtil.trim(path);
                                if(StrUtil.isWrap(path,"${","}")){
                                    String s = StrUtil.unWrap(path, "${", "}");
                                    path = StrUtil.trim(Easy4j.getProperty(s));
                                }
                                path = ListTs.asList(path.split("/")).stream().map(e -> {
                                    if (StrUtil.isWrap(e, "{", "}")) {
                                        return null;
                                    }
                                    return e;
                                }).filter(Objects::nonNull).collect(Collectors.joining("/"));
                                ClassApi classApi = new ClassApi();
                                classApi.setPrefix(prefixUrl);
                                classApi.setPath(path);
                                classApi.setUrl(StrUtil.removeSuffix(StrUtil.addPrefixIfNot(StrUtil.removeSuffix(prefixUrl, "/"),"/"),"/") + "/" + StrUtil.removePrefix(path, "/"));
                                classApi.setSummary(summary);
                                classApi.setDescription(description);
                                result.add(classApi);
                            }

                        }

                    }
                }, null);

                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }

    // 直接解析值 拿到 常量值
    public String getStaticValue(JavaToken javaToken) {
        if (javaToken != null) {
            String string = StrUtil.trim(javaToken.asString());
            if (!StrUtil.isBlank(string) && StrUtil.isWrap(string, "\"", "\"")) {
                return StrUtil.trim(StrUtil.unWrap(string, "\"", "\""));
            } else {
                Optional<JavaToken> nextToken1 = javaToken.getNextToken();
                JavaToken javaToken2 = nextToken1.orElse(null);
                if (javaToken2 != null) {
                    return getStaticValue(javaToken2);
                }
            }
        }
        return null;
    }

    private String handlerAnnotation(AnnotationExpr annotationExpr, String attrName, int arrayIndex) {
        String res = "-";
        if (annotationExpr instanceof NormalAnnotationExpr && StrUtil.isNotBlank(attrName)) {
            NormalAnnotationExpr normalAnno = (NormalAnnotationExpr) annotationExpr;
            Optional<MemberValuePair> namePair = normalAnno.getPairs().stream()
                    .filter(pair -> attrName.equals(pair.getNameAsString()))
                    .findFirst();
            res = namePair.map(e -> extractAnnotationValue(e.getValue(), arrayIndex)).orElse("");
        }
        if (annotationExpr instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr normalAnno = (SingleMemberAnnotationExpr) annotationExpr;
            Expression memberValue = normalAnno.getMemberValue();
            res = extractAnnotationValue(memberValue, arrayIndex);
        }

        return res;

    }

    /**
     * 解析类上的注解：@Table（表名）、@Schema（中文描述）
     */
    private void parseClassAnnotations(ClassOrInterfaceDeclaration clazz, ClassParseResult result) {
        // 遍历类上的所有注解
        for (AnnotationExpr annotation : clazz.getAnnotations()) {
            String annoName = annotation.getNameAsString();

            // 解析@Table注解（MyBatis-Plus的@Table，或JPA的@Table，逻辑类似）
            if ("TableName".equals(annoName) || "com.baomidou.mybatisplus.annotation.TableName".equals(annoName)) {
                if (annotation instanceof NormalAnnotationExpr) {

                    NormalAnnotationExpr normalAnno = (NormalAnnotationExpr) annotation;
                    // 提取@Table(name = "t_user")中的name值
                    Optional<MemberValuePair> namePair = normalAnno.getPairs().stream()
                            .filter(pair -> "value".equals(pair.getNameAsString()))
                            .findFirst();
                    namePair.ifPresent(pair ->
                            result.setTableName(pair.getValue().toString().replace("\"", "").replace("'", ""))
                    );
                }
                if (annotation instanceof SingleMemberAnnotationExpr) {
                    SingleMemberAnnotationExpr normalAnno = (SingleMemberAnnotationExpr) annotation;
                    Expression memberValue = normalAnno.getMemberValue();
                    String s = extractAnnotationValue(memberValue, -1);
                    result.setTableName(s);
                }
            }

            // 解析@Schema注解（Swagger/OpenAPI的@Schema）
            if ("Schema".equals(annoName) || "io.swagger.v3.oas.annotations.media.Schema".equals(annoName)) {
                if (annotation instanceof NormalAnnotationExpr) {
                    NormalAnnotationExpr normalAnno = (NormalAnnotationExpr) annotation;

                    // 提取@Schema(description = "用户信息表")中的description值
                    Optional<MemberValuePair> descPair = normalAnno.getPairs().stream()
                            .filter(pair -> "description".equals(pair.getNameAsString()))
                            .findFirst();
                    descPair.ifPresent(pair ->
                            result.setSchemaDesc(pair.getValue().toString().replace("\"", "").replace("'", ""))
                    );
                }
            }
        }
    }


    /**
     * 提取注解值的核心方法（适配不同值类型）
     *
     * @param valueExpr  注解值的表达式对象
     * @param arrayIndex
     * @return 解析后的字符串值
     */
    private static String extractAnnotationValue(Expression valueExpr, int arrayIndex) {
        //  最常见：字符串值（如@TableId("userId")）
        if (valueExpr instanceof StringLiteralExpr) {
            StringLiteralExpr strExpr = (StringLiteralExpr) valueExpr;
            return strExpr.getValue(); // 直接获取字符串内容（自动去掉引号）
        }
        // 布尔值（如@MyAnnotation(true)）

        else if (valueExpr instanceof BooleanLiteralExpr) {
            BooleanLiteralExpr boolExpr = (BooleanLiteralExpr) valueExpr;
            return String.valueOf(boolExpr.getValue());
        }
        // 数值（如@MyAnnotation(100)）
        else if (valueExpr instanceof IntegerLiteralExpr) {
            IntegerLiteralExpr intExpr = (IntegerLiteralExpr) valueExpr;
            return intExpr.getValue();
        } else if (valueExpr instanceof ArrayInitializerExpr) {
            ArrayInitializerExpr intExpr = (ArrayInitializerExpr) valueExpr;
            NodeList<Expression> values = intExpr.getValues();
            int i = 0;
            for (Expression value : values) {
                if (i == arrayIndex) {
                    return extractAnnotationValue(value, arrayIndex);
                }
                i++;
            }
        }
        // 常量/变量引用（如@MyAnnotation(Constant.ID)）
        else if (valueExpr instanceof NameExpr) {
            NameExpr nameExpr = (NameExpr) valueExpr;
            ResolvedValueDeclaration resolvedConst = nameExpr.resolve();
            String constClassName = resolvedConst.asType().getQualifiedName(); // 常量类全限定名
            String constFieldName = resolvedConst.getName(); // 常量名
            try {
                Class<?> aClass = JavaClassParser.class.getClassLoader().loadClass(constClassName);
                Field declaredField = aClass.getDeclaredField(constFieldName);
                Object o = declaredField.get(null);
                return StrUtil.toString(o);
            } catch (Exception ignored) {
            }

            return nameExpr.getNameAsString(); // 获取常量名（如Constant.ID）
        }else if(valueExpr instanceof FieldAccessExpr){
            // TODO
        }
        // 其他类型（如null、表达式），返回原始字符串
        return valueExpr.toString();
    }


    /**
     * 解析类的所有字段：名称、类型，以及@TableId标注的主键
     */
    private void parseClassFields(ClassOrInterfaceDeclaration clazz, ClassParseResult result, List<ClassField> fieldList) {


        // 遍历类的所有字段
        for (FieldDeclaration field : clazz.getFields()) {
            // 跳过静态字段（static）
            if (field.isStatic()) {
                continue;
            }

            // 字段名称（如userId）
            String fieldName = field.getVariables().get(0).getNameAsString();
            // 字段类型（处理泛型，如List<String>）
            Type fieldType = field.getCommonType();
            String typeStr = fieldType.toString();

            // 封装字段信息
            ClassField classField = new ClassField();
            classField.setFieldName(fieldName);
            classField.setFieldType(typeStr);

            // 解析@TableId注解，标记主键字段
            for (AnnotationExpr annotation : field.getAnnotations()) {
                String annoName = annotation.getNameAsString();
                if ("TableId".equals(annoName) || "com.baomidou.mybatisplus.annotation.TableId".equals(annoName)) {
                    String tableIdFieldName = result.getTableIdFieldName();
                    if (StrUtil.isBlank(tableIdFieldName)) {
                        result.setTableIdFieldName(fieldName);
                    }
                }

                if ("Schema".equals(annoName) || "io.swagger.v3.oas.annotations.media.Schema".equals(annoName)) {
                    if (annotation instanceof NormalAnnotationExpr) {
                        NormalAnnotationExpr normalAnno = (NormalAnnotationExpr) annotation;
                        // 提取@Schema(description = "用户信息表")中的description值
                        Optional<MemberValuePair> descPair = normalAnno.getPairs().stream()
                                .filter(pair -> "description".equals(pair.getNameAsString()))
                                .findFirst();
                        descPair.ifPresent(pair ->
                                classField.setCnDesc(extractAnnotationValue(pair.getValue(), -1))
                        );
                    }
                }
            }

            if (!fieldList.contains(classField)) {
                fieldList.add(classField);
            }


        }

        NodeList<ClassOrInterfaceType> extendedTypes = clazz.getExtendedTypes();

        for (ClassOrInterfaceType extendedType : extendedTypes) {
            SimpleName name = extendedType.getName();
            Field[] fields = new Field[]{};
            if (StrUtil.equals(AutoAudit.class.getSimpleName(), name.asString())) {
                fields = ReflectUtil.getFields(AutoAudit.class);
            } else if (
                    StrUtil.equals(BaseAudit.class.getSimpleName(), name.asString()) ||
                            StrUtil.equals(AuditPageDto.class.getSimpleName(), name.asString())
            ) {
                fields = ReflectUtil.getFields(BaseAudit.class);
            }
            for (Field field : fields) {
                ClassField classField = new ClassField();
                classField.setFieldName(field.getName());
                classField.setFieldType(field.getType().getSimpleName());
                if (field.isAnnotationPresent(Schema.class)) {
                    Schema annotation = field.getAnnotation(Schema.class);
                    String description = annotation.description();
                    classField.setCnDesc(description);
                }
                if (!fieldList.contains(classField)) {
                    fieldList.add(classField);
                }
            }
        }
    }

    // 测试方法
    public static void main1(String[] args) throws IOException {
        // 示例：解析UserDomain.java文件
        String domainFile = "E:\\IdeaProjects\\ssc\\dataspace-approval-service\\src\\main\\java\\com\\ssc\\dataspace\\approval\\domains\\FlowFormContent.java";
        JavaClassParser parser = new JavaClassParser();
        ClassParseResult result = parser.parse(domainFile);

        // 打印解析结果
        System.out.println("类名：" + result.getClassName());
        System.out.println("表名：" + result.getTableName());
        System.out.println("Schema描述：" + result.getSchemaDesc());
        System.out.println("主键字段：" + result.getTableIdFieldName());
        System.out.println("所有字段：");
        for (ClassField field : result.getFields()) {
            System.out.println("  - " + field.getFieldName() + " : " + field.getFieldType() + " : " + field.getCnDesc());
        }
    }

    public static void main(String[] args) {
        // 示例：解析UserDomain.java文件
        String domainFile = "E:\\IdeaProjects\\ssc\\dataspace-approval-service\\src\\main\\java\\com\\ssc\\dataspace\\approval\\controller\\FormSettingController.java";
        JavaClassParser parser = new JavaClassParser();
        List<ClassApi> classApis = parser.parseApi(domainFile);
        for (ClassApi classApi : classApis) {
            System.out.println(classApi);
            System.out.println("-----");

        }
    }
}