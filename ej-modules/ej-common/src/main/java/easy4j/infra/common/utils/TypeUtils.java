package easy4j.infra.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;


/**
 * TypeUtils
 *
 * @author bokun.li
 * @since 2.0.1
 */
public class TypeUtils {
    /**
     * 从参数化类型（ParameterizedType）中提取Map的Key和Value类型
     *
     * @param mapType Map的参数化类型（如 HashMap<String, Integer> 的 Type）
     * @return 数组：[Key的Class, Value的Class]；非Map/无泛型返回null
     */
    public static Class<?>[] getMapKeyAndValueType(Type mapType) {
        ParameterizedType parameterizedType = null;
        // 1. 校验是否为参数化类型（有泛型的Map）
        if (mapType instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType) mapType;
        }
        if (parameterizedType == null) return null;

        // 2. 校验是否为Map类型
        Type rawType = parameterizedType.getRawType();
        if (!(rawType instanceof Class<?>) || !Map.class.isAssignableFrom((Class<?>) rawType)) {
            return null;
        }

        // 3. 获取Map的<K, V>实际类型参数
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 2) {
            return null;
        }

        // 4. 转换为Class（处理Type可能是Class/ParameterizedType等情况）
        Class<?> keyClass = getClassFromType(actualTypeArguments[0]);
        Class<?> valueClass = getClassFromType(actualTypeArguments[1]);
        return new Class<?>[]{keyClass, valueClass};
    }

    public static Class<?> getSetType(Type mapType) {
        ParameterizedType parameterizedType = null;
        // 1. 校验是否为参数化类型（有泛型的Map）
        if (mapType instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType) mapType;
        }
        if (parameterizedType == null) return null;

        // 2. 校验是否为Map类型
        Type rawType = parameterizedType.getRawType();
        if (!(rawType instanceof Class<?>) || !Set.class.isAssignableFrom((Class<?>) rawType)) {
            return null;
        }

        // 3. 获取Map的<K, V>实际类型参数
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
            return null;
        }

        // 4. 转换为Class（处理Type可能是Class/ParameterizedType等情况）
        return getClassFromType(actualTypeArguments[0]);
    }


    public static Class<?> getCollectionType(Type mapType) {
        ParameterizedType parameterizedType = null;
        // 1. 校验是否为参数化类型（有泛型的Map）
        if (mapType instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType) mapType;
        }
        if (parameterizedType == null) return null;

        // 2. 校验是否为Map类型
        Type rawType = parameterizedType.getRawType();
        if (!(rawType instanceof Class<?>) || !Collection.class.isAssignableFrom((Class<?>) rawType)) {
            return null;
        }

        // 3. 获取Map的<K, V>实际类型参数
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
            return null;
        }

        // 4. 转换为Class（处理Type可能是Class/ParameterizedType等情况）
        return getClassFromType(actualTypeArguments[0]);
    }

    /**
     * 将Type转换为Class（处理嵌套泛型/基本类型等）
     */
    public static Class<?> getClassFromType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        // 处理嵌套参数化类型（如 Map<String, List<Integer>> 中的 List<Integer>）
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class<?>) parameterizedType.getRawType();
        }
        // 处理基本类型包装（如 Integer.TYPE 对应 int）
        if (type instanceof TypeVariable<?>) {
            return Object.class; // 泛型变量（如 Map<K, V>）返回Object
        }
        return Object.class;
    }

}