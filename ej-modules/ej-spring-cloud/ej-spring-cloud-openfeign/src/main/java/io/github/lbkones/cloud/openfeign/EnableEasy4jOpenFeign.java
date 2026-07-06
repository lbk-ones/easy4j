package io.github.lbkones.cloud.openfeign;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableFeignClients
@Import({FeignFallbackFactoryRegistrar.class})
public @interface EnableEasy4jOpenFeign {
}
