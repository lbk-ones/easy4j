ej-spring-boot-starter模块的使用说明
---

# 包结构
```
your-service/
├── src/main/java/
│   └── your/service/
│       ├── YourServiceApp.java           # 启动类（使用 @Easy4JStarter）
│       ├── controller/                   # 控制层
│       │   └── YourController.java       # @RestController
│       ├── service/                      # 业务逻辑层
│       │   ├── YourService.java          # 接口
│       │   └── impl/
│       │       └── YourServiceImpl.java   # 实现（@Service）
│       ├── mapper/                       # 数据访问层
│       │   └── YourMapper.java           # @Mapper（MyBatis-Plus）
│       ├── domains/                      # 领域模型
│       │   └── YourEntity.java           # @Entity 或 @TableName
│       ├── dto/                          # 数据传输对象
│       │   └── YourDto.java              # 传输数据用
│       └── api/                          # 远程调用接口（Dubbo/Feign）
│           └── YourApi.java              # @FeignClient 或 Dubbo 服务
├── pom.xml
└── src/main/resources/
    └── application-dev.properties        # 开发环境配置（可选）
```
# 启动类
```java
@Easy4JStarter
@EnableCodeGen
@EnableEasy4jSentinelResource
public class Application
{
    public static void main( String[] args )
    {
        SpringApplication.run(Application.class,args);
    }
}

```
- Easy4JStarter 启动注解
- EnableCodeGen 启用代码生成功能，加入之后，访问 http://127.0.0.1:${server.port}/e4j/cg/index.html 可以访问代码生成页面
- EnableEasy4jSentinelResource 启用限流功能：可以使用@SentinelResource注解进行限流

# pom 依赖
```pom
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.15</version>
        <relativePath/>
    </parent>
    <groupId>${groupId}</groupId>
    <artifactId>${artifactId}</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>${name}</name>
    <url>http://maven.apache.org</url>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.compiler.compilerVersion>17</maven.compiler.compilerVersion>
        <ej.version>2.1.2</ej.version>
    </properties>


    <dependencies>
        <dependency>
            <groupId>io.github.lbkones</groupId>
            <artifactId>ej-spring-boot-starter</artifactId>
            <version>${ej.version}</version>
        </dependency>

        <dependency>
            <groupId>io.github.lbkones</groupId>
            <artifactId>ej-nacos-client-3.X</artifactId>
            <version>${ej.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-launcher</artifactId>
            <scope>test</scope>
        </dependency>


    </dependencies>
    <build>
        
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>1.18.30</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>1.5.5.Final</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>


    </build>
</project>
```
# 使用NACO做配置中心
如果使用配置中心application.properties配置如下
```properties
easy4j.server-name=xxx
easy4j.nacos-url=${NACOS_ADDR}@${NACOS_USERNAME}:${NACOS_PASSWORD}
# namespace
easy4j.nacos-name-space=${NACOS_NAMESPACE}
# group
easy4j.nacos-group=xxx
easy4j.data-ids=xxx-${ENVIRONMENT}.properties
```
> - ${} 代表从环境变量中读取
> - 需要引入ej-nacos-client-3.X坐标

# Controller规则

