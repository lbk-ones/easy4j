package template.service.order;

import easy4j.infra.base.starter.Easy4JStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Hello world!
 *
 */
@Easy4JStarter
@EnableFeignClients(basePackages = "template.service.api.client")
public class OrderApp
{
    public static void main( String[] args )
    {
        SpringApplication.run(OrderApp.class,args);
    }
}
