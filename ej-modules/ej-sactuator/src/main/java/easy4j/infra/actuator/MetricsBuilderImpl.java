package easy4j.infra.actuator;

import com.google.common.collect.Maps;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

import java.util.Map;

public class MetricsBuilderImpl implements MetricsBuilder{


    private final static Map<String,Counter> counterMap = Maps.newConcurrentMap();
    private final static Map<String,Timer> timerMap = Maps.newConcurrentMap();


    @Override
    public Counter buildCounter(String name, String desc, Iterable<Tag> tags) {
        return counterMap.computeIfAbsent(name,na-> Counter.builder(na).description(desc).tags(tags).register(Metrics.globalRegistry));
    }

    @Override
    public Timer buildTimer(String name, String desc, Iterable<Tag> tags) {
        return timerMap.computeIfAbsent(name,na-> Timer.builder(na).description(desc).tags(tags).register(Metrics.globalRegistry));
    }

    @Override
    public Counter getCounter(String name) {
        return counterMap.get(name);
    }

    @Override
    public Timer getTimer(String name) {
        return timerMap.get(name);
    }
}