```java
package cqsk.adminui.be.controller.bus.ops;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import cqsk.adminui.be.controller.req.bus.ops.LoginLogsControllerReq;
import cqsk.adminui.be.dto.bus.ops.LoginLogsDto;
import cqsk.adminui.be.service.bus.ops.ILoginLogsService;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.knife4j.ControllerModule;
import easy4j.infra.knife4j.GlobalApiResponses;
import easy4j.infra.knife4j.GlobalXAccessToken;
import easy4j.infra.log.RequestLog;
import easy4j.module.idempotent.WebIdempotent;
import easy4j.module.mybatisplus.base.EasyPageRes;
import easy4j.module.sauth.annotations.NoLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;


/**
 * no desc
 * <p/>
 * @author bokun
 * @since 2026年6月29日 11:07:36
 */
@RestController
@RequestMapping(LoginLogsController.LOGINLOGS_URL)
@ControllerModule(name = LoginLogsController.LOGINLOGS_URL, description = "登录日志表")// api文档分组的可以没有
@Tag(name = "登录日志表", description = "登录日志表相关查询和操作，不需要的接口不用管")
public class LoginLogsController {

    public static final String LOGINLOGS_URL = "/bus/ops/loginlogs";

    @Resource
    ILoginLogsService iLoginLogsService;

    @Operation(summary = "登录日志表分页查询", description = "登录日志表分页查询，不需要该功能则不理会")
    @PostMapping("pageQueryLoginLogs")
    @SentinelResource(value = LOGINLOGS_URL + "-pageQueryLoginLogs")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为map",
            content = @Content(
                    schema = @Schema(
                            implementation = EasyPageRes.class
                    )
            )
    )
    public EasyResult<EasyPageRes> pageQueryLoginLogs(@RequestBody LoginLogsControllerReq loginLogsControllerReq) {
        return EasyResult.ok(iLoginLogsService.pageQueryLoginLogs(loginLogsControllerReq));
    }


    @Operation(summary = "登录日志表编辑", description = "根据主键编辑或者批量编辑登录日志表，不需要该功能则不理会")
    @RequestLog
    @WebIdempotent
    @PutMapping("updateLoginLogs")
    @SentinelResource(value = LOGINLOGS_URL + "-updateLoginLogs")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为集合",
            content = @Content(
                    schema = @Schema(
                            implementation = LoginLogsDto.class
                    )
            )
    )
    public EasyResult<List<LoginLogsDto>> updateLoginLogs(@RequestBody LoginLogsControllerReq loginLogsControllerReqs) {
        return EasyResult.ok(iLoginLogsService.batchUpdateLoginLogs(loginLogsControllerReqs));
    }


    @Operation(summary = "登录日志表复制", description = "复制已有登录日志表生成新的草稿流程(传入集合可批量操作)，不需要该功能则不理会")
    @RequestLog
    @WebIdempotent
    @PostMapping("copyLoginLogs")
    @SentinelResource(value = LOGINLOGS_URL + "-copyLoginLogs")
    @GlobalXAccessToken
    @GlobalApiResponses
    @ApiResponse(
            responseCode = "data",
            description = "返回data类型为集合",
            content = @Content(
                    schema = @Schema(
                            implementation = LoginLogsDto.class
                    )
            )
    )
    @NoLogin
    public EasyResult<List<LoginLogsDto>> copyLoginLogs(@RequestBody LoginLogsControllerReq loginLogsControllerReqs) {
        return EasyResult.ok(iLoginLogsService.copyLoginLogs(loginLogsControllerReqs));
    }
}

```
- @Operation API文档注解 summary 简称 description 描述
- @RequestLog 请求日志注解加入之后http请求日志会自动记录到数据库去（查询类接口一般不加入这个）
- @WebIdempotent 幂等注解，加入之后会自动对接口进行幂等化,自动以X-Access-Token进行幂等，如果没有则降级为全局幂等（查询类接口一般不加入这个）
- @PostMapping SpringMvc注解
- @SentinelResource sentinel注解，加上之后可以对接口进行限流和熔断，要在启动类上加入@EnableEasy4jSentinelResource才会生效
- @GlobalXAccessToken knife4j中的调试注解，加上之后访问接口必须要携带X-Access-Token
- @GlobalApiResponses knife4j中描述访问的相应结构体
- @ApiResponse knife4j中描述自定义响应体
- @NoLogin 在类或者接口上加上这个注解，代表这个类的所有接口或者加了注解的接口可以不用登录就可以进行访问

# Service接口
```java
package your.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import your.service.domains.YourEntity;

public interface YourService extends IService<YourEntity> {
    // 自定义业务方法
    YourEntity getByName(String name);
}
```
# Service实现类


