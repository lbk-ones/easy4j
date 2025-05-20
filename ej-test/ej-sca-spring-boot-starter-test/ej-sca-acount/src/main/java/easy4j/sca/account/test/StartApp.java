package easy4j.sca.account.test;

import easy4j.module.base.starter.Easy4JStarter;
import org.springframework.boot.SpringApplication;

@Easy4JStarter(
        serverPort = 10001,
        serverName = "test-account",
        serviceDesc = "测试",
        author = "bokun.li",
        enableH2 = true,
        h2Url = "jdbc:h2:mem:testaccount"
        // 使用h2当数据库
)
public class StartApp {
    public static void main(String[] args) {
        SpringApplication.run(StartApp.class, args);
    }
}
