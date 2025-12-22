package easy4j.module.mybatisplus.codegen.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 用于生成domain的数据库配置
 */
@Data
@Accessors(chain = true)
public class EntityConfig implements Serializable {

    private String url;
    private String username;
    private String password;

    /**
     * 如果想以xxx_开头
     * ps: xxx_%
     */
    private String tablePrefix;

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
    private boolean genService= false;

    /**
     * 是否生成 业务实现
     */
    private boolean genServiceImpl= false;

    /**
     * 是否生成 接口
     */
    private boolean genController= false;

    /**
     * 是否生成 接口传参
     */
    private boolean genControllerReq= false;

    /**
     * 是否生成 to
     */
    private boolean genDto= false;
}
