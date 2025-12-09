package easy4j.infra.rpc.registry.jdbc;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.registry.Event;
import easy4j.infra.rpc.registry.SubscribeListener;
import lombok.extern.slf4j.Slf4j;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * jdbc循环检查管理
 * 默认10秒检查一次
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
public class JdbcLoopCheckManager implements AutoCloseable {
    JdbcOperate jdbcOperate;

    ScheduledExecutorService executorService;

    final long period;

    TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    Map<String, List<SubscribeListener>> subscribeListeners = new ConcurrentHashMap<>();
    private static final NamedThreadFactory namedThreadFactory = new NamedThreadFactory("e4j-rpc-jdbc-registry-check-", true);

    public JdbcLoopCheckManager(JdbcOperate jdbcOperate) {
        this.jdbcOperate = jdbcOperate;
        this.executorService = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
        E4jRpcConfig config = IntegratedFactory.getConfig();
        period = config.getRegistryJdbcCheckPeriod();
    }

    public void start() {
        this.executorService.scheduleAtFixedRate(new JdbcLoopCheckThread(), 0, period, timeUnit);
    }

    @Override
    public void close() {
        this.executorService.shutdown();
        subscribeListeners.clear();
    }

    public void addListener(String path, SubscribeListener subscribeListener) {
        if (null != subscribeListener && StrUtil.isNotEmpty(path)) {
            List<SubscribeListener> subscribeListeners1 = subscribeListeners.get(path);
            if (null == subscribeListeners1) {
                subscribeListeners1 = new CopyOnWriteArrayList<>();
                subscribeListeners.put(path, subscribeListeners1);
            }
            subscribeListeners1.add(subscribeListener);
        }
    }

    public void removeListener(String path) {
        if (StrUtil.isNotEmpty(path)) {
            subscribeListeners.remove(path);
        }
    }

    public boolean hasListener(String path) {
        return subscribeListeners.containsKey(path);
    }

    /**
     * JdbcLoopCheckThread
     */
    class JdbcLoopCheckThread implements Runnable {
        Map<String, SysE4jJdbcRegData> lastRegData;
        boolean lastExeIng = false;
        @Override
        public void run() {
            if (lastExeIng) return;
            lastExeIng = true;
            try{
                List<SysE4jJdbcRegData> sysE4jJdbcRegData = jdbcOperate.queryList(new SysE4jJdbcRegData(), SysE4jJdbcRegData.class);
                // fist is not handler
                if (lastRegData == null) {
                    lastRegData = sysE4jJdbcRegData
                            .stream()
                            .filter(e -> e.getDataKey() != null)
                            .collect(Collectors.toMap(SysE4jJdbcRegData::getDataKey, Function.identity(), (e1, e2) -> e2));
                    return;
                }
                Set<String> keys = subscribeListeners.keySet();
                Map<String, SysE4jJdbcRegData> newMap = sysE4jJdbcRegData
                        .stream()
                        .filter(e -> e.getDataKey() != null)
                        .collect(Collectors.toMap(SysE4jJdbcRegData::getDataKey, Function.identity(), (e1, e2) -> e2));
                List<Event> notifyEvent = new ArrayList<>();
                // judge is deleted
                for (String s : lastRegData.keySet()) {
                    SysE4jJdbcRegData sysE4jJdbcRegData1 = newMap.get(s);
                    if (sysE4jJdbcRegData1 == null) {
                        SysE4jJdbcRegData sysE4jJdbcRegData2 = lastRegData.get(s);
                        String dataKey = sysE4jJdbcRegData2.getDataKey();
                        String dataValue = sysE4jJdbcRegData2.getDataValue();
                        // 监听的key可能是前缀
                        for (String key : keys) {
                            if (StrUtil.startWith(dataKey, key)) {
                                Event event = Event.builder().key(key).data(dataValue).path(dataKey).type(Event.Type.REMOVE).build();
                                notifyEvent.add(event);
                            }
                        }
                    }
                }
                // add or update
                for (SysE4jJdbcRegData newValue : sysE4jJdbcRegData) {
                    String dataKey = newValue.getDataKey();
                    String newDataValue = newValue.getDataValue();
                    SysE4jJdbcRegData lastOne = lastRegData.get(dataKey);
                    Event.Type type = null;
                    if (lastOne == null) {
                        type = Event.Type.ADD;
                    } else {
                        String dataValue = lastOne.getDataValue();
                        if (!StrUtil.equals(newDataValue, dataValue)) {
                            type = Event.Type.UPDATE;
                        }
                    }
                    if (type != null) {
                        // listen key maybe is prefix
                        for (String key : keys) {
                            if (StrUtil.startWith(dataKey, key)) {
                                Event event = Event.builder().key(key).data(newDataValue).path(dataKey).type(type).build();
                                notifyEvent.add(event);
                            }
                        }
                    }
                }
                // notify
                Map<String, List<Event>> collect = notifyEvent.stream().filter(e -> e.key() != null).collect(Collectors.groupingBy(Event::key));
                for (String key : collect.keySet()) {
                    List<SubscribeListener> subscribeListeners1 = subscribeListeners.get(key);
                    if (subscribeListeners1 != null) {
                        List<Event> events = collect.get(key);
                        for (Event event : events) {
                            for (SubscribeListener subscribeListener : subscribeListeners1) {
                                subscribeListener.notify(event);
                            }
                        }
                    }
                }
                lastRegData = newMap;
            }finally {
                lastExeIng = false;
            }
        }
    }

}
