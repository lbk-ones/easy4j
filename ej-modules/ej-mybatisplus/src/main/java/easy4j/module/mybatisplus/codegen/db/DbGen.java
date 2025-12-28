package easy4j.module.mybatisplus.codegen.db;

import cn.hutool.cache.GlobalPruneTimer;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.BasicType;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.google.common.collect.Sets;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.DatabaseColumnMetadata;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.PrimaryKeyMetadata;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.TableMetadata;
import easy4j.module.mybatisplus.audit.AutoAudit;
import easy4j.module.mybatisplus.codegen.AbstractGen;
import easy4j.module.mybatisplus.codegen.ObjectValue;
import easy4j.module.mybatisplus.codegen.servlet.PreviewRes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;
import org.springframework.jdbc.support.JdbcUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static easy4j.module.mybatisplus.codegen.db.FieldNameValidator.validateAndCorrect;

@EqualsAndHashCode(callSuper = true)
@Data
public class DbGen extends AbstractGen {

    private DbGenSetting dbGenSetting;

    public DbGen(DbGenSetting dbGenSetting) {
        this.dbGenSetting = dbGenSetting;
    }

    public String getFilePath() {
        return this.getProjectAbsolutePath();
    }

    public String gen(boolean isPreview, boolean isServer, ObjectValue objectValue) {
        String tablePrefix = this.dbGenSetting.getTablePrefix();
        notNull(this.dbGenSetting, "entityDto");
        //notNull(tablePrefix, "tablePrefix");
        notNull(this.dbGenSetting.getUrl(), "url");
        notNull(this.dbGenSetting.getUsername(), "userName");
        notNull(this.dbGenSetting.getPassword(), "passWord");
        if (
                !dbGenSetting.isGenEntity()
                        && !dbGenSetting.isGenMapperXml()
                        && !dbGenSetting.isGenMapper()
                        && !dbGenSetting.isGenService()
                        && !dbGenSetting.isGenServiceImpl()
                        && !dbGenSetting.isGenController()
                        && !dbGenSetting.isGenControllerReq()
                        && !dbGenSetting.isGenDto()
                        && !dbGenSetting.isGenMapStruct()
        ) {
            return null;
        }
        PreviewRes previewRes = new PreviewRes();
        TempDataSource tempDataSource = new TempDataSource(
                SqlType.getDriverClassNameByUrl(this.dbGenSetting.getUrl()),
                this.dbGenSetting.getUrl(),
                this.dbGenSetting.getUsername(),
                this.dbGenSetting.getPassword());
        Connection quietConnection = tempDataSource.getQuietConnection();
        List<String> finalList = ListTs.newList();
        try {
            System.out.println("successful estable complete connection");
            DialectV2 dialectV2 = DialectFactory.get(quietConnection);
            String dbType = dialectV2.getDbType();
            String connectionCatalog = dialectV2.getConnectionCatalog();
            String connectionSchema = dialectV2.getConnectionSchema();
            List<TableMetadata> allTableInfo = dialectV2.getAllTableInfo(
                    connectionCatalog,
                    connectionSchema,
                    tablePrefix,
                    false,
                    new String[]{"TABLE"});
            System.out.println("scan " + allTableInfo.size() + " tables");
            List<EntityInfo> entityInfos = ListTs.newList();
            Field[] fields1 = ReflectUtil.getFields(AutoAudit.class);
            List<String> collect = Arrays.stream(fields1).map(Field::getName).collect(Collectors.toList());
            for (TableMetadata tableMetadata : allTableInfo) {
                Set<String> importList = Sets.newHashSet();
                List<EntityInfo.EFieldInfo> fields = ListTs.newList();
                String tableName = tableMetadata.getTableName();
                if (StrUtil.isNotBlank(tablePrefix)) {
                    boolean b = !StrUtil.endWith(tablePrefix, "%") && !StrUtil.startWith(tablePrefix, "%");
                    if (b && !StrUtil.equals(tableName, tablePrefix)) {
                        continue;
                    }
                }

                // exclude
                List<String> exclude = dbGenSetting.getExclude();
                if (ListTs.isNotEmpty(exclude)) {
                    if (exclude.stream().anyMatch(e -> StrUtil.equalsIgnoreCase(e, tableName))) {
                        continue;
                    }
                }
                List<DatabaseColumnMetadata> columnsNoCacheQuiet = dialectV2.getColumnsNoCacheQuiet(connectionCatalog, connectionSchema, tableName);
                List<PrimaryKeyMetadata> primaryKes = dialectV2.getPrimaryKes(connectionCatalog, connectionSchema, tableName);
                Map<String, PrimaryKeyMetadata> map = ListTs.toMap(primaryKes, e -> e.getTableName() + e.getColumnName());
                columnsNoCacheQuiet.sort((o1, o2) -> {
                    Integer i1 = "YES".equals(o1.getIsAutoincrement()) ? 0 : map.get(o1.getTableName() + o1.getColumnName()) != null ? 1 : 2;
                    Integer i2 = "YES".equals(o2.getIsAutoincrement()) ? 0 : map.get(o2.getTableName() + o2.getColumnName()) != null ? 1 : 2;
                    return i1.compareTo(i2);
                });
                int autoIndex = 0;

                String schema = JavaClassNameUtils.toValidJavaClassName(StrUtil.removePrefix(
                        tableMetadata.getTableName(), this.dbGenSetting.getRemoveTablePrefix()
                ));
                boolean sameTableField = TableField.class.getSimpleName().equals(schema);
                boolean sameSchema = Schema.class.getSimpleName().equals(schema);
                for (DatabaseColumnMetadata databaseColumnMetadata : columnsNoCacheQuiet) {
                    String tableName1 = databaseColumnMetadata.getTableName();
                    if (!StrUtil.equals(tableName1, tableName)) {
                        continue;
                    }
                    Class<?> javaClassByTypeNameAndDbType = dialectV2.getJavaClassByTypeNameAndDbType(databaseColumnMetadata.getTypeName());
                    if (javaClassByTypeNameAndDbType == null) {
                        System.err.println("the 【" + tableName1 + "】-> field " + databaseColumnMetadata.getColumnName() + " vs java class is null！");
                        continue;
                    }
                    boolean needImport = false;
                    if (javaClassByTypeNameAndDbType.isArray()) {
                        Class<?> componentType = javaClassByTypeNameAndDbType.getComponentType();
                        if (!componentType.isPrimitive()) {
                            javaClassByTypeNameAndDbType = componentType;
                            needImport = true;
                        }
                    } else {
                        if (javaClassByTypeNameAndDbType.isPrimitive()) {
                            Class<?> aClass = BasicType.PRIMITIVE_WRAPPER_MAP.get(javaClassByTypeNameAndDbType);
                            if (aClass != null) javaClassByTypeNameAndDbType = aClass;
                        } else {
                            needImport = true;
                        }
                    }
                    if (needImport) {
                        String name = javaClassByTypeNameAndDbType.getName();
                        if (!name.startsWith("java.lang")) {
                            importList.add(name);
                        }
                    }
                    String columnName = databaseColumnMetadata.getColumnName();
                    String tc = tableName1 + columnName;
                    PrimaryKeyMetadata primaryKeyMetadata = map.get(tc);
                    EntityInfo.EFieldInfo eFieldInfo = new EntityInfo.EFieldInfo();
                    if (primaryKeyMetadata != null) {
                        eFieldInfo.setHasPrimaryKey(true);
                    }
                    // 纠正字符不影响 autoIndex 的比对 因为 AutoAudit里面的字段名称一定合法
                    eFieldInfo.setName(toCamelCase(validateAndCorrect(columnName.toLowerCase())));
                    eFieldInfo.setDbName(columnName);
                    eFieldInfo.setSameTableField(sameTableField);
                    eFieldInfo.setSameSchema(sameSchema);
                    eFieldInfo.setDescription(StrUtil.blankToDefault(databaseColumnMetadata.getRemarks(), columnName));
                    eFieldInfo.setType(javaClassByTypeNameAndDbType.getSimpleName());
                    eFieldInfo.setHasAutoincrement("YES".equalsIgnoreCase(databaseColumnMetadata.getIsAutoincrement()));
                    String name = JdbcType.forCode(databaseColumnMetadata.getDataType()).name();
                    eFieldInfo.setMybatisJdbcType(name);
                    if (eFieldInfo.isHasAutoincrement()) {
                        importList.add("com.baomidou.mybatisplus.annotation.IdType");
                    }

                    if (collect.stream().anyMatch(e -> StrUtil.equals(e, eFieldInfo.getName()))) {
                        autoIndex++;
                        continue;
                    }
                    fields.add(eFieldInfo);
                }
                if (fields.isEmpty()) {
                    System.out.println("the table " + tableName + " fields is empty so skip!!!");
                    continue;
                }

                EntityInfo entityInfo = new EntityInfo()
                        .setDescription(StrUtil.blankToDefault(tableMetadata.getRemarks(), schema))
                        .setSchema(schema)
                        .setSameTableField(sameTableField)
                        .setSameSchema(sameSchema)
                        .setTableName(tableName)
                        .setFieldInfoList(fields)
                        .setImportList(importList);
                BeanUtil.copyProperties(this, entityInfo, CopyOptions.create().ignoreNullValue());
                if (collect.size() <= autoIndex) {
                    importList.add(AutoAudit.class.getName());
                    entityInfo.setHasExtend(true);
                }
                entityInfos.add(entityInfo);
            }
            String packageNamePath = parsePackage(this.getParentPackageName());
            String filePath_ = this.getFilePath();
            List<String> entityLines = ListTs.newList();
            List<String> mapperXmlLines = ListTs.newList();
            List<String> mapperLines = ListTs.newList();
            List<String> serviceLines = ListTs.newList();
            List<String> finalLine5 = ListTs.newList();
            List<String> finalLine6 = ListTs.newList();
            List<String> finalLine7 = ListTs.newList();
            List<String> finalLine8 = ListTs.newList();
            PreviewRes.PInfo EntityPInfo = new PreviewRes.PInfo("Entity");
            PreviewRes.PInfo MapperXmlPInfo = new PreviewRes.PInfo("MapperXml");
            PreviewRes.PInfo MapperPInfo = new PreviewRes.PInfo("Mapper");
            PreviewRes.PInfo ServicePInfo = new PreviewRes.PInfo("Service");
            PreviewRes.PInfo ServiceImplPInfo = new PreviewRes.PInfo("ServiceImpl");
            PreviewRes.PInfo ControllerPInfo = new PreviewRes.PInfo("Controller");
            PreviewRes.PInfo ControllerReqPInfo = new PreviewRes.PInfo("ControllerReq");
            PreviewRes.PInfo DtoPInfo = new PreviewRes.PInfo("Dto");
            PreviewRes.PInfo MapStruct = new PreviewRes.PInfo("MapStruct");
            for (EntityInfo entityInfo : entityInfos) {
                String tableName = entityInfo.getTableName();
                if (dbGenSetting.isGenEntity()) {
                    String fileName = entityInfo.getSchema() + ".java";
                    String filePath = filePath_ + File.separator + SRC_MAIN_JAVA + File.separator + packageNamePath + File.separator + getEntityPackageName() + File.separator + fileName;
                    String s = loadTemplate(filePath, "temp", "EntityGen.ftl", entityInfo, isPreview);
                    entityLines.add(s);
                    EntityPInfo.add(fileName, s);
                }
                if (dbGenSetting.isGenMapperXml()) {
                    String fileName = entityInfo.getSchema() + "Mapper.xml";
                    String filePath = filePath_ + File.separator + SRC_MAIN_RESOURCE + File.separator + this.getMapperXmlPackageName() + File.separator + dbType + File.separator + fileName;
                    String s = loadTemplate(filePath, "temp", "MapperXmlGen.ftl", entityInfo, isPreview);
                    mapperXmlLines.add(s);
                    MapperXmlPInfo.add(fileName, s);
                }
                if (dbGenSetting.isGenMapper()) {
                    String fileName = entityInfo.getSchema() + "Mapper.java";
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(this.getMapperPackageName())
                            , fileName);
                    String s = loadTemplate(s1, "temp", "MapperGen.ftl", entityInfo, isPreview);
                    mapperLines.add(s);
                    MapperPInfo.add(fileName, s);
                }
                if (dbGenSetting.isGenService()) {
                    EntityInfo entityInfo1 = BeanUtil.copyProperties(entityInfo, EntityInfo.class);
                    entityInfo1.setCnDesc(entityInfo.getDescription());
                    entityInfo1.setEntityName(entityInfo.getSchema());
                    entityInfo1.setReturnDtoName(entityInfo.getSchema() + "Dto");
                    entityInfo1.setDomainName(entityInfo.getSchema());
                    String fileName = "I" + entityInfo1.getDomainName() + "Service.java";
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(getServiceInterfacePackageName())
                            , fileName);
                    String s = loadTemplate(s1, "temp", "IServiceGen.ftl", entityInfo1, isPreview);
                    serviceLines.add(s);
                    ServicePInfo.add(fileName, s);
                }
                if (dbGenSetting.isGenServiceImpl()) {
                    EntityInfo entityInfo1 = BeanUtil.copyProperties(entityInfo, EntityInfo.class);
                    entityInfo1.setCnDesc(entityInfo.getDescription());
                    entityInfo1.setEntityName(entityInfo.getSchema());
                    entityInfo1.setReturnDtoName(entityInfo.getSchema() + "Dto");
                    entityInfo1.setDomainName(entityInfo.getSchema());
                    String fileName = entityInfo1.getDomainName() + "ServiceImpl.java";
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(getServiceImplPackageName())
                            , fileName);
                    String s = loadTemplate(s1, "temp", "ServiceImplGen.ftl", entityInfo1, isPreview);
                    finalLine5.add(s);
                    ServiceImplPInfo.add(fileName, s);
                }
                if (dbGenSetting.isGenController()) {
                    EntityInfo entityInfo1 = BeanUtil.copyProperties(entityInfo, EntityInfo.class);
                    String fileName = entityInfo1.getSchema() + "Controller.java";
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(this.getControllerPackageName())
                            , fileName);
                    entityInfo1.setCnDesc(StrUtil.blankToDefault(entityInfo.getDescription(), tableName));
                    entityInfo1.setEntityName(entityInfo.getSchema());
                    entityInfo1.setReturnDtoName(entityInfo.getSchema() + "Dto");
                    entityInfo1.setDomainName(entityInfo.getSchema());

