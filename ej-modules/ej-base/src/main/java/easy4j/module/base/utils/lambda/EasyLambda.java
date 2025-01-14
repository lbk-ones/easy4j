package easy4j.module.base.utils.lambda;


import cn.hutool.core.util.ReflectUtil;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.util.Map;


public class EasyLambda {
    private static final Map<SFunction<?,?>, Field> FUNCTION_CACHE = new ConcurrentReferenceHashMap<>();

    public static <T,R> String getFieldName(SFunction<T,R> function) {
        Field field = getField(function);
        return field.getName();
    }

    public static <T,R> Field getField(SFunction<T,R> function) {
        return FUNCTION_CACHE.computeIfAbsent(function, EasyLambda::findField);
    }

    public static <T,R> Field findField(SFunction<T,R> function) {
        final SerializedLambda serializedLambda = getSerializedLambda(function);
        final String implClass = serializedLambda.getImplClass();
        final String implMethodName = serializedLambda.getImplMethodName();
        final String fieldName = convertToFieldName(implMethodName);
        final Field field = getField(fieldName, serializedLambda);
        if (field == null) {
            throw new RuntimeException("No such class 「"+ implClass +"」 field 「" + fieldName + "」.");
        }
        return field;
    }

    static Field getField(String fieldName, SerializedLambda serializedLambda) {
        try {
            String declaredClass = serializedLambda.getImplClass().replace("/", ".");
            Class<?> aClass = ClassUtils.forName(declaredClass,getDefaultClassLoader());
            return ReflectUtil.getField(aClass, fieldName);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("get class field exception.", e);
        }
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = EasyLambda.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    static String convertToFieldName(String getterMethodName) {
        // 获取方法名
        String prefix = null;
        if (getterMethodName.startsWith("get")) {
            prefix = "get";
        }
        else if (getterMethodName.startsWith("is")) {
            prefix = "is";
        }
        if (prefix == null) {
            throw new IllegalArgumentException("invalid getter method: " + getterMethodName);
        }
        // 截取get/is之后的字符串并转换首字母为小写
        return Introspector.decapitalize(getterMethodName.replace(prefix, ""));
    }

    static <T,R> SerializedLambda getSerializedLambda(SFunction<T,R> function) {
        try {
            return ReflectUtil.invoke(function,"writeReplace");
        }
        catch (Exception e) {
            throw new RuntimeException("get SerializedLambda exception.", e);
        }
    }
}