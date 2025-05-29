package easy4j.module.idempotent;

/**
 * StorageTypeEnum
 *
 * @author bokun.li
 * @date 2025-05
 */
public enum StorageTypeEnum {

    DB("db"),

    REDIS("redis");

    private String type;

    StorageTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}