```java
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.module.mybatisplus.base.BaseServiceImpl;
import easy4j.module.mybatisplus.base.EQueryWrapper;
import easy4j.module.mybatisplus.base.EasyPageRes;
import easy4j.module.mybatisplus.base.PageDto;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.exception.EasyException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import java.util.Arrays;
import java.util.Objects;


/**
 * 实现类Demo
 */
@Service
public class DemoServiceImpl extends BaseServiceImpl<DemoMapper, Demo> implements IDemoService {

    // 创建时间
    private static final String CREATE_TIME = "createTime";
    // 是否启用
    private static final String IS_ENABLED = "isEnabled";
    // 第一位有效，第二位无效
    private static final Object[] IS_ENABLED_VALID = new Object[]{1,0};
    // 删除
    private static final String IS_DELETED = "isDeleted";
    // 第一位有效，第二位无效
    private static final Object[] IS_DELETED_VALID = new Object[]{0,1};


    @Override
    public EasyPageRes pageQueryDemo(DemoControllerReq req) {
        CheckUtils.checkByLambda(req, DemoControllerReq::getPageQuery);
        PageDto pageQuery = req.getPageQuery();
        List<List<Object>> keys = pageQuery.getKeys();
        EQueryWrapper<Demo> objectEQueryWrapper = new EQueryWrapper<>();
        parseKeysToQuery(keys, objectEQueryWrapper);
        boolean isDeleted = ReflectUtil.hasField(Demo.class, IS_DELETED);
        if(isDeleted){
            objectEQueryWrapper.eq(IS_DELETED,IS_DELETED_VALID[0]);
        }
        boolean b = ReflectUtil.hasField(Demo.class, CREATE_TIME);
        if(b){
            objectEQueryWrapper.orderByDesc(CREATE_TIME);
        } else{
            List<FieldInfo> primaryKeyName = getPrimaryKeyName(Demo.class);
            FieldInfo fieldInfo = ListTs.get(primaryKeyName, 0);
            if(null != fieldInfo){
                objectEQueryWrapper.orderByDesc(fieldInfo.getFieldName());
            }
        }
        Page<Demo> page = page(new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize()),objectEQueryWrapper);
        EasyPageRes from = EasyPageRes.from(page);
        List<Demo> records = from.getRecords(Demo.class);
        List<DemoDto> dtos = listDemoToDto(records);
        return from.setRecords(dtos);
    }

    public List<DemoDto> listDemoToDto(List<Demo> list) {
        List<DemoDto> collect = list.stream()
                .map(MapperStruct.instance::toDemoDto)
                .collect(Collectors.toList());

        return collect;
    }

    public List<Demo> listDemoDtoToDomain(List<DemoDto> list) {
        return list.stream()
                .map(MapperStruct.instance::toDemo)
                .collect(Collectors.toList());
    }

    @Override
    public List<DemoDto> getAllEnableNotDelete() {
        EQueryWrapper<Demo> query = new EQueryWrapper<>(Demo.class);
        boolean b = ReflectUtil.hasField(Demo.class, IS_ENABLED);
        boolean b2 = ReflectUtil.hasField(Demo.class, IS_DELETED);
        if (b) {
            query.eq(IS_ENABLED, IS_ENABLED_VALID[0]);
        }
        if (b2) {
            query.eq(IS_DELETED, IS_DELETED_VALID[0]);
        }
        List<Demo> domainList = this.getBaseMapper().selectList(query);
        return listDemoToDto(domainList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DemoDto> saveDemo(DemoControllerReq req) {
        CheckUtils.checkByLambda(req, DemoControllerReq::getDemoDtos);
        List<DemoDto> dtoList = req.getDemoDtos();
        List<Demo> newInsert = listDemoDtoToDomain(dtoList);

        if (!newInsert.isEmpty()) {
            try{
                CheckUtils.checkInsert(saveBatch(newInsert),"Demo");
            }catch(DuplicateKeyException exception){
                throw new EasyException(BusCode.A00065);
            }
        }
        return listDemoToDto(newInsert);
    }


    @Override
    public List<DemoDto> getDemoByIds(List<String> strings) {
        if (ListTs.isEmpty(strings)) return new ArrayList<>();
        List<List<String>> partition = ListTs.partition(strings, 100);
        List<Demo> domainList = ListTs.newList();
        boolean b2 = ReflectUtil.hasField(Demo.class, IS_DELETED);
        List<FieldInfo> primaryKeyName = getPrimaryKeyName(Demo.class);
        for (List<String> pList : partition) {
            EQueryWrapper<Demo> query = new EQueryWrapper<>(Demo.class);
            if (primaryKeyName != null && !primaryKeyName.isEmpty()) {
                FieldInfo fieldInfo = primaryKeyName.get(0);
                List<Object> inList = convertPrimaryKey(pList, fieldInfo);
                if(inList != null && !inList.isEmpty()){
                    query.in(StrUtil.toUnderlineCase(fieldInfo.getFieldName()), inList);
                    if(b2) query.eq(IS_DELETED, IS_DELETED_VALID[0]);
                    List<Demo> queryList = this.getBaseMapper().selectList(query);
                    ListTs.addAll(domainList, queryList);
                }
            }
        }
        return listDemoToDto(domainList);
    }

    @Override
    public List<DemoDto> publishDemos(DemoControllerReq req) {
        CheckUtils.checkByLambda(req,DemoControllerReq::getDemoDtos);
        // TODO
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DemoDto> deleteDemos(List<String> ids) {
        CheckUtils.checkParamNotNull(ids,"ids");
        List<DemoDto> queryResList = getDemoByIds(ids);
        if (ListTs.isNotEmpty(queryResList)) {
            boolean b = ReflectUtil.hasField(queryResList.get(0).getClass(), IS_DELETED);
            if(b){
                for (DemoDto dtoItem : queryResList) {
                    ReflectUtil.setFieldValue(dtoItem, IS_DELETED,IS_DELETED_VALID[1]);
                }
                DemoControllerReq req = new DemoControllerReq();
                req.setDemoDtos(queryResList);
                return batchUpdateDemo(req);
            }else{
                for (DemoDto item : queryResList) {
                    this.getBaseMapper().deleteById(item);
                }
            }
        }
        return ListTs.newList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DemoDto> batchUpdateDemo(DemoControllerReq req) {
        CheckUtils.checkByLambda(req,DemoControllerReq::getDemoDtos);
        List<DemoDto> dtos = req.getDemoDtos();
        List<Demo> domainList = listDemoDtoToDomain(dtos);
        DemoMapper baseMapper1 = this.getBaseMapper();
        List<String> ids = ListTs.newList();
        if(ListTs.isEmpty(getDemoByIds(ListTs.mapToList(domainList, this::getIdValueToStr)))){
            throw new EasyException(BusCode.A00012);
        }
        for (Demo domain : domainList){
            clearAudit(domain);
            try{
                baseMapper1.updateById(domain);
            }catch (DuplicateKeyException e){
                throw new EasyException(BusCode.A00065);
            }
            String id = getIdValueToStr(domain);
            if(StrUtil.isNotBlank(id)){
                ids.add(id);
            }
        }
        return getDemoByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DemoDto> copyDemo(DemoControllerReq req) {
        CheckUtils.checkByLambda(req,DemoControllerReq::getDemoDtos);
        List<DemoDto> domainDtos = req.getDemoDtos();
        List<String> ids = listDemoDtoToDomain(domainDtos)
                .stream()
                .map(this::getIdValueToStr)
                .collect(Collectors.toList());
        domainDtos = getDemoByIds(ids);
        List<Demo> domainList = listDemoDtoToDomain(domainDtos);
        domainList.forEach(this::clearId);
        this.patchPrimaryKeys(domainList,Demo.class);
        req.setDemoDtos(listDemoToDto(domainList));
        return saveDemo(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DemoDto> enableOrDisableDemo(DemoControllerReq req) {
        CheckUtils.checkByLambda(req,DemoControllerReq::getDemoDtos);
        List<String> collect = listDemoDtoToDomain(req.getDemoDtos())
                .stream().map(this::getIdValueToStr).collect(Collectors.toList());
        List<DemoDto> queryResList = getDemoByIds(collect);
        if (ListTs.isNotEmpty(queryResList)) {
            boolean b = ReflectUtil.hasField(queryResList.get(0).getClass(), IS_ENABLED);
            if(b){
                for (DemoDto item : queryResList) {
                    Arrays.stream(IS_ENABLED_VALID)
                            .filter(e -> !Objects.equals(ReflectUtil.getFieldValue(item, IS_ENABLED), e))
                            .findFirst()
                            .ifPresent(o -> ReflectUtil.setFieldValue(item, IS_ENABLED, o));
                }
                DemoControllerReq newReq = new DemoControllerReq();
                newReq.setDemoDtos(queryResList);
                return batchUpdateDemo(newReq);
            }
        }
        return ListTs.newList();
    }
}
```

