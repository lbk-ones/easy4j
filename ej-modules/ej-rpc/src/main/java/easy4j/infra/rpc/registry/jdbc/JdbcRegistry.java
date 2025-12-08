package easy4j.infra.rpc.registry.jdbc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import easy4j.infra.rpc.exception.RegistryException;
import easy4j.infra.rpc.heart.NodeHeartbeatInfo;
import easy4j.infra.rpc.heart.NodeHeartbeatManager;
import easy4j.infra.rpc.registry.ConnectionListener;
import easy4j.infra.rpc.registry.Registry;
import easy4j.infra.rpc.registry.SubscribeListener;
import lombok.NonNull;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dataKey 代表最前面的路径（除了最后面的一位）
 * dataKey 最后的一位路径
 *
 * @author bokun
 * @since 2.0.1
 */
public class JdbcRegistry implements Registry {


    final JdbcOperate jdbcOperate;

    final JdbcLoopCheckManager jdbcLoopCheckManager;

    final EphemeralDataManager ephemeralDataManager;


    public JdbcRegistry(JdbcOperate jdbcOperate) {
        this.jdbcOperate = jdbcOperate;
        this.jdbcLoopCheckManager = new JdbcLoopCheckManager(jdbcOperate);
        this.ephemeralDataManager = new EphemeralDataManager(jdbcOperate);
    }

    @Override
    public void start() {
        this.jdbcLoopCheckManager.start();
    }

    @Override
    public boolean isConnected() {
        SysE4jJdbcRegData sysE4jJdbcRegData = new SysE4jJdbcRegData();
        try {
            int count = jdbcOperate.count(sysE4jJdbcRegData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void connectUntilTimeout(@NonNull Duration timeout) throws RegistryException {

    }

    @Override
    public void addConnectionStateListener(ConnectionListener listener) {

    }

    @Override
    public void subscribe(String path, SubscribeListener listener) {
        this.jdbcLoopCheckManager.addListener(path, listener);
    }

    @Override
    public boolean isSubscribe(String path) {
        return this.jdbcLoopCheckManager.hasListener(path);
    }

    @Override
    public void unsubscribe(String path) {
        this.jdbcLoopCheckManager.removeListener(path);
    }

    @Override
    public String get(String key) throws RegistryException {
        SysE4jJdbcRegData sysE4jJdbcRegData = new SysE4jJdbcRegData();
        sysE4jJdbcRegData.setDataKey(key);
        List<SysE4jJdbcRegData> regData = jdbcOperate.queryList(sysE4jJdbcRegData, SysE4jJdbcRegData.class, "data_key", "data_value");
        if (CollUtil.isNotEmpty(regData)) {
            SysE4jJdbcRegData sysE4jJdbcRegData1 = regData.get(0);
            return sysE4jJdbcRegData1.getDataValue();
        }
        return null;
    }

    /**
     * jdbc 的临时节点
     *
     * @param key                the key, cannot be null
     * @param value              the value, cannot be null
     * @param deleteOnDisconnect if true, when the connection state is disconnected, the key will be deleted
     */
    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        SysE4jJdbcRegData where = new SysE4jJdbcRegData();
        where.setDataKey(key);
        Long id;
        List<SysE4jJdbcRegData> sysE4jJdbcRegData1 = jdbcOperate.queryList(where, SysE4jJdbcRegData.class);
        if (CollUtil.isNotEmpty(sysE4jJdbcRegData1)) {
            Entity dataValue = Entity.create("sys_e4j_jdbc_reg_data").set("data_value", value).set("last_update_date", new Date());
            jdbcOperate.update(dataValue, Entity.create().set("data_key", key));
            id = sysE4jJdbcRegData1.get(0).getId();
        } else {
            SysE4jJdbcRegData sysE4jJdbcRegData = new SysE4jJdbcRegData();
            sysE4jJdbcRegData.setDataKey(key);
            sysE4jJdbcRegData.setDataValue(value);
            sysE4jJdbcRegData.setCreateDate(new Date());
            if (deleteOnDisconnect) {
                sysE4jJdbcRegData.setDataType("0");
            } else {
                sysE4jJdbcRegData.setDataType("1");
            }
            Object insert = jdbcOperate.insert(sysE4jJdbcRegData);
            id = ((Long) insert);
        }
        if (deleteOnDisconnect && id != null) {
            ephemeralDataManager.addEphemeralId(id);
        }
    }

    @Override
    public void delete(String key) {
        SysE4jJdbcRegData sysE4jJdbcRegData = new SysE4jJdbcRegData();
        sysE4jJdbcRegData.setDataKey(key);
        jdbcOperate.delete(sysE4jJdbcRegData);
    }

    @Override
    public Collection<String> children(String key) {
        if (!key.endsWith("/")) {
            key = key + "/";
        }
        final String finalKey = key;
        SysE4jJdbcRegData sysE4jJdbcRegData = new SysE4jJdbcRegData();
        sysE4jJdbcRegData.setDataKey("LIKE " + key + "%");
        List<SysE4jJdbcRegData> regData = jdbcOperate.queryList(sysE4jJdbcRegData, SysE4jJdbcRegData.class, "data_key");
        if (CollUtil.isNotEmpty(regData)) {
            return regData.stream().map(e -> {
                String s = StrUtil.replaceFirst(e.getDataKey(), finalKey, "");
                return s.substring(0, s.indexOf("/"));
            }).distinct().collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean exists(String key) {
        SysE4jJdbcRegData sysE4jJdbcRegData = new SysE4jJdbcRegData();
        sysE4jJdbcRegData.setDataKey(key);
        return jdbcOperate.exists(sysE4jJdbcRegData);
    }

    @Override
    public boolean acquireLock(String key) {
        return false;
    }

    @Override
    public boolean acquireLock(String key, long timeout) {
        return false;
    }

    @Override
    public boolean releaseLock(String key) {
        return false;
    }

    @Override
    public void close() throws IOException {
        this.jdbcLoopCheckManager.close();
        try {
            this.ephemeralDataManager.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
