package easy4j.module.mybatisplus.codegen.db;

import cn.hutool.core.util.RandomUtil;
import easy4j.module.mybatisplus.codegen.GlobalGenConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class EntityInfo extends GlobalGenConfig implements Serializable {

    private String tableName = "";

    /**
     * 去除掉表前缀
     */
    String schema = "";

    String description = "";



    List<EFieldInfo> fieldInfoList = new ArrayList<>();

    /**
     * 是否继承 AutoAudit
     */
    boolean hasExtend = false;

    Set<String> importList = new HashSet<>();

    String serialVersionId = String.valueOf(RandomUtil.randomLong());

    private boolean sameTableField;

    private boolean sameSchema;


    // 在 easy4j.module.mybatisplus.codegen.entity.EntityGen 生成的时候 适配 GenDto
    // 实体名称 驼峰 帕斯卡命名发 必须是大写
    String domainName;

    // 中文描述
    String cnDesc;

    // 返回的dto名称
    String returnDtoName;


    // 数据库实体类名称
    String entityName;

    @Data
    @Accessors(chain = true)
    public static class EFieldInfo {
        private boolean hasPrimaryKey;
        private String dbName = "";
        private String name = "";
        private String description = "";
        private String type = "";
        private boolean hasAutoincrement;
        private boolean hasTableField;
        private String mybatisJdbcType;
        private boolean sameTableField;
        private boolean sameSchema;
    }
}
