package ej.spring.boot.starter.test;

import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.sentinel.EnableFlowDegrade;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Easy4JStarter(
        serverPort = 10001,
        serverName = "test-server",
        serviceDesc = "测试",
        author = "bokun.li",
        enableH2 = false,
        h2Url = "jdbc:h2:file:~/h2/testdb;DB_CLOSE_ON_EXIT=false"
        // 使用h2当数据库
)
@EnableFlowDegrade
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
