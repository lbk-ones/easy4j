package easy4j.infra.dbaccess.orm.conditions.wd;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持运行时注册和手动注册
 */
public class WdRegister {

    public static final Map<Class<?>, Class<?>> wdClassMap = new ConcurrentHashMap<>();
    public static final Map<Class<?>, Class<?>> classWdMap = new ConcurrentHashMap<>();


    static {
        WdRegister.register(String.class, WdStr.class);
        WdRegister.register(Integer.class, WdInt.class);
        WdRegister.register(int.class, WdInt.class);
        WdRegister.register(Byte.class, WdByte.class);
        WdRegister.register(byte.class, WdByte.class);
        WdRegister.register(Short.class, WdShort.class);
        WdRegister.register(short.class, WdShort.class);
        WdRegister.register(Float.class, WdFloat.class);
        WdRegister.register(float.class, WdFloat.class);
        WdRegister.register(long.class, WdLong.class);
        WdRegister.register(Long.class, WdLong.class);
        WdRegister.register(Double.class, WdDouble.class);
        WdRegister.register(double.class, WdDouble.class);
        WdRegister.register(Boolean.class, WdBool.class);
        WdRegister.register(boolean.class, WdBool.class);
        WdRegister.register(Character.class, WdChar.class);
        WdRegister.register(char.class, WdChar.class);
        WdRegister.register(BigDecimal.class, WdBigDecimal.class);
        WdRegister.register(Date.class, WdDate.class);
        WdRegister.register(LocalDate.class, WdLocalDate.class);
        WdRegister.register(LocalDateTime.class, WdLocalDateTime.class);
        WdRegister.register(List.class, WdList.class);
        WdRegister.register(Collection.class, WdCollection.class);
        WdRegister.register(Map.class, WdMap.class);
        WdRegister.register(Object.class, WdObject.class);
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
        if(clazz==null) return null;
        return classWdMap.get(clazz);
    }

    public static Class<?> getByWd(Class<?> wd) {
        if(wd==null) return null;

        return wdClassMap.get(wd);
    }
}
