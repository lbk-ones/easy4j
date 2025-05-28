package easy4j.module.base.context;

import java.util.Optional;

public interface Easy4jContext {

    void registerThreadHash(String key, String key2, Object value);

    Optional<Object> getThreadHashValue(String key, String key2);

    Optional<Object> getThreadHash(String key);

    void clearHash();

    void registerSingleton(Class<?> aclass, Object t);

    void registerSingleton(String name, Object t);

    <T> T getSingleton(Class<T> aclass);

    <T> T getSingleton(String name, Class<T> aclass);


}
