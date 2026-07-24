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
public class WdChar extends Wd<Character> implements Serializable {

    @Override
    public WdChar of(String placeHolder, Character value) {
        return new WdChar(placeHolder,value);
    }

    @Override
    public WdChar of(Character value) {
        return new WdChar(value);
    }

    public WdChar() {
        super();
    }

    public WdChar(String placeHolder, Character value) {
        super(placeHolder, value);
    }

    public WdChar(Character value) {
        super(value);
    }


    public static WdChar v(Character val){
        return new WdChar(val);
    }

    public static WdChar v(String prefix, Character val){
        return new WdChar(prefix,val);
    }
}
