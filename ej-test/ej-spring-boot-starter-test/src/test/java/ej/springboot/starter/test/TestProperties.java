package ej.springboot.starter.test;

import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.sentinel.EnableFlowDegrade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@Easy4JStarter(
        serverPort = 10001,
        serverName = "build-server",
        serviceDesc = "测试服务",
        author = "bokun.li",
        enableH2 = true
        // 使用h2当数据库
)
@EnableFlowDegrade
@SpringBootTest(classes = TestProperties.class)
public class TestProperties {

    @Autowired
    Environment environment;

    @Test
    void testProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        System.out.println(activeProfiles);
        System.out.println(defaultProfiles);
    }
}
