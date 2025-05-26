package easy4j.module.base.starter;

import cn.hutool.extra.spring.EnableSpringUtil;
import easy4j.module.base.annotations.Desc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 用于测试
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(value = EasyStarterImport.class)
@EnableSpringUtil
@MapperScan
public @interface Easy4JStarterTest {

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
