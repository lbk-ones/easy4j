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
public class WdBool extends Wd<Boolean> implements Serializable {

    @Override
    public WdBool of(String placeHolder, Boolean value) {
        return new WdBool(placeHolder,value);
    }

    @Override
    public WdBool of(Boolean value) {
        return new WdBool(value);
    }

    public WdBool() {
        super();
    }

    public WdBool(String placeHolder, Boolean value) {
        super(placeHolder, value);
    }

    public WdBool(Boolean value) {
        super(value);
    }

    public static WdBool v(Boolean val){
        return new WdBool(val);
    }

    public static WdBool v(String prefix, Boolean val){
        return new WdBool(prefix,val);
    }
}
