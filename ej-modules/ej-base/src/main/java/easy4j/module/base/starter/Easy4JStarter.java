/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.base.starter;

import cn.hutool.extra.spring.EnableSpringUtil;

import easy4j.module.base.annotations.Desc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 正常注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
        excludeFilters = {@ComponentScan.Filter(
                type = FilterType.CUSTOM,
                classes = {TypeExcludeFilter.class}
        ), @ComponentScan.Filter(
                type = FilterType.CUSTOM,
                classes = {AutoConfigurationExcludeFilter.class}
        )}
)
/**
 * Easy4JStarter
 *
 * @author bokun.li
 * @date 2025-05
 */
@Import(value = EasyStarterImport.class)
@EnableSpringUtil
@MapperScan
public @interface Easy4JStarter {
    @AliasFor(
            annotation = EnableAutoConfiguration.class
    )
    Class<?>[] exclude() default {};

    @AliasFor(
            annotation = EnableAutoConfiguration.class
    )
    String[] excludeName() default {};

    @AliasFor(
            annotation = ComponentScan.class,
            attribute = "basePackages"
    )
    String[] scanBasePackages() default {};

    @AliasFor(
            annotation = ComponentScan.class,
            attribute = "basePackageClasses"
    )
    Class<?>[] scanBasePackageClasses() default {};

    @AliasFor(
            annotation = ComponentScan.class,
            attribute = "nameGenerator"
    )
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    @AliasFor(
            annotation = Configuration.class
    )
    boolean proxyBeanMethods() default true;

    int serverPort() default 8080;

    String serverName() default "easy4j-service";

    String serviceDesc() default "";

    String author() default "";

    boolean enableH2() default false;

    String h2Url() default "jdbc:h2:mem:testdb@easy4j:easy4j";

    @Desc("示例  jdbc:mysql://localhost:3306/order@root:123456")
    String ejDataSourceUrl() default "";


    String h2ConsoleUsername() default "easy4j";
    String h2ConsolePassword() default "easy4j";
}
