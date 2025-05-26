package easy4j.module.base.resolve;

/**
 * 标准抽象类 无泛型
 */
public abstract class StandAbstractEasy4jResolve extends AbstractEasy4jResolve<Object,Object> {
    @Override
    public Object handler(Object s, Object p) {
        return s;
    }
}
