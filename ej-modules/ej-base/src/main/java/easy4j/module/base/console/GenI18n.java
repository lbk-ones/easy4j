package easy4j.module.base.console;

import cn.hutool.core.util.ReflectUtil;
import easy4j.module.base.annotations.Desc;
import easy4j.module.base.utils.BusCode;

import java.lang.reflect.Field;

public class GenI18n {
    public static void main(String[] args) {
        Field[] fields = ReflectUtil.getFields(BusCode.class);
        for (Field field : fields) {
            String name = field.getName();
            if (field.isAnnotationPresent(Desc.class)) {
                Desc annotation = field.getAnnotation(Desc.class);
                String value = annotation.value();
                System.out.println(name + "=" + value.replaceAll(",", "，"));
            }
        }
    }
}