                    String s = loadTemplate(s1, "temp", "ControllerGen.ftl", entityInfo1, isPreview);
                    finalLine6.add(s);
                    ControllerPInfo.add(fileName, s);
                }
                if (dbGenSetting.isGenControllerReq()) {
                    EntityInfo entityInfo1 = BeanUtil.copyProperties(entityInfo, EntityInfo.class);
                    String fileName = entityInfo1.getSchema() + "ControllerReq.java";
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(this.getControllerReqPackageName())
                            , fileName);

                    entityInfo1.setCnDesc(StrUtil.blankToDefault(entityInfo.getDescription(), tableName));
                    entityInfo1.setEntityName(entityInfo.getSchema());
                    entityInfo1.setReturnDtoName(entityInfo.getSchema() + "Dto");
                    entityInfo1.setDomainName(entityInfo.getSchema());

                    String s = loadTemplate(s1, "temp", "ControllerReqGen.ftl", entityInfo1, isPreview);
                    finalLine7.add(s);
                    ControllerReqPInfo.add(fileName, s);
                }

                if (dbGenSetting.isGenDto()) {
                    EntityInfo entityInfo1 = BeanUtil.copyProperties(entityInfo, EntityInfo.class);

                    entityInfo1.setSchema(entityInfo.getSchema() + "Dto");
                    String fileName = entityInfo1.getSchema() + ".java";
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(this.getDtoPackageName())
                            , fileName);

                    entityInfo1.setCnDesc(entityInfo.getDescription());
                    entityInfo1.setEntityName(entityInfo.getSchema());
                    entityInfo1.setReturnDtoName(entityInfo.getSchema());
                    entityInfo1.setDomainName(entityInfo.getSchema());
                    String s = loadTemplate(s1, "temp", "DtoGen.ftl", entityInfo1, isPreview);
                    finalLine8.add(s);
                    DtoPInfo.add(fileName, s);
                }
                ListTs.addAll(finalList, entityLines);
                ListTs.addAll(finalList, mapperXmlLines);
                ListTs.addAll(finalList, mapperLines);
                ListTs.addAll(finalList, serviceLines);
                ListTs.addAll(finalList, finalLine5);
                ListTs.addAll(finalList, finalLine6);
                ListTs.addAll(finalList, finalLine7);
                ListTs.addAll(finalList, finalLine8);
            }
            // gen mapstruct
            if (!entityInfos.isEmpty()) {
                if (dbGenSetting.isGenMapStruct()) {
                    Map<String, Object> params = new MSGen(this, dbGenSetting, entityInfos)
                            .getParams();
                    if (params != null) {
                        String fileName = this.getMapperStructClassSimpleName() + ".java";
                        String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                                , packageNamePath
                                , parsePackage(this.getMapperStructPackageName())
                                , fileName);
                        String s = loadTemplate(s1, "temp", "mapstruct2.ftl", params, isPreview);
                        finalList.add(s);
                        MapStruct.add(fileName, s);
                    }
                }
            }


            previewRes.add(EntityPInfo);
            previewRes.add(MapperXmlPInfo);
            previewRes.add(MapperPInfo);
            previewRes.add(ServicePInfo);
            previewRes.add(ServiceImplPInfo);
            previewRes.add(ControllerPInfo);
            previewRes.add(ControllerReqPInfo);
            previewRes.add(DtoPInfo);
            previewRes.add(MapStruct);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.closeConnection(quietConnection);
            if (!isServer) {
                GlobalPruneTimer.INSTANCE.shutdown();
            }
        }
        if (objectValue != null) objectValue.setObject(previewRes);
        return String.join("\n", finalList);
    }


    String toCamelCase(String name) {
        boolean startWtih_ = StrUtil.startWith(name, "_");
        boolean endWtih_ = StrUtil.endWith(name, "_");
        String camelCase = StrUtil.toCamelCase(name);
        if (startWtih_) {
            camelCase = "_" + camelCase;
        }
        if (endWtih_) camelCase = camelCase + "_";
        return camelCase;

    }

}
