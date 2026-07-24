package easy4j.infra.dbaccess.orm.conditions.wd;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

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
public class WdLocalDateTime extends Wd<LocalDateTime> implements Serializable {

    @Override
    public WdLocalDateTime of(String placeHolder, LocalDateTime value) {
        return new WdLocalDateTime(placeHolder,value);
    }

    @Override
    public WdLocalDateTime of(LocalDateTime value) {
        return new WdLocalDateTime(value);
    }

    public WdLocalDateTime() {
        super();
    }

    public WdLocalDateTime(String placeHolder, LocalDateTime value) {
        super(placeHolder, value);
    }

    public WdLocalDateTime(LocalDateTime value) {
        super(value);
    }

    public static WdLocalDateTime v(LocalDateTime val){
        return new WdLocalDateTime(val);
    }

    public static WdLocalDateTime v(String prefix, LocalDateTime val){
        return new WdLocalDateTime(prefix,val);
    }
}
