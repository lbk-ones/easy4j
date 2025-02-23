package easy4j.module.idempotent;

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
