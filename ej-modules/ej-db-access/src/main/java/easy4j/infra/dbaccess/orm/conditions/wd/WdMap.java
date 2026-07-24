package easy4j.infra.dbaccess.orm.conditions.wd;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

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
public class WdMap extends Wd<Map<Object,Object>> implements Serializable {

    @Override
    public WdMap of(String placeHolder, Map<Object,Object> value) {
        return new WdMap(placeHolder,value);
    }

    @Override
    public WdMap of(Map<Object,Object> value) {
        return new WdMap(value);
    }

    public WdMap() {
        super();
    }

    public WdMap(String placeHolder, Map<Object,Object> value) {
        super(placeHolder, value);
    }

    public WdMap(Map<Object,Object> value) {
        super(value);
    }


    public static WdMap v(Map<Object,Object> val){
        return new WdMap(val);
    }

    public static WdMap v(String prefix, Map<Object,Object> val){
        return new WdMap(prefix,val);
    }
}
