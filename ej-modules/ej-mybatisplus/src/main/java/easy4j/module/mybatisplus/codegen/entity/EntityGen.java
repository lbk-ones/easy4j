package easy4j.module.mybatisplus.codegen.entity;

import cn.hutool.cache.GlobalPruneTimer;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.BasicType;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
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
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;
import org.springframework.jdbc.support.JdbcUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class EntityGen extends AbstractGen {

    private EntityConfig entityConfig;

    public EntityGen(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
    }

    public String getFilePath() {
        return this.getProjectAbsolutePath();
    }

    public String gen() {
        String tablePrefix = this.entityConfig.getTablePrefix();
        notNull(this.entityConfig, "entityDto");
        notNull(tablePrefix, "tablePrefix");
        notNull(this.entityConfig.getUrl(), "url");
        notNull(this.entityConfig.getUsername(), "userName");
        notNull(this.entityConfig.getPassword(), "passWord");
        if (
                !entityConfig.isGenEntity()
                        && !entityConfig.isGenMapperXml()
                        && !entityConfig.isGenMapper()
                        && !entityConfig.isGenService()
                        && !entityConfig.isGenServiceImpl()
                        && !entityConfig.isGenController()
                        && !entityConfig.isGenControllerReq()
        ) {
            return "skip gen file from db!";
        }
        TempDataSource tempDataSource = new TempDataSource(
                SqlType.getDriverClassNameByUrl(this.entityConfig.getUrl()),
                this.entityConfig.getUrl(),
                this.entityConfig.getUsername(),
                this.entityConfig.getPassword());
        Connection quietConnection = tempDataSource.getQuietConnection();
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
            System.out.println("scan table has " + allTableInfo.size() + " records");
            List<EntityInfo> entityInfos = ListTs.newList();
            Field[] fields1 = ReflectUtil.getFields(AutoAudit.class);
            List<String> collect = Arrays.stream(fields1).map(Field::getName).collect(Collectors.toList());
            for (TableMetadata tableMetadata : allTableInfo) {
                Set<String> importList = Sets.newHashSet();
                List<EntityInfo.EFieldInfo> fields = ListTs.newList();
                String tableName = tableMetadata.getTableName();
                boolean b = !StrUtil.endWith(tablePrefix, "%") && !StrUtil.startWith(tablePrefix, "%");
                if (b && !StrUtil.equals(tableName, tablePrefix)) {
                    continue;
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
                for (DatabaseColumnMetadata databaseColumnMetadata : columnsNoCacheQuiet) {
                    String tableName1 = databaseColumnMetadata.getTableName();
                    if (!StrUtil.equals(tableName1, tableName)) {
                        continue;
                    }
                    Class<?> javaClassByTypeNameAndDbType = dialectV2.getJavaClassByTypeNameAndDbType(databaseColumnMetadata.getTypeName());
                    if (javaClassByTypeNameAndDbType == null) {
                        System.err.println("the field " + databaseColumnMetadata.getColumnName() + " vs java class is null！");
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
                    eFieldInfo.setName(StrUtil.toCamelCase(columnName.toLowerCase()));
                    eFieldInfo.setDbName(columnName);
                    eFieldInfo.setDescription(databaseColumnMetadata.getRemarks());
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
                String schema = JavaClassNameUtils.toValidJavaClassName(StrUtil.removePrefix(
                        tableMetadata.getTableName(), this.entityConfig.getRemoveTablePrefix()
                ));
                EntityInfo entityInfo = new EntityInfo()
                        .setEntityPackageName(this.getEntityPackageName())
                        .setDescription(StrUtil.blankToDefault(tableMetadata.getRemarks(), ""))
                        .setSchema(schema)
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
            for (EntityInfo entityInfo : entityInfos) {

                if (entityConfig.isGenEntity()) {
                    String filePath = filePath_ + File.separator + SRC_MAIN_JAVA + File.separator + packageNamePath + File.separator + getEntityPackageName() + File.separator + entityInfo.getSchema() + ".java";
                    String s = loadTemplate(filePath, "temp", "EntityGen.ftl", entityInfo);
                    System.out.println("【gen entity】"+s);
                }

                if (entityConfig.isGenMapperXml()) {
                    String filePath = filePath_ + File.separator + SRC_MAIN_RESOURCE + File.separator + this.getMapperXmlPackageName() + File.separator + dbType + File.separator + entityInfo.getSchema() + "Mapper.xml";
                    String s = loadTemplate(filePath, "temp", "MapperXmlGen.ftl", entityInfo);
                    System.out.println("【gen mapper xml】"+s);
                }

                if (entityConfig.isGenMapper()) {
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(this.getMapperPackageName())
                            , entityInfo.getSchema() + "Mapper.java");
                    String s = loadTemplate(s1, "temp", "MapperGen.ftl", entityInfo);
                    System.out.println("【gen mapper】"+s);
                }

                if (entityConfig.isGenService()) {
                    EntityInfo entityInfo1 = BeanUtil.copyProperties(entityInfo,EntityInfo.class);
                    entityInfo1.setCnDesc(entityInfo.getDescription());
                    entityInfo1.setEntityName(entityInfo.getSchema());
                    entityInfo1.setReturnDtoName(entityInfo.getSchema()+"Dto");
                    entityInfo1.setDomainName(entityInfo.getSchema());
                    String s1  =  joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(getServiceInterfacePackageName())
                            , "I"+entityInfo1.getDomainName() + "Service.java");


                    String s = loadTemplate(s1, "temp", "IServiceGen.ftl", entityInfo1);
                    System.out.println("【gen service】"+s);
                }

                if (entityConfig.isGenServiceImpl()) {
                    EntityInfo entityInfo1 = BeanUtil.copyProperties(entityInfo,EntityInfo.class);
                    entityInfo1.setCnDesc(entityInfo.getDescription());
                    entityInfo1.setEntityName(entityInfo.getSchema());
                    entityInfo1.setReturnDtoName(entityInfo.getSchema()+"Dto");
                    entityInfo1.setDomainName(entityInfo.getSchema());
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(getServiceImplPackageName())
                            , entityInfo1.getDomainName() + "ServiceImpl.java");


                    String s = loadTemplate(s1, "temp", "ServiceImplGen.ftl", entityInfo1);
                    System.out.println("【gen service impl】"+s);
                }

                if (entityConfig.isGenController()) {
                    EntityInfo entityInfo1 = BeanUtil.copyProperties(entityInfo,EntityInfo.class);
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(this.getControllerPackageName())
                            , entityInfo1.getSchema() + "Controller.java");


                    entityInfo1.setCnDesc(entityInfo.getDescription());
                    entityInfo1.setEntityName(entityInfo.getSchema());
                    entityInfo1.setReturnDtoName(entityInfo.getSchema()+"Dto");
                    entityInfo1.setDomainName(entityInfo.getSchema());

                    String s = loadTemplate(s1, "temp", "ControllerGen.ftl", entityInfo1);
                    System.out.println("【gen controller】"+s);
                }

                if (entityConfig.isGenControllerReq()) {
                    EntityInfo entityInfo1 = BeanUtil.copyProperties(entityInfo,EntityInfo.class);
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(this.getControllerReqPackageName())
                            , entityInfo1.getSchema() + "ControllerReq.java");

                    entityInfo1.setCnDesc(entityInfo.getDescription());
                    entityInfo1.setEntityName(entityInfo.getSchema());
                    entityInfo1.setReturnDtoName(entityInfo.getSchema()+"Dto");
                    entityInfo1.setDomainName(entityInfo.getSchema());

                    String s = loadTemplate(s1, "temp", "ControllerReqGen.ftl", entityInfo1);
                    System.out.println("【gen controlle req】"+s);
                }

                if (entityConfig.isGenDto()) {
                    EntityInfo entityInfo1 = BeanUtil.copyProperties(entityInfo,EntityInfo.class);

                    entityInfo1.setSchema(entityInfo.getSchema()+"Dto");
                    String s1 = joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                            , packageNamePath
                            , parsePackage(this.getDtoPackageName())
                            , entityInfo1.getSchema() + ".java");

                    entityInfo1.setCnDesc(entityInfo.getDescription());
                    entityInfo1.setEntityName(entityInfo.getSchema());
                    entityInfo1.setReturnDtoName(entityInfo.getSchema());
                    entityInfo1.setDomainName(entityInfo.getSchema());
                    String s = loadTemplate(s1, "temp", "DtoGen.ftl", entityInfo1);
                    System.out.println("【gen dto】"+s);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.closeConnection(quietConnection);
            GlobalPruneTimer.INSTANCE.shutdown();
        }
        System.out.println("entity-gen successfull------>");
        return null;
    }


}
