package io.github.lbkones.config.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Properties;

public interface ConfigChange {

    void change(String key,Map<@Nullable String, @Nullable Object> properties);

}
