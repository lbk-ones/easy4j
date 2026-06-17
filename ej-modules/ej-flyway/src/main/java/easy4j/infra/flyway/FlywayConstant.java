package easy4j.infra.flyway;

/**
 * flyway相关常量抽离出来
 */
public class FlywayConstant {

    public static final String FLYWAY_LOCATION = "spring.flyway.locations";
    public static final String FLYWAY_CLASS_PATH_PREFIX = "classpath:db/migration/";
    public static final String FLYWAY_TABLE = "spring.flyway.table";
    public static final String FLYWAY_OUT_OF_ORDER = "spring.flyway.out-of-order";
    public static final String FLYWAY_CLEAN_DISABLED = "spring.flyway.clean-disabled";
    public static final String FLYWAY_BASELINE_ON_MIGRATE = "spring.flyway.baseline-on-migrate";
    public static final String FLYWAY_BASELINE_VERSION = "spring.flyway.baseline-version";
    public static final String FLYWAY_VALIDATE_ON_MIGRATE = "spring.flyway.validate-on-migrate";
    public static final String FLYWAY_ENABLED = "spring.flyway.enabled";
    public static final String FLYWAY_URL = "spring.flyway.url";
    public static final String FLYWAY_DRIVER_CLASS_NAME = "spring.flyway.driver-class-name";
    public static final String FLYWAY_USER = "spring.flyway.user";
    public static final String FLYWAY_PASSWORD = "spring.flyway.password";

}
