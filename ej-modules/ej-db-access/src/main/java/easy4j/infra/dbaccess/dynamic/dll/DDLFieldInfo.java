package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.common.annotations.Desc;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DDLFieldInfo {

    @Desc("数据库类型")
    private String dbType;

    @Desc("数据库版本")
    private String dbVersion;

    @Desc("字段的类型")
    private Class<?> fieldClass;

    @Desc("字段名称，默认属性名称转下划线")
    private String name;

    @Desc("是否是主键")
    private boolean isPrimary;

    @Desc("是否递增")
    private boolean isAutoIncrement;

    @Desc("字段类型")
    private String dataType;

    @Desc("长度")
    private int dataLength;

    @Desc("精度")
    private int dataDecimal;

    @Desc("数据类型元素通常会被解析为('xxx','xxx')")
    private String[] dataTypeAttr;

    @Desc("默认值")
    private String def;

    private boolean defTime;

    @Desc("是否为空")
    private boolean isNotNull;

    @Desc("是否唯一")
    private boolean isUnique;

    @Desc("是否是单字段索引")
    private boolean isIndex;

    @Desc("自定义约束 比如 unique、not null、default")
    private String[] constraint;

    private String comment;


    @Desc("配置上下文信息")
    private DDLConfig dllConfig;

}
