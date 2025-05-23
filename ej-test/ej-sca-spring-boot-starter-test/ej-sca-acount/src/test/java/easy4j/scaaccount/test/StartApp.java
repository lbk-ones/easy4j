package easy4j.scaaccount.test;

import easy4j.module.base.starter.Easy4JStarter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@Easy4JStarter(
        serverPort = 100010,
        serverName = "test-account",
        serviceDesc = "测试",
        author = "bokun.li",
        enableH2 = true,
        h2Url = "jdbc:h2:mem:testaccount"
        // 使用h2当数据库
)
@EnableDiscoveryClient
@SpringBootTest
public class StartApp {

   @Autowired
   DiscoveryClient discoveryClient;

    @Test
    void testDiscoveryClient(){
        List<ServiceInstance> instances = discoveryClient.getInstances("my-service");
        for (ServiceInstance instance : instances) {
            String host = instance.getHost() + ":" + instance.getPort();
            System.out.println(host);
        }
    }
}
