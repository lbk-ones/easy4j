package easy4j.module.base.context;

import java.util.Optional;

public interface Easy4jContext {

    void registerThreadHash(String key, String key2, Object value);

    Optional<Object> getThreadHashValue(String key, String key2);

    Optional<Object> getThreadHash(String key);

    void clearHash();

    /**
     * 以 aclass 全类名作为default类型的key t作为值
     *
     * @param aclass
     * @param t
     */
    <T, R extends T> void set(Class<T> aclass, R t);

    /**
     * 以 aclass 全类名作为type类型的key t作为值
     *
     * @param aclass
     * @param t
     */
    void setType(String type, Class<?> aclass, Object t);

    <T> T getType(String type, Class<T> aclass);

    void set(String name, Object t);

    void setType(String type, String name, Object t);

    <T> T getType(String type, String name, Class<T> t);

    <T> T get(Class<T> aclass);

    <T> T get(String name, Class<T> aclass);


}
