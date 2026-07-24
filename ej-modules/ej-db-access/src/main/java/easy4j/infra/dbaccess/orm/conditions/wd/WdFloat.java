package easy4j.infra.dbaccess.orm.conditions.wd;

import lombok.EqualsAndHashCode;

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
public class WdFloat extends Wd<Float> implements Serializable {

    @Override
    public WdFloat of(String placeHolder, Float value) {
        return new WdFloat(placeHolder,value);
    }

    @Override
    public WdFloat of(Float value) {
        return new WdFloat(value);
    }

    public WdFloat() {
        super();
    }

    public WdFloat(String placeHolder, Float value) {
        super(placeHolder, value);
    }

    public WdFloat(Float value) {
        super(value);
    }

    public static WdFloat v(Float val){
        return new WdFloat(val);
    }

    public static WdFloat v(String prefix, Float val){
        return new WdFloat(prefix,val);
    }
}
