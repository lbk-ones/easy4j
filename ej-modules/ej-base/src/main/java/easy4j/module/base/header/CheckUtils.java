package easy4j.module.base.header;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.utils.ListTs;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 检查工具类
 * @author bokun
 */
public class CheckUtils {

    /**
     * 使用到了 中文注解
     * @see io.swagger.v3.oas.annotations.media.Schema
     * @param t
     * @param message
     * @param <T>
     */
    public static <T> void checkByLambda(Object t, Func1<T,?>...message) {
        Set<String> resultList = new HashSet<>();
        Class<?> aClass = t.getClass();
        for (Func1<T, ?> trFunction : message) {
            String fieldName = LambdaUtil.getFieldName(trFunction);
            Field field = ReflectUtil.getField(aClass, fieldName);
            Object value = ReflectUtil.getFieldValue(t, field);
            if (ObjectUtil.isEmpty(value)) {
                Schema annotation = field.getAnnotation(Schema.class);
                String description = annotation.description();
                if (StrUtil.isNotBlank(description)) {
                    description += "【"+fieldName+"】";
                    resultList.add(description);
                }else{
                    resultList.add(field.getName());
                }
            }
        }
        if(resultList.isEmpty()){
            String join = String.join("，", resultList);
            throw new EasyException("A00004,"+join);
        }
    }

    public static String joinElementsAfterPosition(String[] array, int position) {
        if (array == null || position < 0 || position >= array.length) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = position; i < array.length; i++) {
            if (i > position) {
                result.append(array[i]);
                if(i<array.length-1){
                    result.append(".");
                }
            }
        }
        return result.toString();
    }
    public static Object getValueByPath(Object obj, String path) {
        return getValueByPath(obj, path,false);
    }

    /**
     * 根据路径表达式从对象中获取值，支持 list.[].wt 和 list.[0].wt 等形式
     *
     * @param obj 要从中获取值的对象
     * @param path 路径表达式 list.wt | list.[0].wt | list.[].wt.name | list.[0].wt.name.city
     * @param fastError 快速失败 list.[].wt 如果list集合有属性中的对象wt为空那么直接返回null
     * @return 获取到的值，如果未找到则返回 null
     * @author bokun
     */
    public static Object getValueByPath(Object obj, String path,boolean fastError) {
        if (obj == null || path == null || path.isEmpty()) {
            return null;
        }
        String[] parts = path.split("\\.");
        Object current = obj;
        boolean lastIsArray = false;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (current == null || StrUtil.isBlank(part)) {
                return null;
            }
            if (part.startsWith("[")) {
                // not allow list.[].[].wt
                if(lastIsArray){
                    continue;
                }
                lastIsArray = true;
                if (current instanceof List) {
                    List<?> list = (List<?>) current;
                    if (part.equals("[]")) {
                        List<Object> returnRe = ListTs.newArrayList();
                        // 处理 list.[].wt.yy.[].ht 情况
                        for (Object item : list) {
                            String s = joinElementsAfterPosition(parts, i);
                            // 如果值是空的代表当前属性不存在
                            Object valueByPath = getValueByPath(item, s,fastError);
                            if(!ObjectUtil.isEmpty(valueByPath)){
                                returnRe.add(valueByPath);
                            }else{
                                if(fastError){
                                    return null;
                                }
                            }
                        }
                        return returnRe;
                    } else {
                        // 处理 list.[0].wt 情况
                        try {
                            int index = Integer.parseInt(part.substring(1, part.length() - 1));
                            if (index < list.size()) {
                                current = list.get(index);
                            } else {
                                return null;
                            }
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                lastIsArray = false;
                try{
                    if(current instanceof Map){
                        Map<?, ?> current1 = (Map<?, ?>) current;
                        current = current1.get(part);
                    }else{
                        current = ReflectUtil.getFieldValue(current, part);
                    }
                }catch (Throwable e){
                    current = null;
                }
            }
        }
        return current;
    }
    /**
     * 根据路径检查参数是否为空
     * @param t
     * @param message list.[].user.address.city 获取 list.[0].user.name.[].ahha
     */
    public static void checkByPath(Object t, String ...message) {
        Set<String> resultList = new HashSet<>();
        if (t instanceof Iterator) {
            Iterator<?> t1 = (Iterator<?>) t;
            while (t1.hasNext()) {
                Object next = t1.next();
                for (String fieldName : message) {
                    fieldName = fieldName.replaceAll("^(\\[]\\.)+", "");
                    Object valueByPath = getValueByPath(next, fieldName,true);
                    if (ObjectUtil.isEmpty(valueByPath)) {
                        resultList.add(fieldName);
                    }
                }
            }
        }else{
            for (String s : message) {
                s = s.replaceAll("^(\\[]\\.)+", "");
                Object valueByPath = getValueByPath(t, s,true);
                if (ObjectUtil.isEmpty(valueByPath)) {
                    resultList.add(s);
                }
            }
        }
        if(!resultList.isEmpty()){
            String join = String.join("，", resultList);
            throw new EasyException("A00004,"+join);
        }
    }


}