**说明**：
- `@Service` 注解标记服务层 Bean
- 继承 `BaseServiceImpl<M, T>` 获得默认 CRUD 方法
- `this.getBaseMapper().lambdaQuery()` 提供类型安全的查询 API
- CheckUtils.checkByLambda 检查参数中的字段

# Domain实体
```java
package your.service.domains;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("YOUR_TABLE")
public class YourEntity {
    @TableId
    private String id;           // 主键
    private String name;         // 字段名称
    private Integer status;      // 状态（0：禁用，1：启用）
    private Long createTime;     // 创建时间
}
```

**注解说明**：
- `@Data` - Lombok 注解，自动生成 getter/setter/equals/hashCode/toString
- `@TableName` - MyBatis-Plus 注解，指定数据库表名
- `@TableId` - MyBatis-Plus 注解，标记主键字段

# Mapper接口
```java
package your.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import your.service.domains.YourEntity;

@Mapper
public interface YourMapper extends BaseMapper<YourEntity> {
    // MyBatis-Plus 提供了默认的 CRUD 方法
    // 如需自定义方法，可在此添加
}
```

**说明**：
- 继承 `BaseMapper<T>` 可获得完整的 CRUD 方法
- `@Mapper` 注解注册 Bean

# MapperXML规则
> mapperXML的目录为classpath:mappers/{对应的数据库名称比如mysql}/**/*.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="xxx">
    <resultMap id="BaseResultMap" type="xxx.domains.xxx">
        <id column="IDE_KEY" jdbcType="VARCHAR" property="ideKey" />
        <result column="EXPIRE_DATE" jdbcType="TIMESTAMP" property="expireDate" />
    </resultMap>
</mapper>
```

# 统一返回体

所有接口必须使用 `EasyResult<T>` 作为返回值：

```java
// ✅ 正确
@GetMapping("/{id}")
public EasyResult<YourEntity> get(@PathVariable String id) {
    YourEntity entity = yourService.getById(id);
    return EasyResult.ok(entity);
}

// ✅ 返回简单数据
@PostMapping
public EasyResult<Boolean> add(@RequestBody YourEntity entity) {
    boolean success = yourService.save(entity);
    return EasyResult.ok(success);
}

// ✅ 返回集合
@GetMapping
public EasyResult<List<YourEntity>> list() {
    List<YourEntity> list = yourService.list();
    return EasyResult.ok(list);
}

// ✅ 返回分页数据
@GetMapping("/page")
public EasyResult<Page<YourEntity>> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<YourEntity> page = new Page<>(pageNum, pageSize);
    return EasyResult.ok(yourService.page(page));
}

// ❌ 错误 - 直接返回实体
@GetMapping("/{id}")
public YourEntity get(@PathVariable String id) {
    return yourService.getById(id);  // 不符合规范
}
```

# 参数验证

使用 `CheckUtils` 进行参数校验：

```java
import easy4j.infra.common.header.CheckUtils;
public void test(){
    // 检查单个字段非空
    CheckUtils.checkByLambda(entity, YourEntity::getId, YourEntity::getName);

    // 检查对象非空
    CheckUtils.checkObjIsNull(entity, "E00001", "实体不存在");

    // 业务异常抛出
    if (!condition) {
        throw new EasyException("E00002", "业务异常描述");
    }
}

```
# 异常处理

所有业务异常使用统一的 `EasyException`：

```java
import easy4j.infra.common.exception.EasyException;

@Service
public class YourServiceImpl {
    
