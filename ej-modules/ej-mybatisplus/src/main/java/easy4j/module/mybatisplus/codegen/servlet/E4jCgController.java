package easy4j.module.mybatisplus.codegen.servlet;
import com.google.common.collect.Lists;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.servlet.MethodType;
import easy4j.infra.common.utils.servlet.SRes;
import easy4j.infra.common.utils.servlet.ServletHandler;
import easy4j.infra.common.utils.servlet.UrlMap;

import java.util.Map;

public class E4jCgController {


    /**
     * 获取基础信息
     *
     * @param servletHandler
     */
    @UrlMap(url = "/init",method = MethodType.POST)
    public void init(ServletHandler servletHandler){
        Map<String, String> formDataMap = servletHandler.getFormDataMap();
        StandRes standRes = new StandRes();
        String dbUrl = Easy4j.getProperty(SysConstant.DB_URL_STR);
        String userName = Easy4j.getProperty(SysConstant.DB_USER_NAME);
        String password = Easy4j.getProperty(SysConstant.DB_USER_PASSWORD);
        standRes.setUrl(dbUrl);
        standRes.setUsername(userName);
        standRes.setPassword(password);
        standRes.setExclude(Lists.newArrayList());
        standRes.setParentPackageName(Easy4j.mainClassPath);
        standRes.setProjectAbsolutePath(System.getenv("user.dir"));
        standRes.setDeleteIfExists(false);
        standRes.setAuthor("easy4j");
        standRes.setForceDelete(false);
        standRes.setAllTables(Lists.newArrayList());
        servletHandler.responseJson(SRes.success(standRes));
    }

    /**
     * 预览
     * @param servletHandler handler
     */
    @UrlMap(url = "/preview",method = MethodType.POST)
    public void preview(ServletHandler servletHandler){
        Map<String, String> formDataMap = servletHandler.getFormDataMap();
        // TODO
        servletHandler.responseJson(SRes.success("ok"));
    }

    /**
     * 生成代码
     * @param servletHandler handler
     */
    @UrlMap(url = "/gen",method = MethodType.POST)
    public void gen(ServletHandler servletHandler){
        Map<String, String> formDataMap = servletHandler.getFormDataMap();
        // TODO
        servletHandler.responseJson(SRes.success("ok"));
    }


}
