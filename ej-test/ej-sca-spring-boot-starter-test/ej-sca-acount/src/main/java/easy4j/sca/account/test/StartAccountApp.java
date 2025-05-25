package easy4j.sca.account.test;

import easy4j.module.base.starter.Easy4JStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Easy4JStarter
@EnableDiscoveryClient
@EnableFeignClients
public class StartAccountApp {
    public static void main(String[] args) {
        SpringApplication.run(StartAccountApp.class, args);
    }
}