    public void updateEntity(String id, YourEntity entity) {
        YourEntity existing = this.getById(id);
        
        // 业务验证
        if (existing == null) {
            throw new EasyException("E00001", "记录不存在");
        }
        
        if (existing.getStatus() == 0) {
            throw new EasyException("E00002", "记录已禁用");
        }
        
        this.updateById(entity);
    }
}
```

# 国际化(i18n)
> - src/main/resources/i18n/messages_zh_CN.properties 中文码
> - src/main/resources/i18n/messages_en_US.properties 英文码
> - src/main/resources/i18n/messages.properties 默认的码

```properties
B00001=业务[{0}]异常：{0}
B00002=任务{0}不存在
B00003=用户已禁用
B00005=密码不符合规则，必须满足至少 8 位长度，并且满足大写字母、小写字母、数字、符号这四种因子中的至少三种
B10001=请求参数不能为空
```

# 部分工具如下
- easy4j.infra.base.starter.env.Easy4j            系统参数或者配置中心远程参数获取
- easy4j.module.sauth.core.Easy4jAuth             系统权限相关操作，认证、鉴权、获取用户信息等
- easy4j.infra.dbaccess.dialect.v2.DialectV2      不同数据库方言相关内容获取
- easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL 动态DDL工具（建表，逆向相关）
- easy4j.infra.dbaccess.dynamic.DynamicTableQuery 动态表查询
- easy4j.infra.dbaccess.DBAccess                  摆脱上层orm框架，直连其他数据库的orm框架
- easy4j.infra.dbaccess.TempDataSource            临时数据源实现，每次都拿取连接
- easy4j.infra.common.utils.ThreadPoolUtils       线程池工具
- easy4j.infra.common.utils.SP                    字符串常量比如逗号句号什么的
- easy4j.infra.common.utils.ServiceLoaderUtils    java拿取SPI工具集合
- easy4j.infra.common.utils.ListTs                集合工具集
- easy4j.infra.common.utils.EasyMap               Map工具
- easy4j.infra.common.utils.BusCode               系统使用的i18n字段
- easy4j.infra.common.utils.lambda.EasyLambda     lambda工具集
- easy4j.infra.common.utils.json.JacksonUtil      json工具类
- easy4j.infra.common.utils.xml.JacksonXmlUtil    xml工具类（之前代码里面的xml工具全部废掉，用这个）
- easy4j.infra.common.utils.ObjectHolder          单例类
- easy4j.infra.common.header.EasyResult           通用返回实体（带泛型）
- easy4j.infra.common.i18n.I18nUtils              i18n工具集
- easy4j.infra.common.header.CheckUtils           参数检查工具
- easy4j.infra.common.exception.EasyException     通用异常（所有业务异常都抛它i18n也是一样）
- easy4j.infra.common.utils.EStopWatch            步进器（StopWatch的改良版本）
- easy4j.infra.log.DbLog                          数据库日志工具
- easy4j.module.mapstruct.TransferMapper          如果MapStruct不使用 ConversionService 那么就用这个
- easy4j.module.redis.RedisCacheWithFallback      redis缓存查询降级
- easy4j.module.seed.CommonKey                    雪花算法主键生成器
- easy4j.module.mybatisplus.IdGenner              mybatisplus版本的主键生成
- easy4j.module.seed.leaf.LeafGenIdService        leaf主键生成器
- easy4j.infra.context.api.lock.RedissonLock      redis分布式锁（如果使用了redis的话）
- easy4j.infra.context.api.lock.DbLock            数据库分布式锁
- easy4j.infra.context.Easy4jContext              上下文获取工具
- cn.hutool.extra.spring.SpringUtil               通过代码的形式拿取bean
- cn.hutool.core.convert.Convert                  类型转换
- cn.hutool.core.date.DateUtil                    时间工具类
- cn.hutool.core.util.StrUtil                     字符串处理
- easy4j.infra.common.utils.minio.EasyMinio       MinIo工具类
- easy4j.infra.common.utils.EasyExcelUtils        Excel工具类
- easy4j.infra.common.utils.MonitoredBlockingQueue 阻塞队列监控
- easy4j.infra.common.utils.OSUtils               系统信息获取工具类
- easy4j.infra.common.utils.ServiceLoaderUtils    SPI缓存工具类
- org.springframework.cache.CacheManager          缓存管理器如果要使用缓存就用这个（有Redis和Caffeine版本）

# easy4j 框架配置参数完整说明

## 1. 基础服务配置

### 1.1 服务信息
| 参数 | 说明 | 示例值 | 类型 |
|------|------|--------|------|
| `easy4j.dev` | 开发环境标识，true时降低启动时间优化 | true/false | boolean |
| `easy4j.server-port` | 服务端口，默认8080，等同于 server.port | 8080 | int |
| `easy4j.server-name` | 服务名称，等同于 spring.application.name | demo-service | string |
| `easy4j.server-desc` | 服务描述 | "用户管理服务" | string |
| `easy4j.author` | 业务模块负责人 | "张三" | string |

### 1.2 环境配置
| 参数 | 说明 | 示例值 |
|------|------|--------|
| `easy4j.env` | 环境标识，类似 spring.profiles.active，用于nacos远程配置文件名称加后缀 | dev/test/prod |
| `easy4j.default-i18n` | 默认国际化语言，默认中文 | zh_CN |

---

## 2. 数据源与数据库配置

### 2.1 主数据源
| 参数 | 说明 | 示例值 |
|------|------|--------|
| `easy4j.data-source-url` | 数据源简写，包含数据库类型、地址、用户名和密码 | jdbc:postgresql://localhost:5432/postgres@root:123456 |

### 2.2 H2 数据库配置
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.h2-enable` | 是否启用H2数据库 | false | boolean |
| `easy4j.h2-url` | H2数据库地址 | - | string |
| `easy4j.h2-console-username` | H2控制台用户名 | easy4j | string |
| `easy4j.h2-console-password` | H2控制台密码 | easy4j | string |

