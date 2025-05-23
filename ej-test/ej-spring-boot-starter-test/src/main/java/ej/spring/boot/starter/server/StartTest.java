package ej.spring.boot.starter.server;


import easy4j.module.base.starter.Easy4JStarter;
import org.springframework.boot.SpringApplication;

@Easy4JStarter(
        serverPort = 9051,
        serverName = "test-ej-service",
        enableH2 = true
)
public class StartTest {
    public static void main(String[] args) {
        SpringApplication.run(StartTest.class, args);
    }

}
