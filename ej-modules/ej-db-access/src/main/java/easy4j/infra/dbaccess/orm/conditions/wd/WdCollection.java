package easy4j.infra.dbaccess.orm.conditions.wd;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Collection;

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
public class WdCollection extends Wd<Collection<?>> implements Serializable {

    @Override
    public WdCollection of(String placeHolder, Collection<?> value) {
        return new WdCollection(placeHolder,value);
    }

    @Override
    public WdCollection of(Collection<?> value) {
        return new WdCollection(value);
    }

    public WdCollection() {
        super();
    }

    public WdCollection(String placeHolder, Collection<?> value) {
        super(placeHolder, value);
    }

    public WdCollection(Collection<?> value) {
        super(value);
    }

    public static WdCollection v(Collection<?> val){
        return new WdCollection(val);
    }

    public static WdCollection v(String prefix, Collection<?> val){
        return new WdCollection(prefix,val);
    }
}
