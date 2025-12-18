package easy4j.module.sca.flow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@Schema(description = "代办任务信息")
public class TaskRes implements Serializable {

    @Schema(description = "任务ID")
    String taskId;


    @Schema(description = "要审批的人")
    String assignee;

    @Schema(description = "流程定义KEY")
    String processKey;

    @Schema(description = "任务名称")
    String name;

    @Schema(description = "流程实例ID")
    String instanceId;

    @Schema(description = "创建时间")
    Date createTime;

    @Schema(description = "合并之后的变量集合")
    Map<String, Object> variables;

    @Schema(description = "未合并的本地变量")
    Map<String, Object> localVariables;

}
