package easy4j.module.base.resolve;

/**
 * 数据处理抽象层
 * @param <T> 处理完成之后放到 t 去
 * @param <R> 传入参数
 */
public interface Easy4jResolve<T,R> {

    T handler(T t,R p);

}
