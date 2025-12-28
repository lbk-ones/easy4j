package template.service.order;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.module.mybatisplus.codegen.servlet.EnableCodeGen;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Hello world!
 */
@Easy4JStarter
@EnableFeignClients(basePackages = "template.service.api.client")
@EnableCodeGen
public class OrderApp {
    public static void main(String[] args) {
        SpringApplication.run(OrderApp.class, args);
    }
}
