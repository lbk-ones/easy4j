package easy4j.module.sca.flow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(description = "实例信息")
public class ProcessInstanceRes implements Serializable {

    @Schema(description = "实例ID")
    String instanceId;

    @Schema(description = "业务关联ID,开启实例穿了的才会有值")
    String businessKey;

    @Schema(description = "开始时间")
    Date startTime;

    @Schema(description = "结束时间")
    Date endTime;

}
