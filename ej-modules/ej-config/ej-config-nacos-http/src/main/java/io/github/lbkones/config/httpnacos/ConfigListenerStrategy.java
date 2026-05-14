package io.github.lbkones.config.httpnacos;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ==================== 核心接口 ====================
 */

/**
 * 配置监听策略接口 - 抽象不同版本的监听逻辑
 */
public interface ConfigListenerStrategy {
    
    /**
     * 启动监听
     */
    void startListening(String dataId, String group, ConfigChangeCallback callback) throws Exception;
    
    /**
     * 停止监听
     */
    void stopListening(String dataId, String group);
    
    /**
     * 获取策略名称
     */
    String getStrategyName();
}
