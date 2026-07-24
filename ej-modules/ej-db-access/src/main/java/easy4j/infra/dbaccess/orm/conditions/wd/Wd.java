package easy4j.infra.dbaccess.orm.conditions.wd;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.db.meta.JdbcType;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.handler.DefaultTypeHandler;
import easy4j.infra.dbaccess.orm.handler.TypeHandler;
import easy4j.infra.dbaccess.orm.handler.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * 这个类的目的是对条件构造器传进来的值的包装
 * <br/>
 * 可以通过这个类实现对占位符的改写
 * <br/>
 * 比如pg pg是没有隐式转换 那么可以使用  Wd.of("?::INTEGER","1") 这样来包裹 会自动解析成 name = ?::INTEGER
 * <br/>
 * mysql 字符串转时间 Wd.of("CAST(? AS DATETIME)","2024-01-15 10:30:00")
 * <br/>
 *
 * @author bokun.li
 * @since 2.1.4
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class Wd<T> extends TypeReference<T> implements Serializable {

    private Wd<T> instance = this;

    public static final String DEFAULT_PLACE = SP.QUESTION_MARK;


    // 整个替换
    private String placeHolder = DEFAULT_PLACE;

    // 值
    private T value;

    // 类型转换器
    private TypeHandler typeHandler = DefaultTypeHandler.INSTANCE;


    // 类型转换器
    private JdbcType jdbcType;

    public Wd() {
        register();
    }

    public void setValueObject(Object valueObj) {
        this.value = (T) valueObj;

    }

    public Wd(String placeHolder, T value) {
        this.placeHolder = placeHolder;
        this.value = value;
        register();
    }

    public void register() {
        WdRegister.register(getRawType(), getClass());
    }

    public Wd(T value) {
        this.value = value;
    }

    /**
     * 静态构造方法
     *
     * @param placeHolder 要替换的占位符
     * @param value       包装的值
     */
    public abstract Wd<T> of(String placeHolder, T value);

    /**
     * 静态构造方法
     *
     * @param value 包装的值
     */
    public abstract Wd<T> of(T value);


    /**
     * 如果没有被Wd包装 则包装一遍 null不包装
     * 有注解则注解优先
     *
     * @param value 值
     */
    public static Object wrapIf(Object value, WdFieldInfo wdFieldInfo) {
        if (value instanceof Wd<?> wd) {
            setFieldInfo(wdFieldInfo, wd);
            return wd;
        } else {
            if (value != null) {
                Class<?> aClass = value.getClass();
                Class<?> aClass1 = WdRegister.getByClass(aClass);
                if (aClass1 != null) {
                    Object o = ReflectUtil.newInstance(aClass1);
                    if (o instanceof Wd<?> wd) {
                        wd.setValueObject(value);
                        setFieldInfo(wdFieldInfo, wd);
                    }
                    return o;
                } else {
                    if (log.isErrorEnabled()) {
                        log.error("not found the class {} WdType", aClass.getName());
                    }
                }
            }
        }
        return value;
    }

    public static void setFieldInfo(WdFieldInfo wdFieldInfo, Wd<?> wd) {
        if (wdFieldInfo == null) return;
        if (wd == null) return;
        String placeHolder1 = wdFieldInfo.getPlaceHolder();
        if (placeHolder1 != null) {
            wd.setPlaceHolder(placeHolder1);
        }
        Class<? extends TypeHandler<?>> typeHandler1 = wdFieldInfo.getTypeHandler();
        if (typeHandler1 != null) {
            TypeHandler<?> typeHandler2 = ReflectUtil.newInstance(typeHandler1);
            wd.setTypeHandler(typeHandler2);
        }
        JdbcType jdbcType1 = wdFieldInfo.getJdbcType();
        if (jdbcType1 != null) {
            wd.setJdbcType(jdbcType1);
        }
    }

    /**
     * 获取真正的值
     *
     * @param object wd包装类实例
     */
    public static Object value(Object object) {
        if (object == null) return null;
        if (object instanceof Wd<?> wd) {
            return wd.getValue();
        } else {
            return object;
        }
    }

    /**
     * 获取占位符
     *
     * @param object wd包装类实例
     */
    public static String place(Object object) {
        if (object == null) return DEFAULT_PLACE;
        if (object instanceof Wd<?> wd) {
            return wd.getPlaceHolder();
        } else {
            return DEFAULT_PLACE;
        }
    }

    /**
     * 获取真正的参数类型
     *
     * @param value wd子类字节码对象
     */
    public static Class<?> type(Class<?> value) {
        if (value == null) return null;
        if (Wd.class.isAssignableFrom(value)) {
            Wd o = (Wd) ReflectUtil.newInstance(value);
            return (Class<?>) o.getRawType();
        } else {
            return value;
        }
    }

    /**
     * 获取类型转换器
     *
     * @param object
     * @return
     */
    public static TypeHandler getTypeHandler(Object object) {
        if (object instanceof Wd<?> wd) {
            return wd.getTypeHandler();
        }
        return DefaultTypeHandler.INSTANCE;
    }

    public static JdbcType getJdbcType(Object object) {
        if (object instanceof Wd<?> wd) {
            return wd.getJdbcType();
        }
        return null;
    }


    @Override
    public String toString() {
        return String.valueOf(value);
    }


}