### 2.3 动态数据源
| 参数 | 说明 |
|------|------|
| `easy4j.dynamic-data-source` | 动态数据源配置，支持多数据源切换 |
| `easy4j.db-access-not-cache-schema` | 是否不缓存动态表查询的schema信息，默认false（缓存） |

### 2.4 数据库日志与监控
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.enable-print-sys-db-sql` | 是否开启系统SQL日志记录 | true | boolean |
| `easy4j.db-request-log-enable` | 是否启用RequestLog注解进行请求日志收集 | true | boolean |

### 2.5 数据库迁移
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.flyway-enable` | 是否启用Flyway数据库版本管理，Linux服务器默认启用 | false | boolean |
| `easy4j.flyway-checksum-disabled` | 是否禁用Flyway启动时的脚本内容检查 | true | boolean |

---

## 3. 分布式标识与ID生成配置

### 3.1 雪花算法配置
| 参数 | 说明 | 示例值 |
|------|------|--------|
| `easy4j.seed-ip-segment` | Seed模块的雪花算法IP前缀，用于多网卡确定IP，如设置则按IP分配工作ID避免主键重复 | 10 |

**说明**: 在分布式系统中，通过指定IP前缀来确保各节点获得唯一的工作ID，防止分布式主键冲突。

---

## 4. 跨域与请求处理配置

