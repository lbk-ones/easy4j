package io.github.lbkones.registry.nacos;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableDiscoveryClient
public @interface EnableEasy4jDiscoveryClient {
}
