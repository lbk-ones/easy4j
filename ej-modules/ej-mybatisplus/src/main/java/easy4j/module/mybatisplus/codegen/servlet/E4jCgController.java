package easy4j.module.mybatisplus.codegen.servlet;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.collect.Lists;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.PackageScanner;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.servletmvc.MethodType;
import easy4j.infra.common.utils.servletmvc.SRes;
import easy4j.infra.common.utils.servletmvc.ServletHandler;
import easy4j.infra.common.utils.servletmvc.UrlMap;
import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.TableMetadata;
import easy4j.module.mybatisplus.codegen.AutoGen;
import easy4j.module.mybatisplus.codegen.GenDto;
import easy4j.module.mybatisplus.codegen.GlobalGenConfig;
import easy4j.module.mybatisplus.codegen.MultiGenDto;
import easy4j.module.mybatisplus.codegen.db.DbGenSetting;
import easy4j.module.mybatisplus.codegen.servlet.ast.ClassApi;
import easy4j.module.mybatisplus.codegen.servlet.ast.ClassField;
import easy4j.module.mybatisplus.codegen.servlet.ast.ClassParseResult;
import easy4j.module.mybatisplus.codegen.servlet.ast.JavaClassParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.stream.Collectors;

public class E4jCgController {


    /**
     * 获取基础信息
     *
     * @param servletHandler
     */
    @UrlMap(url = "/init", method = MethodType.POST)
    public void init(ServletHandler servletHandler) {
        StandRes standRes = new StandRes();
        String dbUrl = Easy4j.getProperty(SysConstant.DB_URL_STR);
        String userName = Easy4j.getProperty(SysConstant.DB_USER_NAME);
        String password = Easy4j.getProperty(SysConstant.DB_USER_PASSWORD);
        standRes.setUrl(dbUrl);
        standRes.setUsername(userName);
        standRes.setPassword(password);
        standRes.setExclude("");
        standRes.setParentPackageName(Easy4j.mainClassPath);
        standRes.setProjectAbsolutePath(System.getProperty("user.dir"));
        standRes.setDeleteIfExists(false);
        standRes.setAuthor("easy4j");
        standRes.setForceDelete(false);
        try {
            DataSource dataSource = SpringUtil.getBean(DataSource.class);
            Connection connection = dataSource.getConnection();
            DialectV2 dialectV2 = DialectFactory.get(connection);
            List<TableMetadata> allTableInfoByTableType = dialectV2.getAllTableInfoByTableType(null, new String[]{"TABLE"});
            List<String> collect = allTableInfoByTableType
                    .stream()
                    .map(TableMetadata::getTableName)
                    .collect(Collectors.toList());

            standRes.setAllTables(collect);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        servletHandler.responseJson(SRes.success(standRes));
    }

    /**
     * 预览
     *
     * @param servletHandler handler
     */
    @UrlMap(url = "/db/preview", method = MethodType.POST)
    public void preview(ServletHandler servletHandler) {
        genOrPreview(servletHandler, true);

    }

    private void genOrPreview(ServletHandler servletHandler, boolean isPreview) {
        Optional<StandRes> formOrQuery = servletHandler.getFormOrQuery(StandRes.class);
        if (checkNotNullR(servletHandler,
                servletHandler.getFormDataMap(),
                ListTs.asList("url", "username", "password")
        )) {
            return;
        }
        formOrQuery.ifPresent(standRes -> {
            String url = standRes.getUrl();
            String username = standRes.getUsername();
            String password = standRes.getPassword();
            String tablePrefix = standRes.getTablePrefix();
            List<String> exclude = ListTs.asList(standRes.getExclude().split(SP.COMMA));
            String removeTablePrefix = standRes.getRemoveTablePrefix();
            String parentPackageName = standRes.getParentPackageName();
            String projectAbsolutePath = standRes.getProjectAbsolutePath();
            String urlPrefix = standRes.getUrlPrefix();
            boolean isDeleteIfExists = standRes.isDeleteIfExists();
            String headerDesc = standRes.getHeaderDesc();
            String author = standRes.getAuthor();
            boolean isForceDelete = standRes.isForceDelete();
            String entityPackageName = standRes.getEntityPackageName();
            String controllerPackageName = standRes.getControllerPackageName();
            String controllerReqPackageName = standRes.getControllerReqPackageName();
            String dtoPackageName = standRes.getDtoPackageName();
            String mapperPackageName = standRes.getMapperPackageName();
            String mapperXmlPackageName = standRes.getMapperXmlPackageName();
            String serviceInterfacePackageName = standRes.getServiceInterfacePackageName();
            String serviceImplPackageName = standRes.getServiceImplPackageName();
            List<String> allTables = standRes.getAllTables();
            boolean isGenMapperXml = standRes.isGenMapperXml();
            boolean isGenMapper = standRes.isGenMapper();
            boolean isGenEntity = standRes.isGenEntity();
            boolean isGenService = standRes.isGenService();
            boolean isGenServiceImpl = standRes.isGenServiceImpl();
            boolean isGenController = standRes.isGenController();
            boolean isGenControllerReq = standRes.isGenControllerReq();
            boolean isGenMapStruct = standRes.isGenMapStruct();
            boolean isGenDto = standRes.isGenDto();
            if (isGenController && StrUtil.isBlank(urlPrefix)) {
                servletHandler.responseJson(SRes.error("urlPrefix is not null "));
                return;
            }
            GenDto globalGenConfig1 = new GenDto()
                    .setAuthor(author)
                    .setHeaderDesc(headerDesc)
                    .setParentPackageName(parentPackageName)
                    .setProjectAbsolutePath(projectAbsolutePath)
                    .setUrlPrefix(urlPrefix)
                    .setDeleteIfExists(isDeleteIfExists)
                    .setEntityPackageName(entityPackageName)
                    .setControllerPackageName(controllerPackageName)
                    .setControllerReqPackageName(controllerReqPackageName)
                    .setDtoPackageName(dtoPackageName)
                    .setMapperPackageName(mapperPackageName)
                    .setMapperXmlPackageName(mapperXmlPackageName)
                    .setServiceInterfacePackageName(serviceInterfacePackageName)
                    .setMapperStructClassSimpleName(standRes.getMapperStructClassSimpleName())
                    .setMapperStructPackageName(standRes.getMapperStructPackageName())
                    .setServiceImplPackageName(serviceImplPackageName)
                    .setCreateTimeName(StrUtil.toCamelCase(standRes.getCreateTimeName()))
                    .setIsDeletedName(StrUtil.toCamelCase(standRes.getIsDeletedName()))
                    .setIsEnabledName(StrUtil.toCamelCase(standRes.getIsEnabledName()))
                    .setIsDeletedValid(standRes.getIsDeletedValid())
                    .setIsDeletedNotValid(standRes.getIsDeletedNotValid())
                    .setIsEnabledValid(standRes.getIsEnabledValid())
                    .setIsEnabledNotValid(standRes.getIsEnabledNotValid());

            PreviewRes res = AutoGen.build(globalGenConfig1)
                    .fromDbGen(new DbGenSetting()
                            .setUrl(url)
                            .setUsername(username)
                            .setPassword(password)
                            .setTablePrefix(tablePrefix)
                            .setRemoveTablePrefix(removeTablePrefix)
                            .setGenEntity(isGenEntity)
                            .setGenMapperXml(isGenMapperXml)
                            .setGenMapper(isGenMapper)
                            .setGenDto(isGenDto)
                            .setGenService(isGenService)
                            .setGenServiceImpl(isGenServiceImpl)
                            .setGenController(isGenController)
                            .setGenControllerReq(isGenControllerReq)
                            .setGenMapStruct(isGenMapStruct)
                            .setExclude(exclude)
                    )
                    .auto(isPreview, true);
            servletHandler.responseJson(SRes.success(res));
        });
        if (!formOrQuery.isPresent()) {
            servletHandler.responseJson(SRes.error("no query"));
        }
    }

    private boolean checkNotNullR(ServletHandler servletHandler, Map<String, String> formDataMap, List<String> nameList) {
        Set<String> objects = new HashSet<>();
        if (formDataMap != null) {
            for (String s : nameList) {
                String s2 = formDataMap.get(s);
                if (StrUtil.isBlank(s2)) {
                    objects.add(s);
                }
            }
        }
        if (!objects.isEmpty()) {
            servletHandler.responseJson(SRes.error(String.join(",", objects) + " is not null "));
            return true;
        }
        return false;

    }

    /**
     * 生成代码
     *
     * @param servletHandler handler
     */
    @UrlMap(url = "/db/gen", method = MethodType.POST)
    public void gen(ServletHandler servletHandler) {
        genOrPreview(servletHandler, false);
    }

    /**
     * 包扫描
     *
     * @param servletHandler handler
     */
    @UrlMap(url = "/db/scanPackage", method = MethodType.POST)
    public SRes scanPackage(ServletHandler servletHandler) {
        PackageRes packageRes = new PackageRes();
        Map<String, String> formDataMap = servletHandler.getFormDataMap();
        String parentPackageName = formDataMap.get("parentPackageName");
        String projectAbsolutePath = formDataMap.get("projectAbsolutePath");
        if (StrUtil.isBlank(parentPackageName)) parentPackageName = Easy4j.mainClassPath;
        String dtoPackageName = formDataMap.get("dtoPackageName");
        String entityPackageName = formDataMap.get("entityPackageName");
        String controllerPackageName = formDataMap.get("controllerPackageName");
        String abPath = projectAbsolutePath + SP.SLASH + "src/main/java";
        if (StrUtil.isNotBlank(dtoPackageName)) {
            String dtoPackage = String.join(".", ListTs.asList(
                    parentPackageName,
                    dtoPackageName
            ));
            String filePath = abPath + SP.SLASH + (String.join(SP.SLASH, ListTs.asList(dtoPackage.split("\\."))));
            List<String> allDtos = new ArrayList<>();
            File file = new File(filePath);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null) files = new File[]{};
                allDtos = Arrays.stream(files).filter(File::isFile).map(e -> e.getName().substring(0, e.getName().lastIndexOf(".") < 0 ? e.getName().length() : e.getName().lastIndexOf("."))).collect(Collectors.toList());
            }
            if (allDtos.isEmpty()) {
                try {
                    Set<Class<?>> classes = PackageScanner.scanPackage(dtoPackage, false);
                    allDtos = classes.stream().map(Class::getSimpleName).collect(Collectors.toList());
                    packageRes.setAllDtos(allDtos);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                packageRes.setAllDtos(allDtos);
            }
        }
        if (StrUtil.isNotBlank(entityPackageName)) {
            String entityPackage = String.join(".", ListTs.asList(
                    parentPackageName,
                    entityPackageName
            ));
            String filePath = abPath + SP.SLASH + (String.join(SP.SLASH, ListTs.asList(entityPackage.split("\\."))));
            List<String> allEntitys = new ArrayList<>();
            File file = new File(filePath);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null) files = new File[]{};
                allEntitys = Arrays.stream(files).filter(File::isFile).map(e -> e.getName().substring(0, e.getName().lastIndexOf(".") < 0 ? e.getName().length() : e.getName().lastIndexOf("."))).collect(Collectors.toList());
            }
            if (allEntitys.isEmpty()) {
                try {
                    Set<Class<?>> classes = PackageScanner.scanPackage(entityPackage, false);
                    allEntitys = classes.stream().map(Class::getSimpleName).collect(Collectors.toList());
                    packageRes.setAllEntitys(allEntitys);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                packageRes.setAllEntitys(allEntitys);
            }

        }
        // 扫描controller文件
        if (StrUtil.isNotBlank(controllerPackageName)) {
            String packageName = String.join(".", ListTs.asList(
                    parentPackageName,
                    controllerPackageName
            ));
            String filePath = abPath + SP.SLASH + (String.join(SP.SLASH, ListTs.asList(packageName.split("\\."))));
            List<String> allEntitys = new ArrayList<>();
            File file = new File(filePath);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null) files = new File[]{};
                allEntitys = Arrays.stream(files).filter(File::isFile).map(e -> e.getName().substring(0, e.getName().lastIndexOf(".") < 0 ? e.getName().length() : e.getName().lastIndexOf("."))).collect(Collectors.toList());
            }
            if (allEntitys.isEmpty()) {
                try {
                    Set<Class<?>> classes = PackageScanner.scanPackage(packageName, false);
                    allEntitys = classes.stream().map(Class::getSimpleName).collect(Collectors.toList());
                    packageRes.setAllControllers(allEntitys);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                packageRes.setAllControllers(allEntitys);
            }

        }
        return SRes.success(packageRes);
    }


    /**
     * 自定义生成代码
     *
     * @param servletHandler handler
     */
    @UrlMap(url = "/custom/gen", method = MethodType.POST)
    public SRes customGen(ServletHandler servletHandler) {
        Map<String, String> formDataMap = servletHandler.getFormDataMap();
        if (checkNotNullR(servletHandler,
                formDataMap,
                ListTs.asList("domainName", "cnDesc", "returnDtoName", "entityName")
        )) {
            return null;
        }
        String domainName = formDataMap.get("domainName");
        String parentPackageName = formDataMap.get("parentPackageName");
        String projectAbsolutePath = formDataMap.get("projectAbsolutePath");
        String urlPrefix = formDataMap.get("urlPrefix");
        boolean deleteIfExists = "true".equals(formDataMap.get("deleteIfExists"));
        String cnDesc = formDataMap.get("cnDesc");
        String returnDtoName = formDataMap.get("returnDtoName");
        String entityName = formDataMap.get("entityName");
        boolean isPreview = "true".equals(formDataMap.get("isPreview"));
        String entityPackageName = formDataMap.get("entityPackageName");
        String controllerPackageName = formDataMap.get("controllerPackageName");
        String controllerReqPackageName = formDataMap.get("controllerReqPackageName");
        String dtoPackageName = formDataMap.get("dtoPackageName");
        String mapperPackageName = formDataMap.get("mapperPackageName");
        String mapperXmlPackageName = formDataMap.get("mapperXmlPackageName");
        String serviceInterfacePackageName = formDataMap.get("serviceInterfacePackageName");
        String serviceImplPackageName = formDataMap.get("serviceImplPackageName");
        String createTimeName = StrUtil.toCamelCase(formDataMap.get("createTimeName"));
        String isDeletedName = StrUtil.toCamelCase(formDataMap.get("isDeletedName"));
        String isEnabledName = StrUtil.toCamelCase(formDataMap.get("isEnabledName"));
        String isDeletedValid = formDataMap.get("isDeletedValid");
        String isDeletedNotValid = formDataMap.get("isDeletedNotValid");
        String isEnabledValid = formDataMap.get("isEnabledValid");
        String isEnabledNotValid = formDataMap.get("isEnabledNotValid");
        String author = formDataMap.get("author");
        String headerDesc = formDataMap.get("headerDesc");
        AutoGen build = AutoGen.build(MultiGenDto.build(
                        new GlobalGenConfig()
                                .setAuthor(author)
                                .setHeaderDesc(headerDesc)
                                .setParentPackageName(parentPackageName)
                                .setProjectAbsolutePath(projectAbsolutePath)
                                .setUrlPrefix(urlPrefix)
                                .setDeleteIfExists(deleteIfExists)
                                .setEntityPackageName(entityPackageName)
                                .setControllerPackageName(controllerPackageName)
                                .setControllerReqPackageName(controllerReqPackageName)
                                .setDtoPackageName(dtoPackageName)
                                .setMapperPackageName(mapperPackageName)
                                .setMapperXmlPackageName(mapperXmlPackageName)
                                .setServiceInterfacePackageName(serviceInterfacePackageName)
                                .setServiceImplPackageName(serviceImplPackageName)
                                .setCreateTimeName(createTimeName)
                                .setIsDeletedName(isDeletedName)
                                .setIsEnabledName(isEnabledName)
                                .setIsDeletedValid(isDeletedValid)
                                .setIsDeletedNotValid(isDeletedNotValid)
                                .setIsEnabledValid(isEnabledValid)
                                .setIsEnabledNotValid(isEnabledNotValid)
                )
                .multiGen(
                        domainName + "-" + returnDtoName + "-" + cnDesc + "-" + entityName
                ));
        boolean genMapper = "true".equals(formDataMap.get(LambdaUtil.getFieldName(StandRes::isGenMapper)));
        boolean isGenController = "true".equals(formDataMap.get(LambdaUtil.getFieldName(StandRes::isGenController)));
        boolean isGenControllerReq = "true".equals(formDataMap.get(LambdaUtil.getFieldName(StandRes::isGenControllerReq)));
        boolean isGenService = "true".equals(formDataMap.get(LambdaUtil.getFieldName(StandRes::isGenService)));
        boolean isGenServiceImpl = "true".equals(formDataMap.get(LambdaUtil.getFieldName(StandRes::isGenServiceImpl)));
        if (genMapper) build.genMapper(); // 外围的这几个是配合multiGen使用的
        if (isGenController) build.genController();
        if (isGenControllerReq) build.genControllerReq();
        if (isGenService) build.genIService();
        if (isGenServiceImpl) build.genServiceImpl();
        PreviewRes previewRes = new PreviewRes();
        if (!build.genList.isEmpty()) {
            previewRes = build.custom(isPreview, true);
        }
        return SRes.success(previewRes);
    }

    /**
     * 前端配置生成前的初始化 基于 arco-design-vue3组件库的arco-vue3-supertable
     */
    @UrlMap(url = "/pageGenInit")
    public SRes pageGenInit(ServletHandler servletHandler) {
        PageViewRes pageViewRes = new PageViewRes();
        Map<String, String> formDataMap = servletHandler.getFormDataMap();
        String parentPackageName = formDataMap.get("parentPackageName");
        String controllerPackageName = formDataMap.get("controllerPackageName");
        String projectAbsolutePath = formDataMap.get("projectAbsolutePath");
        if (StrUtil.isBlank(parentPackageName)) parentPackageName = Easy4j.mainClassPath;
        String dtoPackageName = formDataMap.get("dtoPackageName");
        String entityPackageName = formDataMap.get("entityPackageName");
        String dtoName_ = formDataMap.get("dtoName");
        String domainName_ = formDataMap.get("domainName");
        String controllerName = formDataMap.get("controllerName");
        String isEnabledName = StrUtil.toCamelCase(formDataMap.get("isEnabledName"));
        if (checkNotNullR(servletHandler,
                formDataMap,
                ListTs.asList("dtoName", "domainName", "controllerName", "isEnabledName")
        )) {
            return null;
        }
        String abPath = projectAbsolutePath +
                File.separator +
                String.join(File.separator, ListTs.asList("src", "main", "java")) +
                File.separator +
                String.join(File.separator, parentPackageName.split("\\."));
        String dtoAb = abPath + File.separator + dtoPackageName;
        String domainAb = abPath + File.separator + entityPackageName;
        String dtoName = dtoAb + File.separator + dtoName_ + ".java";
        String domainName = domainAb + File.separator + domainName_ + ".java";
        try {
            File file = new File(dtoName);
            File file2 = new File(domainName);
            ClassParseResult dtoParse;
            ClassParseResult domainParse;
            if (file.exists() && file2.exists()) {
                dtoParse = JavaClassParser.INSTANCE.parse(dtoName);
                domainParse = JavaClassParser.INSTANCE.parse(domainName);
            } else {
                Class<?> dtoClass = Class.forName(parentPackageName + SP.DOT + dtoPackageName + SP.DOT + dtoName_);
                Class<?> domainClass = Class.forName(parentPackageName + SP.DOT + entityPackageName + SP.DOT + domainName_);
                dtoParse = clazzToClassParseResult(dtoClass);
                domainParse = clazzToClassParseResult(domainClass);
            }

            String tableName = domainParse.getTableName();
            pageViewRes.setUniqueId(RandomUtil.randomString(4) + "_" + StrUtil.blankToDefault(tableName, StrUtil.toUnderlineCase(domainParse.getClassName()).toLowerCase()));

            String schemaDesc = domainParse.getSchemaDesc();
            if (StrUtil.endWith(schemaDesc, "表")) schemaDesc = StrUtil.replaceLast(schemaDesc, "表", "");
            pageViewRes.setCnDesc(schemaDesc);
            pageViewRes.setRowKey(domainParse.getTableIdFieldName());
            pageViewRes.setControllerReqDtoName(StrUtil.lowerFirst(dtoName_) + "s");
            // scan all apis
            scanAllApi(abPath, parentPackageName, controllerPackageName, pageViewRes, controllerName);
            // default actions
            patchDefaultActions(pageViewRes);
            List<ClassField> fields = dtoParse.getFields();
            List<PageViewRes.ColumnInfo> objects = Lists.newArrayList();
            int size = fields.size();
            for (int i = 0; i < size; i++) {
                ClassField field = fields.get(i);
                String fieldName = field.getFieldName();
                if (StrUtil.equals(isEnabledName, fieldName) && StrUtil.isNotBlank(fieldName)) {
                    pageViewRes.getActions()
                            .add(new PageViewRes.ACTION("enabled", "启用/禁用")
                                    .setType("confirm")
                                    .setConfirmMessage("确定要启用/禁用选中的数据吗？")
                            );
                }
                PageViewRes.ColumnInfo columnInfo = new PageViewRes.ColumnInfo();
                columnInfo.setTitle(StrUtil.blankToDefault(field.getCnDesc(), "-"));
                columnInfo.setDataIndex(fieldName);
                PageViewRes.ColumnInfo.Form form = columnInfo.getForm();
                String fieldType = field.getFieldType();
                boolean isDate = false;
                if (ListTs.asList("Date", "LocalDateTime", "LocalDate").contains(fieldType)) {
                    form.setType("date");
                    isDate = true;
                    form.setPlaceholder("请选择" + columnInfo.getTitle());
                } else if (ListTs.asList("String", "char", "Character").contains(fieldType)) {
                    form.setType("input");
                    form.setPlaceholder("请输入" + columnInfo.getTitle());
                } else if (ListTs.asList("int", "double", "short", "long", "float", "BigDecimal", "Integer", "Double", "Short", "Long", "Float").contains(fieldType)) {
                    form.setType("number");
                    form.setPlaceholder("请输入" + columnInfo.getTitle());
                } else if (ListTs.asList("boolean", "Boolean").contains(fieldType)) {
                    form.setType("switch");
                    form.setPlaceholder("请选择" + columnInfo.getTitle());
                } else {
                    form.setPlaceholder("请输入" + columnInfo.getTitle());
                }
                ClassField classField = ListTs.get(fields, i + 1);
                if (null != classField) {
                    form.setEnterNext(classField.getFieldName());
                }
                if (i < size - 1) {
                    if (isDate) {
                        columnInfo.setWidth(175);
                    } else {
                        columnInfo.setWidth(160);
                    }
                }
                objects.add(columnInfo);
            }
            pageViewRes.setColumns(objects);
        } catch (Exception e) {
            throw new RuntimeException("出现异常:" + e.getMessage());
        }
        return SRes.success(pageViewRes);
    }

    private ClassParseResult clazzToClassParseResult(Class<?> dtoClass) {
        ClassParseResult classParseResult = new ClassParseResult();
        classParseResult.setClassName(dtoClass.getName());
        classParseResult.setTableName(Optional.ofNullable(dtoClass.getAnnotation(TableName.class)).map(TableName::value).orElseGet(() -> StrUtil.toUnderlineCase(dtoClass.getSimpleName())));
        classParseResult.setSchemaDesc(Optional.ofNullable(dtoClass.getAnnotation(Schema.class)).map(Schema::description).orElse(dtoClass.getSimpleName()));
        Field[] fields = ReflectUtil.getFields(dtoClass);
        List<ClassField> collect = Arrays.stream(fields).map(field -> {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                return null;
            }
            if (field.isAnnotationPresent(TableId.class)) {
                String tableIdFieldName = classParseResult.getTableIdFieldName();
                if (StrUtil.isBlank(tableIdFieldName)) classParseResult.setTableIdFieldName(field.getName());
            }
            ClassField classField = new ClassField();
            classField.setFieldName(field.getName());
            classField.setFieldType(field.getType().getSimpleName());
            classField.setCnDesc(Optional.ofNullable(field.getAnnotation(Schema.class)).map(Schema::description).orElseGet(field::getName));
            return classField;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        classParseResult.setFields(collect);
        return classParseResult;
    }

    private void patchDefaultActions(PageViewRes pageViewRes) {
        List<PageViewRes.ACTION> actions = pageViewRes.getActions();
        actions.add(new PageViewRes.ACTION("view", "查看")
                .setMessage("查看成功"));
        actions.add(new PageViewRes.ACTION("edit", "编辑")
                .setMessage("编辑成功"));
        actions.add(new PageViewRes.ACTION("delete", "删除")
                .setStatus("danger")
                .setType("confirm")
                .setConfirmMessage("确定要删除选中的数据吗？")
                .setMessage("删除成功")
        );
    }

    private static void scanAllApi(String abPath, String parentPackageName, String controllerPackageName, PageViewRes pageViewRes, String controllerName) {
        Set<PageViewRes.API> allUrls = new HashSet<>();
        if (StrUtil.isNotBlank(controllerPackageName)) {
            String filePath = abPath + File.separator + controllerPackageName;
            List<String> allEntitys = new ArrayList<>();
            File file = new File(filePath);
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null) files = new File[]{};
                allEntitys = Arrays.stream(files)
                        .filter(File::isFile)
                        .map(e -> e.getName().substring(0, e.getName().lastIndexOf(".") < 0 ? e.getName().length() : e.getName().lastIndexOf(".")))
                        .collect(Collectors.toList());
                for (String allEntity : allEntitys) {
                    if (!StrUtil.equals(controllerName, allEntity)) continue;
                    // ast parse
                    List<ClassApi> classApis = JavaClassParser.INSTANCE.parseApi(abPath + File.separator + controllerPackageName + File.separator + allEntity + ".java");
                    for (ClassApi classApi : classApis) {
                        String url = classApi.getUrl();
                        PageViewRes.API api = new PageViewRes.API();
                        api.setUrl(url);
                        api.setSummary(classApi.getSummary());
                        api.setDescription(classApi.getDescription());
                        allUrls.add(api);
                    }
                }
            }
            // jar 环境
            if (allEntitys.isEmpty()) {
                String cp = String.join(".", ListTs.asList(
                        parentPackageName,
                        controllerPackageName
                ));
                try {
                    Set<Class<?>> classes = PackageScanner.scanPackage(cp, false);
                    for (Class<?> aClass : classes) {
                        String simpleName = aClass.getSimpleName();
                        if (!StrUtil.equals(controllerName, simpleName)) continue;
                        if (!(aClass.isAnnotationPresent(Controller.class) || aClass.isAnnotationPresent(RestController.class))) {
                            continue;
                        }
                        if (aClass.isAnnotationPresent(RequestMapping.class)) {
                            RequestMapping annotation = aClass.getAnnotation(RequestMapping.class);
                            String[] value = annotation.value();
                            String[] path = annotation.path();
                            String[] strings = ArrayUtil.addAll(value, path);
                            String prefix = ListTs.get(strings, 0);
                            Method[] methods = ReflectUtil.getMethods(aClass);
                            for (Method method : methods) {
                                List<Class<? extends Annotation>> list = ListTs.asList(RequestMapping.class, PostMapping.class, GetMapping.class, PutMapping.class, DeleteMapping.class);
                                if (list.stream().noneMatch(method::isAnnotationPresent)) {
                                    continue;
                                }
                                boolean annotationPresent = method.isAnnotationPresent(Operation.class);
                                String summary_ = "";
                                String description_ = "";
                                if (annotationPresent) {
                                    Operation annotation1 = method.getAnnotation(Operation.class);
                                    summary_ = annotation1.summary();
                                    description_ = annotation1.description();
                                }
                                final String summary = summary_;
                                final String description = description_;
                                list.stream().filter(method::isAnnotationPresent).findFirst().ifPresent(e -> {
                                    Annotation annotation1 = method.getAnnotation(e);
                                    Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation1);
                                    String[] path2 = (String[]) annotationAttributes.get("path");
                                    String[] value2 = (String[]) annotationAttributes.get("value");
                                    String url = ListTs.get(ArrayUtil.addAll(value2, path2), 0);
                                    String allUrl = StrUtil.removeSuffix(
                                            StrUtil.addPrefixIfNot(
                                                    StrUtil.removeSuffix(
                                                            prefix,
                                                            "/"
                                                    )
                                                    , "/"
                                            ),
                                            "/"
                                    ) + "/" + StrUtil.removePrefix(url, "/");
                                    // 处理 /{id} 这种 如果 /{id}/xxx 这种可能会被误操作 但是先不管
                                    allUrl = ListTs.asList(allUrl.split("/")).stream().map(e2 -> {
                                        if (StrUtil.isWrap(e2, "{", "}")) {
                                            return null;
                                        }
                                        return e2;
                                    }).filter(Objects::nonNull).collect(Collectors.joining("/"));
                                    PageViewRes.API api = new PageViewRes.API();
                                    api.setUrl(allUrl);
                                    api.setSummary(summary);
                                    api.setDescription(description);
                                    allUrls.add(api);
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }
        String rL = StrUtil.replaceLast(controllerName, "Controller", "");
        for (PageViewRes.API allUrl : allUrls) {
            String url = allUrl.getUrl();
            if (StrUtil.contains(url, "pageQuery" + rL)) {
                if (StrUtil.isBlank(pageViewRes.getPageApiUrl())) pageViewRes.setPageApiUrl(url);
            }
            if (StrUtil.contains(url, "save" + rL)) {
                if (StrUtil.isBlank(pageViewRes.getFormAddApiUrl())) pageViewRes.setFormAddApiUrl(url);
            }
            if (StrUtil.contains(url, "delete" + rL)) {
                if (StrUtil.isBlank(pageViewRes.getFormDeleteApiUrl())) pageViewRes.setFormDeleteApiUrl(url);
            }
            if (StrUtil.contains(url, "update" + rL)) {
                if (StrUtil.isBlank(pageViewRes.getFormUpdateApiUrl())) pageViewRes.setFormUpdateApiUrl(url);
            }
            if (StrUtil.contains(url, "enableOrDisable" + rL)) {
                if (StrUtil.isBlank(pageViewRes.getEnableOrDisabledUrl())) pageViewRes.setEnableOrDisabledUrl(url);
            }
        }
        List<PageViewRes.API> strings = new ArrayList<>(allUrls);
        strings.sort(Comparator.comparing(PageViewRes.API::getUrl));
        pageViewRes.setAllApiUrl(strings);
    }

}
