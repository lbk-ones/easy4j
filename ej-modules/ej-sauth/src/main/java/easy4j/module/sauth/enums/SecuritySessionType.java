package easy4j.module.sauth.enums;

/**
 * SecuritySessionType
 *
 * @author bokun.li
 * @date 2025-05
 */
public enum SecuritySessionType {

    DB,

    REDIS;

    public static SecuritySessionType get(String name) {
        for (SecuritySessionType value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }
}