package easy4j.module.mybatisplus.codegen.servlet;

import easy4j.module.mybatisplus.codegen.GlobalGenConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class StandRes extends GlobalGenConfig {

    /**
     * jdbc url
     */
    private String url;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

    /**
     * 要扫描的表前缀 例如： xxx_% 百分号需要保留
     */
    private String tablePrefix;

    /**
     * 排除一些表
     */
    private String exclude = "";

    /**
     * 去除表格前缀
     */
    private String removeTablePrefix;


    // 所有的表
    private List<String> allTables;


    /**
     * 是否生成mybatis mapper xml文件
     */
    private boolean genMapperXml = false;

    /**
     * 是否生成mybatis mapper 接口文件
     */
    private boolean genMapper = false;

    /**
     * 是否生成 实体
     */
    private boolean genEntity = false;


    /**
     * 是否生成 业务接口
     */
    private boolean genService = false;

    /**
     * 是否生成 业务实现
     */
    private boolean genServiceImpl = false;

    /**
     * 是否生成 接口
     */
    private boolean genController = false;

    /**
     * 是否生成 接口传参
     */
    private boolean genControllerReq = false;

    /**
     * 是否生成 to
     */
    private boolean genDto = false;


    /**
     * 是否生成 mapstruct
     */
    private boolean genMapStruct = false;

    @Override
    public StandRes setParentPackageName(String parentPackageName) {
        super.setParentPackageName(parentPackageName);
        return this;
    }

    @Override
    public StandRes setProjectAbsolutePath(String projectAbsolutePath) {
        super.setProjectAbsolutePath(projectAbsolutePath);
        return this;
    }

    @Override
    public StandRes setDeleteIfExists(boolean deleteIfExists) {
        super.setDeleteIfExists(deleteIfExists);
        return this;
    }

    @Override
    public StandRes setHeaderDesc(String headerDesc) {
        super.setHeaderDesc(headerDesc);
        return this;
    }

    @Override
    public StandRes setAuthor(String author) {
        super.setAuthor(author);
        return this;
    }

    @Override
    public StandRes setForceDelete(boolean forceDelete) {
        super.setForceDelete(forceDelete);
        return this;
    }

    @Override
    public StandRes setEntityPackageName(String entityPackageName) {
        super.setEntityPackageName(entityPackageName);
        return this;
    }

    @Override
    public StandRes setControllerPackageName(String controllerPackageName) {
        super.setControllerPackageName(controllerPackageName);
        return this;
    }

    @Override
    public StandRes setControllerReqPackageName(String controllerReqPackageName) {
        super.setControllerReqPackageName(controllerReqPackageName);
        return this;
    }

    @Override
    public StandRes setDtoPackageName(String dtoPackageName) {
        super.setDtoPackageName(dtoPackageName);
        return this;
    }

    @Override
    public StandRes setMapperPackageName(String mapperPackageName) {
        super.setMapperPackageName(mapperPackageName);
        return this;
    }

    @Override
    public StandRes setMapperXmlPackageName(String mapperXmlPackageName) {
        super.setMapperXmlPackageName(mapperXmlPackageName);
        return this;
    }

    @Override
    public StandRes setServiceInterfacePackageName(String serviceInterfacePackageName) {
        super.setServiceInterfacePackageName(serviceInterfacePackageName);
        return this;
    }

    @Override
    public StandRes setServiceImplPackageName(String serviceImplPackageName) {
        super.setServiceImplPackageName(serviceImplPackageName);
        return this;
    }

    @Override
    public StandRes setMapperStructPackageName(String mapperStructPackageName) {
        super.setMapperStructPackageName(mapperStructPackageName);
        return this;
    }

    @Override
    public StandRes setMapperStructClassSimpleName(String mapperStructClassSimpleName) {
        super.setMapperStructClassSimpleName(mapperStructClassSimpleName);
        return this;
    }

    @Override
    public StandRes setUrlPrefix(String urlPrefix) {
        super.setUrlPrefix(urlPrefix);
        return this;
    }


    @Override
    public StandRes setCreateTimeName(String createTimeName) {
         super.setCreateTimeName(createTimeName);
        return this;
    }

    @Override
    public StandRes setIsEnabledName(String isEnabledName) {
         super.setIsEnabledName(isEnabledName);
        return this;
    }

    @Override
    public StandRes setIsDeletedName(String isDeletedName) {
         super.setIsDeletedName(isDeletedName);
        return this;
    }

    @Override
    public StandRes setIsDeletedValid(String isDeletedValid) {
         super.setIsDeletedValid(isDeletedValid);
        return this;
    }

    @Override
    public StandRes setIsDeletedNotValid(String isDeletedNotValid) {
         super.setIsDeletedNotValid(isDeletedNotValid);
        return this;
    }

    @Override
    public StandRes setIsEnabledValid(String isEnabledValid) {
         super.setIsEnabledValid(isEnabledValid);
        return this;
    }

    @Override
    public StandRes setIsEnabledNotValid(String isEnabledNotValid) {
         super.setIsEnabledNotValid(isEnabledNotValid);
        return this;
    }
}
