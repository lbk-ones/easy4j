package easy4j.module.sauth.enums;

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
