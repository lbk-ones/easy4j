package easy4j.sca.storage.test;

import easy4j.module.base.starter.Easy4JStarter;

@Easy4JStarter(
        serverPort = 10002,
        serverName = "test-storage",
        serviceDesc = "测试",
        author = "bokun.li",
        enableH2 = false,
        h2Url = "jdbc:h2:file:~/h2/testdb;DB_CLOSE_ON_EXIT=false"
        // 使用h2当数据库
)
/**
 * StartApp
 *
 * @author bokun.li
 * @date 2025-05
 */
public class StartApp {
}