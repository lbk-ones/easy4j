package easy4j.module.mybatisplus.codegen;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)
public class GlobalGenConfig {
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


    public GlobalGenConfig setUrlPrefix(String urlPrefix) {
        if(StrUtil.endWith(urlPrefix, SP.SLASH)){
            urlPrefix = StrUtil.replaceLast(urlPrefix,SP.SLASH,"");
        }
        this.urlPrefix = urlPrefix;
        return this;
    }
}
