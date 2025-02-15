package easy4j.module.base.plugin.doc;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1、分组接口
 * 2、自定义接口文档
 */
@Configuration
public class OpenAPIConfig {


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OpenApiDoc")
                        .description("api 文档")
                        .version("1.0.0"));
    }

}