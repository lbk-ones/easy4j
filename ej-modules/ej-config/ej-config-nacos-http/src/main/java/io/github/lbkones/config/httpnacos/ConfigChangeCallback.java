package io.github.lbkones.config.httpnacos;

/**
 * 配置变更回调
 */
public interface ConfigChangeCallback {
    void onConfigChanged(String dataId, String group, String newContent);
}
