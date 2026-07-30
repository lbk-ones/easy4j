package easy4j.infra.dbaccess.orm;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.extra.spring.SpringUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.dbaccess.TempDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * IDBAccess 的 工厂类，通过这个工厂类创建的 IDBAccess，将全部继承，OrmProperties的spring全局配置, 除了 {@link OrmFactory#getInternal}这个方法
 *
 * @author bokun.li
 * @since 2.1.4
 */
public class OrmFactory {

    private final static Map<String, IDBAccess> CACHE_MAP = new ConcurrentHashMap<>();

    static {
        // 程序结束之后关闭连接池
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (IDBAccess value : CACHE_MAP.values()) {
                DBAccessImpl value1 = (DBAccessImpl) value;
                DataSource dataSource = value1.getAccessUtils().getAccessConfig().getDataSource();
                if (dataSource instanceof HikariDataSource d2) {
                    try {
                        d2.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }));
    }


    /**
     * 传入 config，不会缓存
     *
     * @param config                 配置类
     * @param isCopyGlobalProperties 是否copy全局配置表
     * @return IDBAccess
     */
    public static IDBAccess get(AccessConfig config, SpringOrmProperties ormProperties, boolean isCopyGlobalProperties) {
        if (config.getDataSource() == null) {
            config.setDataSource(SpringUtil.getBean(DataSource.class));
        }
        if (isCopyGlobalProperties) {
            copyProperties(ormProperties, config);
        }
        return new DBAccessImpl(config);
    }

    /**
     * 获取临时数据源
     *
     * @param url      jdbcUrl
     * @param username 用户名
     * @param password 密码
     * @return 数据源
     */
    public static DataSource getTempDataSource(String url, String username, String password) {
        return new TempDataSource(SqlType.getDriverClassNameByUrl(url), url, username, password);
    }

    /**
     * 传入 dataSource，不会缓存
     *
     * @param dataSource 数据源
     * @return IDBAccess
     */
    public static IDBAccess get(DataSource dataSource) {
        AccessConfig config = new AccessConfig();
        config.setDataSource(dataSource);
        return get(config, null, true);
    }

    /**
     * 内部使用的orm,全局配置不会覆盖传入的配置
     *
     * @param dataSource 数据源 可以为null,为null自动获取spring里面的dataSource
     * @return IDBAccess
     */
    public static IDBAccess getInternal(DataSource dataSource, Consumer<AccessConfig> configConsumer) {
        AccessConfig config = new AccessConfig();
        config.setDataSource(dataSource);
        configConsumer.accept(config);
        return get(config, null, false);
    }

    /**
     * 传入 连接信息，会缓存
     *
     * @param url      jdbc连接
     * @param username 用户名
     * @param password 密码
     * @return IDBAccess
     */
    public static IDBAccess getTempCache(String url, String username, String password) {
        String key = "temp_" + url + username + password;
        IDBAccess idbAccess = CACHE_MAP.get(key);
        if (idbAccess != null) {
            return idbAccess;
        } else {
            synchronized (CACHE_MAP) {
                if (CACHE_MAP.get(key) == null) {
                    CACHE_MAP.putIfAbsent(key, getTempNoCache(url, username, password));
                }
            }
            return CACHE_MAP.get(key);
        }

    }

    /**
     * 传入 连接信息，不缓存
     *
     * @param url      jdbc连接
     * @param username 用户名
     * @param password 密码
     * @return IDBAccess
     */
    public static IDBAccess getTempNoCache(String url, String username, String password) {
        AccessConfig config = new AccessConfig();
        config.setDataSource(getTempDataSource(url, username, password));
        return get(config, null, true);
    }

    /**
     * 传入 连接信息，会缓存
     *
     * @param url            jdbc连接
     * @param username       用户名
     * @param password       密码
     * @param configConsumer 配置
     * @return IDBAccess
     */
    public static IDBAccess getTempCache(String url, String username, String password, Consumer<AccessConfig> configConsumer) {
        String key = "temp_" + url + username + password;
        IDBAccess idbAccess = CACHE_MAP.get(key);
        if (idbAccess != null) {
            return idbAccess;
        } else {
            synchronized (CACHE_MAP) {
                if (CACHE_MAP.get(key) == null) {
                    CACHE_MAP.putIfAbsent(key, getTempNoCache(url, username, password, configConsumer));
                }
            }
            return CACHE_MAP.get(key);
        }

    }

    /**
     * 传入 连接信息，不缓存
     *
     * @param url      jdbc连接
     * @param username 用户名
     * @param password 密码
     * @return IDBAccess
     */
    public static IDBAccess getTempNoCache(String url, String username, String password, Consumer<AccessConfig> configConsumer) {
        AccessConfig config = new AccessConfig();
        if (configConsumer != null) {
            configConsumer.accept(config);
        }
        config.setDataSource(getTempDataSource(url, username, password));
        return get(config, null, true);
    }

    /**
     * 传入 连接信息，使用HikariDataSource 会缓存
     *
     * @param url            jdbc连接
     * @param username       用户名
     * @param password       密码
     * @param configConsumer 配置
     * @return IDBAccess
     */
    public static IDBAccess getHikari(String url, String username, String password, BiConsumer<HikariConfig, AccessConfig> configConsumer) {
        String key = "hikari_" + url + "_" + username;
        IDBAccess idbAccess = CACHE_MAP.get(key);
        if (idbAccess != null) {
            return idbAccess;
        } else {
            synchronized (CACHE_MAP) {
                if (CACHE_MAP.get(key) == null) {
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(url);
                    config.setUsername(username);
                    config.setPassword(password);

                    // 数据库连接信息
                    config.setDriverClassName(SqlType.getDriverClassNameByUrl(url));

                    // 连接池基本配置
                    config.setMaximumPoolSize(20);           // 最大连接数
                    config.setMinimumIdle(5);                // 最小空闲连接数
                    config.setConnectionTimeout(30000);      // 连接超时时间（毫秒）
                    config.setIdleTimeout(600000);           // 空闲连接超时时间（毫秒，10分钟）
                    config.setMaxLifetime(1800000);          // 连接最大生命周期（毫秒，30分钟）

                    // 连接验证配置
                    config.setConnectionTestQuery(SqlType.getValidateSqlByUrl(url));  // 测试连接有效性的SQL
                    config.setValidationTimeout(5000);          // 验证超时时间（毫秒）
                    config.setLeakDetectionThreshold(60000);    // 泄漏检测阈值（毫秒）

                    // 其他配置
                    config.setAutoCommit(true);              // 自动提交
                    config.setReadOnly(false);               // 不是只读
                    String s = MD5.create().digestHex(key);
                    config.setPoolName(key.replaceAll(":", "-"));      // 连接池名称
                    config.setInitializationFailTimeout(1);  // 初始化失败超时（秒）
                    config.setIsolateInternalQueries(false); // 隔离内部查询
                    config.setRegisterMbeans(true); // 注册到
                    AccessConfig accessConfig = new AccessConfig();
                    if (configConsumer != null) {
                        configConsumer.accept(config, accessConfig);
                    }
                    HikariDataSource hikariDataSource = new HikariDataSource(config);
                    accessConfig.setDataSource(hikariDataSource);
                    IDBAccess idbAccess1 = get(accessConfig, null, true);
                    CACHE_MAP.putIfAbsent(key, idbAccess1);
                }
            }
            return CACHE_MAP.get(key);
        }
    }


    public static void copyProperties(SpringOrmProperties ormProperties, AccessConfig accessConfig) {
        if (ormProperties == null) {
            try {
                ormProperties = SpringUtil.getBean(SpringOrmProperties.class);
            } catch (Exception ignored) {
            }
        }
        if (ormProperties != null) {
            CopyOptions copyOptions = CopyOptions
                    .create()
                    .setIgnoreNullValue(true)
                    .setIgnoreProperties("plugins");
            BeanUtil.copyProperties(ormProperties, accessConfig, copyOptions);
        }

    }
}
