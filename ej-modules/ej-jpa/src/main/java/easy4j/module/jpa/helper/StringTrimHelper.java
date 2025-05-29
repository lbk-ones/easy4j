package easy4j.module.jpa.helper;


import cn.hutool.core.util.ReflectUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.jpa.annotations.Trim;

import java.lang.reflect.Field;

/**
 * StringTrimHelper
 *
 * @author bokun.li
 * @date 2025-05
 */
public class StringTrimHelper {

	public static void trim(Object obj) throws EasyException {
		Class<? extends Object> clazz = obj.getClass();

		for(Field field : ReflectUtil.getFields(clazz)){
			if(field.isAnnotationPresent(Trim.class)){
				Object o = ReflectUtil.getFieldValue(obj,field);
				if(o == null){
					continue;
				}
				if(o instanceof String){
					String f = (String)o;
					ReflectUtil.setFieldValue(obj,field,f.trim());
				}else{
					EasyException.throwExc("unable format not string field");
				}
			}
		}
	}
}