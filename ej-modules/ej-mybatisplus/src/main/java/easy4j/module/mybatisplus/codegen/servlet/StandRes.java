package easy4j.module.mybatisplus.codegen.servlet;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class StandRes {

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
    private List<String> exclude;

    /**
     * 去除表格前缀
     */
    private String removeTablePrefix;


    // 父包名称
    String parentPackageName;

    // 项目所在绝对路径
    String projectAbsolutePath;

    // 生成controller中的url前缀 格式为  xxx/xxx  后面的地址根据domainName转小写自动生成
    String urlPrefix;

    // 存在则删除
    boolean deleteIfExists;

    // 类文件头注释
    String headerDesc = "no desc";

    // 作者
    String author = "bokun.li";

    // 只删除
    boolean forceDelete = false;

    // domains 路径
    private String entityPackageName = "domains";

    // controller 路径
    private String controllerPackageName = "controller";

    // controller.req 路径
    private String controllerReqPackageName = "controller.req";

    // dto 路径
    private String dtoPackageName = "dto";

    // mapper 路径
    private String mapperPackageName = "mapper";

    // xml路径
    private String mapperXmlPackageName = "mappers";

    // service 路径
    private String serviceInterfacePackageName = "service";

    // service 路径
    private String serviceImplPackageName = "service.impl";

    // 所有的表
    private List<String> allTables;

}
