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

    @Desc("是否递增,pg数据库自动关联序列")
    private boolean isAutoIncrement;

    @Desc("从哪个数开始递增")
    private int startWith;

    @Desc("每次递增多少，默认1")
    private int increment = 1;

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

    @Desc("整形默认值")
    private int defNum = -1;

    @Desc("时间是否是默认时间")
    private boolean defTime;

    @Desc("是否为空")
    private boolean isNotNull;

    @Desc("是否唯一")
    private boolean isUnique;

    @Desc("unique null是否也不能重复，默认是允许重复的")
    private boolean isUniqueNotNullDistinct;

    @Desc("check约束，直接传入check括号里面的约束")
    private String check;

    @Desc("是否是单字段索引")
    private boolean isIndex;

    @Desc("自定义约束 比如 unique、not null、default")
    @Deprecated
    private String[] constraint;

    @Desc("字段注释")
    private String comment;

    @Desc("是否长文本")
    private boolean isLob = false;

    @Desc("是否是json类型字段")
    private boolean isJson = false;

    @Desc("生成字段列的约束")
    private boolean genConstraint;

    @Desc("配置上下文信息")
    private DDLConfig dllConfig;

}
