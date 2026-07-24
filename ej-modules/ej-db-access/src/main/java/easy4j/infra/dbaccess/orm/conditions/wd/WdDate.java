package easy4j.infra.dbaccess.orm.conditions.wd;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

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
public class WdDate extends Wd<Date> implements Serializable {

    @Override
    public WdDate of(String placeHolder, Date value) {
        return new WdDate(placeHolder,value);
    }

    @Override
    public WdDate of(Date value) {
        return new WdDate(value);
    }

    public WdDate() {
        super();
    }

    public WdDate(String placeHolder, Date value) {
        super(placeHolder, value);
    }

    public WdDate(Date value) {
        super(value);
    }

    public static WdDate v(Date val){
        return new WdDate(val);
    }

    public static WdDate v(String prefix, Date val){
        return new WdDate(prefix,val);
    }
}
