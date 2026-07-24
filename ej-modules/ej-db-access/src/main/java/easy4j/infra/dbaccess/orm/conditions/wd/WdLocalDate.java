package easy4j.infra.dbaccess.orm.conditions.wd;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

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
public class WdLocalDate extends Wd<LocalDate> implements Serializable {

    @Override
    public WdLocalDate of(String placeHolder, LocalDate value) {
        return new WdLocalDate(placeHolder,value);
    }

    @Override
    public WdLocalDate of(LocalDate value) {
        return new WdLocalDate(value);
    }

    public WdLocalDate() {
        super();
    }

    public WdLocalDate(String placeHolder, LocalDate value) {
        super(placeHolder, value);
    }

    public WdLocalDate(LocalDate value) {
        super(value);
    }

    public static WdLocalDate v(LocalDate val){
        return new WdLocalDate(val);
    }

    public static WdLocalDate v(String prefix, LocalDate val){
        return new WdLocalDate(prefix,val);
    }
}
