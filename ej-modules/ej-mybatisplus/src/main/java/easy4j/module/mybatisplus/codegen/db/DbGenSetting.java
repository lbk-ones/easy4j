package easy4j.module.mybatisplus.codegen.db;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 用于生成domain的数据库配置
 */
@Data
@Accessors(chain = true)
public class DbGenSetting implements Serializable {

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
}
