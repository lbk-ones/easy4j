package easy4j.infra.knife4j;

import easy4j.infra.common.utils.SysConstant;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

// 标记在类或方法上，统一返回 ApiResult
@Target({PARAMETER, METHOD, FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Parameter(name = SysConstant.X_ACCESS_TOKEN,in = ParameterIn.HEADER,required = true,description = "访问授权token",schema =@Schema(type="String"))
public @interface GlobalXAccessToken {
}