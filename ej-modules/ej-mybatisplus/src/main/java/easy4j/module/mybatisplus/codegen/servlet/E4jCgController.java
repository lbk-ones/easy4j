package easy4j.module.mybatisplus.codegen.servlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.servlet.MethodType;
import easy4j.infra.common.utils.servlet.SRes;
import easy4j.infra.common.utils.servlet.ServletHandler;
import easy4j.infra.common.utils.servlet.UrlMap;
import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.TableMetadata;
import easy4j.module.mybatisplus.codegen.AutoGen;
import easy4j.module.mybatisplus.codegen.GenDto;
import easy4j.module.mybatisplus.codegen.GlobalGenConfig;
import easy4j.module.mybatisplus.codegen.MultiGenDto;
import easy4j.module.mybatisplus.codegen.db.DbGenSetting;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;
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
        standRes.setExclude(Lists.newArrayList());
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
        checkNotNullR(servletHandler,
                servletHandler.getFormDataMap(),
                ListTs.asList("url", "username", "password")
        );
        formOrQuery.ifPresent(standRes -> {
            String url = standRes.getUrl();
            String username = standRes.getUsername();
            String password = standRes.getPassword();
            String tablePrefix = standRes.getTablePrefix();
            List<String> exclude = standRes.getExclude();
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
                    .setServiceImplPackageName(serviceImplPackageName);
            String preview = AutoGen.build(globalGenConfig1)
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
                            .setExclude(exclude)
                    )
                    .auto(isPreview, true);
            servletHandler.responseJson(SRes.success(preview));
        });
        if (!formOrQuery.isPresent()) {
            servletHandler.responseJson(SRes.error("no query"));
        }
    }

    private void checkNotNullR(ServletHandler servletHandler, Map<String, String> formDataMap, List<String> nameList) {
        if (formDataMap != null) {
            for (String s : nameList) {
                String s2 = formDataMap.get(s);
                if (StrUtil.isBlank(s2)) {
                    servletHandler.responseJson(SRes.error(s + " is not null "));
                    break;
                }
            }
        }

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


}