### 4.1 跨域CORS
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.cors-reject-enable` | 是否开启全局允许跨域 | true | boolean |
| `easy4j.cors-allow-domains` | 跨域允许的域名列表，多个用逗号隔开 | - | string |

### 4.2 请求处理
| 参数 | 说明 | 默认值 |
|------|------|--------|
| `easy4j.cache-http-content-length` | 请求体缓存字节流最大大小 | 5MB |
| `easy4j.print-request-log` | 是否打印简单的请求日志 | false |

---

## 5. 认证与授权配置

### 5.1 JWT与签名密钥
| 参数 | 说明 |
|------|------|
| `easy4j.jwt-secret` | JWT签名密钥串 |
| `easy4j.signature-secret` | 签名密钥串（用于字典等敏感接口） |
| `easy4j.sign-urls` | 需要加强校验的接口清单 |

### 5.2 会话管理
| 参数 | 说明 | 默认值 |
|------|------|--------|
| `easy4j.session-expire-time-seconds` | 会话过期时间 | 3小时 |
| `easy4j.session-refresh-time-remaining` | 会话刷新剩余时间（秒） | 10分钟 |

### 5.3 简单权限认证（Simple Auth）
| 参数 | 说明 | 默认值 | 必填 |
|------|------|--------|------|
| `easy4j.simple-auth-enable` | 简单权限认证是否开启 | false | 否 |
| `easy4j.simple-auth-is-server` | 是否为权限认证服务端，是则自动建表和注册服务 | false | 否 |
| `easy4j.simple-auth-session-storage-type` | 权限session存储类型 | - | 是(db\|redis) |
| `easy4j.simple-auth-username` | 权限认证用户名 | - | 是 |
| `easy4j.simple-auth-username-cn` | 权限认证用户名中文 | - | 否 |
| `easy4j.simple-auth-password` | 权限认证密码，$开头代表从环境变量获取 | - | 是 |
| `easy4j.simple-auth-user-impl-type` | 用户信息实现类型(default\|extra) | - | 当is-server=true时必填 |
| `easy4j.simple-auth-is-cache-authority` | 权限列表是否缓存 | false | 否 |
| `easy4j.simple-auth-register-to-nacos` | 服务端是否将权限注册到nacos供远程调用 | false | 否 |
| `easy4j.simple-auth-scan-package-prefix` | 权限扫描包名前缀 | 启动类所在包 | 否 |
| `easy4j.simple-auth-session-repeat-strategy` | 认证会话重复策略 | default | 否 |
| `easy4j.simple-auth-access-tokens` | 认证会话口令集合，$开头代表从环境变量获取 | - | 否 |

#### 会话重复策略说明
- `default/public`: 共用会话
- `new`: 新建会话
- `reject`: 不允许重复登录
- `kick`: 把已存在的会话踢下线

### 5.4 Cookie配置（用于Token携带）
| 参数 | 说明 | 默认值 |
|------|------|--------|
| `easy4j.simple-auth-token-use-cookie` | 是否使用Cookie而非Header携带token | false |
| `easy4j.simple-auth-token-use-cookie-httponly` | Cookie是否使用HttpOnly模式 | false |
| `easy4j.simple-auth-token-use-cookie-secure` | Cookie是否使用Secure模式（强制HTTPS） | false |
| `easy4j.simple-auth-token-use-cookie-domain` | Cookie是否设置domain | false |
| `easy4j.simple-auth-token-use-cookie-path` | Cookie的path属性 | / |
| `easy4j.simple-auth-token-use-cookie-same-site` | Cookie的SameSite策略 | Lax |

---

## 6. Redis缓存配置

### 6.1 Redis连接
| 参数 | 说明 | 示例值 | 类型 |
|------|------|--------|------|
| `easy4j.redis-enable` | 是否启用Redis，配置了url则自动启用 | true/false | boolean |
| `easy4j.redis-server-url` | Redis服务器地址，格式：host:port@[username:]password | 127.0.0.1:6379@user:123456 | string |
| `easy4j.redis-connection-type` | Redis连接方式 | Single | Single/Sentinel/Cluster |

### 6.2 Redis连接池
| 参数 | 说明 | 默认值 |
|------|------|--------|
| `easy4j.redis-min-ide-size` | Redis最小空闲连接数 | 30 |
| `easy4j.redis-connection-pool-size` | Redis连接池最大连接数 | 500 |

---

## 7. 服务通信与注册中心配置

### 7.1 Nacos配置中心
| 参数 | 说明 | 示例值 |
|------|------|--------|
| `easy4j.nacos-config-url` | Nacos配置中心地址 | localhost:8848 |
| `easy4j.nacos-config-username` | Nacos配置中心用户名 | - |
| `easy4j.nacos-config-password` | Nacos配置中心密码 | - |
| `easy4j.nacos-config-group` | Nacos配置中心Group | DEFAULT_GROUP |
| `easy4j.nacos-config-namespace` | Nacos配置中心命名空间 | - |
| `easy4j.nacos-config-strict` | 配置中心严格模式 | false |
| `easy4j.nacos-config-file-extension` | 远程配置文件后缀 | properties |
| `easy4j.data-ids` | 配置中心data-ids列表，多个逗号隔开，不同组则 data-id?group=XXX_GROUP | - |

### 7.2 Nacos注册中心
| 参数 | 说明 | 示例值 |
|------|------|--------|
| `easy4j.nacos-discovery-url` | Nacos注册中心地址 | localhost:8848 |
| `easy4j.nacos-discovery-username` | Nacos注册中心用户名 | - |
| `easy4j.nacos-discovery-password` | Nacos注册中心密码 | - |
| `easy4j.nacos-discovery-group` | Nacos注册中心Group | DEFAULT_GROUP |
| `easy4j.nacos-discovery-namespace` | Nacos注册中心命名空间 | - |

### 7.3 Nacos快捷配置
| 参数 | 说明 | 优先级 |
|------|------|--------|
| `easy4j.nacos-url` | Nacos地址快捷配置，格式：地址@username:password | 低 |
| `easy4j.nacos-group` | 通用Group配置，配置中心和注册中心同步使用 | 低 |
| `easy4j.nacos-name-space` | 通用命名空间配置，配置中心和注册中心同步使用 | 低 |

### 7.4 服务注册
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.force-register-to-registry` | 强制将本机服务注册到注册中心 | false | boolean |

---

## 8. 链路追踪与日志配置

### 8.1 链路追踪
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.simple-link-tracking` | 单服务简单链路追踪 | false | boolean |

### 8.2 日志相关
| 参数 | 说明 | 默认值 |
|------|------|--------|
| `easy4j.log-path` | 日志所在目录 | ./logs |
| `easy4j.seata-tx-log` | Seata事务日志是否整合到logback | false |

---

## 9. 限流降级与熔断配置

### 9.1 Sentinel流控与降级（非SCA架构）
| 参数 | 说明 | 类型 |
|------|------|------|
| `easy4j.sentinel-flow-count` | 每秒允许通过的请求数（QPS） | int |
| `easy4j.sentinel-flow-grade-type` | 限流模式：1=QPS、0=THREAD | int |
| `easy4j.sentinel-degrade-count` | 降级熔断阈值比例 | double |
| `easy4j.sentinel-degrade-grade-type` | 降级模式：0=平均响应时间、1=异常比例、2=异常次数 | int |
| `easy4j.sentinel-degrade-time` | 熔断时长（秒） | int |
| `easy4j.sentinel-runtime-port` | 本地服务与Sentinel控制台通讯端口 | int |

### 9.2 Sentinel控制台
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.sentinel-dashboard-enable` | 是否开启Sentinel控制台 | false | boolean |
| `easy4j.sentinel-dashboard-eager` | Sentinel控制台是否提前初始化 | true(if enabled) | boolean |
| `easy4j.sentinel-dashboard-url` | Sentinel控制台地址 | - | string |

---

## 10. Spring Cloud架构配置

