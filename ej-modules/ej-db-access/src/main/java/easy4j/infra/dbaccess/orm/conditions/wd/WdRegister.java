package easy4j.infra.dbaccess.orm.conditions.wd;

import cn.hutool.core.clone.CloneSupport;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持运行时注册和手动注册
 */
public class WdRegister {

    private static final Map<Class<?>, Class<?>> wdClassMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Class<?>> classWdMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> wdObjectVs = new ConcurrentHashMap<>();


    static {
        WdRegister.register(String.class, WdStr.class);
        wdObjectVs.put(String.class, new WdStr());

        WdRegister.register(Integer.class, WdInt.class);
        wdObjectVs.put(Integer.class, new WdInt());

        WdRegister.register(int.class, WdInt.class);
        wdObjectVs.put(int.class, new WdInt());

        WdRegister.register(Byte.class, WdByte.class);
        wdObjectVs.put(Byte.class, new WdByte());

        WdRegister.register(byte.class, WdByte.class);
        wdObjectVs.put(byte.class, new WdByte());

        WdRegister.register(Short.class, WdShort.class);
        wdObjectVs.put(Short.class, new WdShort());

        WdRegister.register(short.class, WdShort.class);
        wdObjectVs.put(short.class, new WdShort());

        WdRegister.register(Float.class, WdFloat.class);
        wdObjectVs.put(Float.class, new WdFloat());

        WdRegister.register(float.class, WdFloat.class);
        wdObjectVs.put(float.class, new WdFloat());

        WdRegister.register(long.class, WdLong.class);
        wdObjectVs.put(long.class, new WdLong());

        WdRegister.register(Long.class, WdLong.class);
        wdObjectVs.put(Long.class, new WdLong());

        WdRegister.register(Double.class, WdDouble.class);
        wdObjectVs.put(Double.class, new WdDouble());

        WdRegister.register(double.class, WdDouble.class);
        wdObjectVs.put(double.class, new WdDouble());

        WdRegister.register(Boolean.class, WdBool.class);
        wdObjectVs.put(Boolean.class, new WdBool());

        WdRegister.register(boolean.class, WdBool.class);
        wdObjectVs.put(boolean.class, new WdBool());

        WdRegister.register(Character.class, WdChar.class);
        wdObjectVs.put(Character.class, new WdChar());

        WdRegister.register(char.class, WdChar.class);
        wdObjectVs.put(char.class, new WdChar());

        WdRegister.register(BigDecimal.class, WdBigDecimal.class);
        wdObjectVs.put(BigDecimal.class, new WdBigDecimal());

        WdRegister.register(Date.class, WdDate.class);
        wdObjectVs.put(Date.class, new WdDate());

        WdRegister.register(LocalDate.class, WdLocalDate.class);
        wdObjectVs.put(LocalDate.class, new WdLocalDate());

        WdRegister.register(LocalDateTime.class, WdLocalDateTime.class);
        wdObjectVs.put(LocalDateTime.class, new WdLocalDateTime());

        WdRegister.register(List.class, WdList.class);
        wdObjectVs.put(List.class, new WdList());

        WdRegister.register(Collection.class, WdCollection.class);
        wdObjectVs.put(Collection.class, new WdCollection());

        WdRegister.register(Map.class, WdMap.class);
        wdObjectVs.put(Map.class, new WdMap());

        WdRegister.register(DateTime.class, WdDateTime.class);
        wdObjectVs.put(DateTime.class, new WdDateTime());

        WdRegister.register(Object.class, WdObject.class);
        wdObjectVs.put(Object.class, new WdObject());


        // ---------------------------
        wdObjectVs.put(WdStr.class, new WdStr());
        wdObjectVs.put(WdInt.class, new WdInt());
        wdObjectVs.put(WdByte.class, new WdByte());
        wdObjectVs.put(WdShort.class, new WdShort());
        wdObjectVs.put(WdFloat.class, new WdFloat());
        wdObjectVs.put(WdLong.class, new WdLong());
        wdObjectVs.put(WdDouble.class, new WdDouble());
        wdObjectVs.put(WdBool.class, new WdBool());
        wdObjectVs.put(WdChar.class, new WdChar());
        wdObjectVs.put(WdBigDecimal.class, new WdBigDecimal());
        wdObjectVs.put(WdDate.class, new WdDate());
        wdObjectVs.put(WdLocalDate.class, new WdLocalDate());
        wdObjectVs.put(WdLocalDateTime.class, new WdLocalDateTime());
        wdObjectVs.put(WdList.class, new WdList());
        wdObjectVs.put(WdCollection.class, new WdCollection());
        wdObjectVs.put(WdMap.class, new WdMap());
        wdObjectVs.put(WdDateTime.class, new WdDateTime());
        wdObjectVs.put(WdObject.class, new WdObject());
    }

    // 减轻反射的开销
    public static Object newInstance(Class<?> clazz) {
        Object o = wdObjectVs.get(clazz);
        if (o != null) {
            if (o instanceof Wd<?> wd2) {
                return wd2.cloneNew();
            }
        }
        return null;
    }

    // 减轻反射的开销
    public static Object instanceCache(Class<?> clazz) {
        Object o1 = wdObjectVs.get(clazz);
        if (o1 != null) {
            if (o1 instanceof Wd<?> wd2) {
                o1 = wd2.cloneNew();
            }else{
                if(o1 instanceof CloneSupport<?> o2){
                    o1 = o2.clone();
                }
            }
        } else {
            o1 = ReflectUtil.newInstance(clazz);
            wdObjectVs.put(clazz, o1);
        }
        return o1;
    }

    public static void register(Type type, Class<?> wd) {
        if (!Wd.class.isAssignableFrom(wd)) {
            return;
        }
        if (type instanceof Class<?> type1) {
            classWdMap.putIfAbsent(type1, wd);
            wdClassMap.putIfAbsent(wd, type1);
        }
    }

    public static Class<?> getByClass(Class<?> clazz) {
        if (clazz == null) return null;
        return classWdMap.get(clazz);
    }

    public static Class<?> getByWd(Class<?> wd) {
        if (wd == null) return null;

        return wdClassMap.get(wd);
    }
}
