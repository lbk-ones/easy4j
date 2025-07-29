package easy4j.infra.knife4j;

import easy4j.infra.common.header.EasyResult;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 标记在类或方法上，统一返回 ApiResult
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "操作成功,返回通用返回体EasyResult",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(
                                implementation = EasyResult.class
                        )
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "服务器内部错误",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = EasyResult.class)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "未找到接口",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = J404.class)
                )
        )
})
public @interface GlobalApiResponses {
}