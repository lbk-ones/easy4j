package easy4j.module.sca.flow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "动态表单内容")
@Data
public class FormData implements Serializable {

    @Schema(description = "字段名称")
    private String dataKey;

    @Schema(description = "字段值")
    private String dataValue;

    @Schema(description = "大字段")
    private String largeTextValue;

    @Schema(description = "二进制")
    private byte[] dataBytesA;

}
