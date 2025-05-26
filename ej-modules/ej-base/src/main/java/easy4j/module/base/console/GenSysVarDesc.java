package easy4j.module.base.console;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.properties.SpringVs;
import easy4j.module.base.utils.SysConstant;
import jodd.util.StringPool;

import java.lang.reflect.Field;

/**
 * 生成系统参数描述
 */
public class GenSysVarDesc {
    public static void main(String[] args) {
        Field[] fields = ReflectUtil.getFields(EjSysProperties.class);
        for (Field field : fields) {
            String name = field.getName();
            SpringVs annotation = field.getAnnotation(SpringVs.class);
            String value = annotation.desc();
            String lowerCase = StrUtil.toUnderlineCase(name).toLowerCase();
            String replace = SysConstant.PARAM_PREFIX + StringPool.DOT + lowerCase.replace(StringPool.UNDERSCORE, StringPool.DASH);
            String print = "- **" + replace + "**: " + value;
            System.out.println(print);
        }
    }
}
