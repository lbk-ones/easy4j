package easy4j.infra.knife4j;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description = "HTTP状态码404的返回值")
public class J404 {

    @Schema(description = "时间戳")
    private String timestamp;
    @Schema(description = "状态404",defaultValue = "404")
    private Integer status;
    @Schema(description = "错误信息：Not Found")
    private String error;
    @Schema(description = "请求路径")
    private String path;
}