### 10.1 SCA（Spring Cloud Alibaba）
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.enable-sca` | 是否启用SCA，引用sca-starter则默认开启 | false | boolean |
| `easy4j.sca-gateway-flow-qps` | Spring Cloud Gateway流控规则 | - | string |

### 10.2 Seata分布式事务
| 参数 | 说明 | 类型 |
|------|------|------|
| `easy4j.seata-enable` | 是否启用Seata分布式事务 | boolean |
| `easy4j.seata-nacos-url` | Seata注册中心地址，多个逗号隔开，格式：地址@用户:密码 | string |
| `easy4j.seata-nacos-cluster` | Seata注册中心集群名称 | string |
| `easy4j.seata-tx-group` | Seata事务组名称 | string |
| `easy4j.seata-nacos-group` | Seata注册中心Group | string |
| `easy4j.seata-registry-type` | Seata注册中心类型 | string |

---

## 11. 任务调度配置

### 11.1 XXL-Job分布式任务调度
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.xxl-job-enable` | 是否启用XXL-Job | false | boolean |
| `easy4j.xxl-job-admin-url` | XXL-Job Admin地址 | - | string |
| `easy4j.xxl-job-access-token` | XXL-Job AccessToken | default_token | string |

### 11.2 Quartz本地任务调度
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.global-quartz-job-print-log` | Quartz全局日志打印 | false | boolean |
| `easy4j.quartz-job-restart-check-delete` | 任务删除后重启是否删除触发器，默认删除 | true | boolean |

---

## 12. API文档与监控配置

### 12.1 Knife4j API文档
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.knife4j-nacos-aggregation` | Knife4j通过Nacos进行聚合 | false | boolean |
| `easy4j.knife4j-nacos-routers` | Knife4j要聚合的路由信息 | - | string |

### 12.2 Spring Boot Admin监控
| 参数 | 说明 | 类型 |
|------|------|------|
| `easy4j.admin-server-url` | Boot Admin监控地址，配置则自动开启admin-client | string |

### 12.3 Metrics指标采集
| 参数 | 说明 | 默认值 | 类型 |
|------|------|--------|------|
| `easy4j.metrics-enable` | 是否开启指标采集 | true | boolean |

---

## 13. 文件存储配置

### 13.1 MinIO对象存储
| 参数 | 说明 | 类型 |
|------|------|------|
| `easy4j.minio-url` | MinIO服务地址 | string |
| `easy4j.minio-access-key` | MinIO访问Key | string |
| `easy4j.minio-secret-key` | MinIO访问密钥 | string |

---

## 14. 代码生成与开发工具配置

### 14.1 代码生成
| 参数 | 说明 |
|------|------|
| `easy4j.code-gen` | 代码生成相关配置 |

---

## 参数值格式说明

### 布尔值
```
true | false
```

### 时间单位
```
秒(seconds) | 分钟(minutes) | 小时(hours)
```

### 端口号
```
1-65535
```

### 数据库URL格式
```
jdbc:postgresql://host:port/database@username:password
jdbc:mysql://host:port/database@username:password
```

### Redis连接格式
```
host:port@[username:]password
示例：127.0.0.1:6379@user:123456
或：127.0.0.1:6379@123456（仅密码）
```

### Nacos地址格式
```
host:port@username:password
示例：localhost:8848@admin:admin
```

---

## 配置优先级与冲突解决

1. **快捷配置优先级低**
    - `easy4j.nacos-url` < 具体的 `nacos-config-url` 和 `nacos-discovery-url`
    - `easy4j.nacos-group` 和 `easy4j.nacos-name-space` < 具体的Group和Namespace

2. **具体配置优先级高**
    - 具体的 `nacos-config-url` 会覆盖 `nacos-url`

3. **环境变量密钥**
    - 密码以 `$` 开头代表从环境变量中获取
    - 示例：`$DB_PASSWORD` 从环境变量 `DB_PASSWORD` 获取值

---

## 常见配置场景

### 场景1: 单机开发环境
```properties
easy4j.dev=true
easy4j.server-port=8080
easy4j.server-name=demo-service
easy4j.data-source-url=jdbc:mysql://localhost:3306/demo@root:123456
easy4j.h2-enable=false
easy4j.redis-enable=false
```

### 场景2: 微服务生产环境
```properties
easy4j.dev=false
easy4j.server-name=demo-service
easy4j.nacos-url=nacos.prod.com:8848@admin:$NACOS_PASSWORD
easy4j.nacos-name-space=production
easy4j.redis-server-url=redis.prod.com:6379@$REDIS_PASSWORD
easy4j.redis-connection-type=Cluster
easy4j.enable-sca=true
easy4j.seata-enable=true
```

### 场景3: 权限认证服务端
```properties
easy4j.simple-auth-enable=true
easy4j.simple-auth-is-server=true
easy4j.simple-auth-session-storage-type=redis
easy4j.simple-auth-user-impl-type=default
easy4j.simple-auth-username=admin
easy4j.simple-auth-password=$AUTH_PASSWORD
easy4j.simple-auth-register-to-nacos=true
```