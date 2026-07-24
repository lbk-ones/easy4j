package easy4j.infra.dbaccess.orm.conditions.wd;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

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
public class WdBigDecimal extends Wd<BigDecimal> implements Serializable {

    @Override
    public WdBigDecimal of(String placeHolder, BigDecimal value) {
        return new WdBigDecimal(placeHolder,value);
    }

    @Override
    public WdBigDecimal of(BigDecimal value) {
        return new WdBigDecimal(value);
    }

    public static WdBigDecimal v(BigDecimal val){
        return new WdBigDecimal(val);
    }

    public static WdBigDecimal v(String prefix, BigDecimal val){
        return new WdBigDecimal(prefix,val);
    }

    public WdBigDecimal() {
        super();
    }

    public WdBigDecimal(String placeHolder, BigDecimal value) {
        super(placeHolder, value);
    }

    public WdBigDecimal(BigDecimal value) {
        super(value);
    }
}
