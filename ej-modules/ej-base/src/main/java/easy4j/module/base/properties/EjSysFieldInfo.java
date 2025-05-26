package easy4j.module.base.properties;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.ListTs;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * 参数信息封装
 */
@Data
public class EjSysFieldInfo {
    private String fieldName;
    private String sysConstantName;
    private String desc;
    private String[] vs;


    public static List<EjSysFieldInfo> getAllEjSysInfoList(){
        List<EjSysFieldInfo> res = ListTs.newArrayList();
        Field[] fieldsValue = ReflectUtil.getFields(EjSysProperties.class);
        for (Field field : fieldsValue) {
            String ejSysPropertyName = Easy4j.getEjSysPropertyName(field);
            if(StrUtil.isBlank(ejSysPropertyName)){
                continue;
            }
            SpringVs annotation = field.getAnnotation(SpringVs.class);
            String name = field.getName();
            EjSysFieldInfo ejSysFieldInfo = new EjSysFieldInfo();
            ejSysFieldInfo.setFieldName(name);
            ejSysFieldInfo.setSysConstantName(ejSysPropertyName);
            if(Objects.nonNull(annotation)){
                ejSysFieldInfo.setDesc(annotation.desc());
                ejSysFieldInfo.setVs(annotation.vs());
                ejSysFieldInfo.setVs(annotation.vs());
            }
            res.add(ejSysFieldInfo);
        }
        return res;
    }

}
