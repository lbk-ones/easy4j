package easy4j.module.jpa.base;


import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import easy4j.module.base.annotations.Desc;
import easy4j.module.base.exception.EasyException;
import easy4j.module.jpa.annotations.AllowCopy;
import easy4j.module.jpa.annotations.FieldDesc;
import easy4j.module.jpa.annotations.MapJsonToField;
import easy4j.module.jpa.constant.Constant;
import easy4j.module.jpa.helper.DtoHelper;
import easy4j.module.jpa.helper.StringTrimHelper;
import easy4j.module.jpa.page.SortDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Id;
import javax.persistence.Version;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
/**
 * 所有继承这个dto的字段最好是包装类型的字段 按规范来 别取相关的字段
 * @author bokun.li
 */
@Slf4j
@Data
public abstract class BaseDto implements Serializable {

	// 查询值
	@Desc("界面搜索的key")
	private String searchKey;

	// 主键
	@Id
	@Desc("主键")
	private String id;

	// 排序字段
	@Desc("排序字段")
	private List<SortDto> sortDtoList;

	@Desc("数据状态值 -1已删除 0禁用 1启用 2禁用和启用 3全部")
	private int recordStatus = 4;

	@Desc("每页多少条记录")
	private Integer pageSize = Constant.PAGE_SIZE;

	@Desc("页码 从0开始")
	private Integer pageNo = Constant.PAGE_NUMBER;

	@Version
	private int version;

	public void trim() throws EasyException {
		try {
			StringTrimHelper.trim(this);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new EasyException(e.getMessage());
		} 
	}

	/**
	 * 从DTO生成新的实体，使用前请先做必要的验证
	 * @param clazz
	 * @param <T>
	 * @return
	 * @throws EasyException
	 */
	public  <T>T toNewEntity(Class<T> clazz) throws EasyException {
		//T instance = clazz.newInstance();
		try {
			return DtoHelper.copyDtoToNewEntity(this, clazz);
		}catch (Exception e){
			log.error(e.getMessage(), e);
			throw new EasyException(e.getMessage());
		}
	}

	/**
	 * 从DTO修改原来的实体，使用前请先做必要的验证
	 * @param old
	 * @param <T>
	 * @return
	 * @throws EasyException
	 */
	public <T>T toModifyEntity(T old) throws EasyException {
		try {
			return DtoHelper.copyDtoToOldEntity(this, old);
		}catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EasyException(e.getMessage());
		}
	}

	public abstract void toNewEntityValidate() throws EasyException;


	public boolean checkIsModify(Object o1,Object o2){
		Object o = ObjectUtil.defaultIfNull(ReflectUtil.getFieldValue(o1, LambdaUtil.getFieldName(BaseEntity::getVersion)), "0");
		int fieldValue = Integer.parseInt(o.toString());
		Object oo2 = ObjectUtil.defaultIfNull(ReflectUtil.getFieldValue(o2, LambdaUtil.getFieldName(BaseEntity::getVersion)), "0");
		int fieldValue2 = Integer.parseInt(oo2.toString());
		return fieldValue != fieldValue2;
	}


	/**
	 * jpa的动态跟新有点傻 所以单独处理一下 类似动态更新 将某些被选中的字段拷贝到 modal 实体里面 为空的就不拷贝了
	 * 在字段上面加 @AllowCopy 注解（可以加到父类的字段上面去） 标识这个字段可以被拷贝到其他对象里头
	 * 和上面那个 toNewEntity 有点相像 但是这个要高级那么一丢丢
	 * 支持 cn.hutool.core.annotation.Alias 这个注解  可以把当前dto的这个字段的值 赋值到 另外一个对象的另外一个属性上面去
	 * ！！！ 为空的属性不会被拷贝  ！！！
	 * @param toObj
	 */
	public void copyPickPropertyToOtherObj(Object toObj){
		if(Objects.isNull(toObj)){
			return;
		}
		Object obj = this;
		Field[] fields = ReflectUtil.getFields(obj.getClass());
		for (Field field : fields) {
			if(field.isAnnotationPresent(AllowCopy.class)){
				String fieldName = ReflectUtil.getFieldName(field);
				Object fieldValue = ReflectUtil.getFieldValue(obj, field);
				if(Objects.nonNull(fieldValue)){
					Field objField = ReflectUtil.getField(toObj.getClass(), fieldName);
					if(Objects.nonNull(objField)){
						ReflectUtil.setFieldValue(toObj,objField,fieldValue);
					}
				}
			}
		}
	}

	/**
	 * 获取字段描述信息
	 *
	 * @author bokun.li
	 * @date 2023/6/1
	 */
	public Map<String,String> GetFieldDesc(){
		Object obj = this;
		Map<String, String> hashMap = Maps.newHashMap();
		Field[] fields = ReflectUtil.getFields(obj.getClass());
		for (Field field : fields) {
			if(field.isAnnotationPresent(FieldDesc.class)){
				FieldDesc annotation = field.getAnnotation(FieldDesc.class);
				String fieldName = ReflectUtil.getFieldName(field);
				String value = annotation.value();
				hashMap.put(fieldName, value);
			}
		}
		return hashMap;
	}

//	public String checkId() throws EasyException{
//		if (StrUtil.isBlank(this.id)) {
//			throw new EasyException("id不能为空");
//		}
//		return this.id;
//	}

	/**
	 * 从json里面 去映射字段到obj 处理一些第三方映射性的东西
	 *
	 * @author bokun.li
	 * @date 2023/6/26
	 */
	public void mapJsonToField(Object obj, JSONObject jsonObject){
		if(Objects.isNull(jsonObject) || Objects.isNull(obj)){
			return;
		}
		try{
			Field[] fields = ReflectUtil.getFields(obj.getClass());
			String name = obj.getClass().getName();
			if (fields.length == 0 || name.equals("java.lang.Object")) {
				return;
			}
			for (Field field : fields) {
				try{
					if (field.isAnnotationPresent(MapJsonToField.class)) {
						MapJsonToField annotation = field.getAnnotation(MapJsonToField.class);
						String[] value = annotation.value();
						// 遍历
						for (String jsonName : value) {
							String jsonValue = jsonObject.getString(jsonName);
							if(StrUtil.isNotBlank(jsonValue) ){
								// 会自动转换类型 good
								ReflectUtil.setFieldValue(obj,field,jsonValue);
								// 按顺序找 找到就不继续遍历了
								break;
							}
						}
					}
				}catch (Exception e){
					log.error("字段解析错误",e);
				}

			}
		}catch (Exception e){
			log.error("JSON解析错误---",e);
		}
	}
}
