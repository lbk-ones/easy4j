package ej.spring.boot.nacos;


import easy4j.infra.base.starter.Easy4JStarterNd;
import org.springframework.boot.SpringApplication;

@Easy4JStarterNd(
        serverPort = 9091,
        serverName = "nacos-server-embed"
)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
