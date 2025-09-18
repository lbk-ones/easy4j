package easy4j.infra.actuator;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

/**
 * 指标构建器
 * 时间指标，指数指标
 * @author bokun.li
 * @date 2025/9/18
 */
public interface MetricsBuilder {

    /**
     * 构建counter指标
     * @param name 指标名称
     * @param desc 指标描述
     * @param tags 指标tag
     * @return io.micrometer.core.instrument.Counter
     */
    Counter buildCounter(String name, String desc, Iterable<Tag> tags);

    /**
     * 构建Timer指标
     * @param name 指标名称
     * @param desc 指标描述
     * @param tags 指标tag
     * @return io.micrometer.core.instrument.Timer
     */
    Timer buildTimer(String name, String desc, Iterable<Tag> tags);


    /**
     * 根据name获取Counter指标
     * @param name
     * @return
     */
    Counter getCounter(String name);


    /**
     *
     * @param name
     * @return
     */
    Timer getTimer(String name);

}
