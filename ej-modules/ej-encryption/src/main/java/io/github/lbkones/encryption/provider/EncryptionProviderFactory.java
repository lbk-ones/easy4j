package io.github.lbkones.encryption.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加密提供者工厂，管理所有加密方式
 */
public class EncryptionProviderFactory {

    private static final Map<String, EncryptionProvider> providers = new ConcurrentHashMap<>();

    /**
     * 注册加密提供者
     */
    public static void register(String name, EncryptionProvider provider) {
        providers.put(name, provider);
    }

    /**
     * 获取加密提供者
     */
    public static EncryptionProvider get(String name) {
        EncryptionProvider encryptionProvider = providers.get(name);
        if (encryptionProvider == null) {
            encryptionProvider = providers.get(name.toLowerCase());
            if (encryptionProvider == null) {
                encryptionProvider = providers.get(name.toUpperCase());
            }
        }
        return encryptionProvider;
    }

    /**
     * 移除加密提供者
     */
    public static void remove(String name) {
        providers.remove(name);
    }

    /**
     * 获取所有加密提供者
     */
    public static Map<String, EncryptionProvider> getAll() {
        return new ConcurrentHashMap<>(providers);
    }
}
