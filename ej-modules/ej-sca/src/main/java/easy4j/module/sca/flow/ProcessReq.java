package easy4j.module.sca.flow;

import easy4j.infra.common.utils.EasyMap;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "流程sdk请求体")
public class ProcessReq  implements Serializable {

    /**
     * 流程部署ID
     */
    @Schema(description = "流程部署ID")
    String deployId;

    /**
     * 实例ID
     */
    @Schema(description = "实例ID")
    String instanceId;

    /**
     * 结束实例的理由
     */
    @Schema(description = "结束实例的理由")
    String endInstanceReason;

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    String taskId;

    /**
     * 流程定义key
     */
    @Schema(description = "流程定义key")
    String processKey;

    /**
     * 业务key,由业务方定，用来关联流程引擎实例的
     */
    @Schema(description = "业务key,由业务方定，用来关联流程引擎实例的")
    String businessKey;


    /**
     * 审批开始提交的表单内容，没使用动态表单则忽略
     */
    @Schema(description = "审批开始提交的表单内容，没使用动态表单则忽略")
    EasyMap<String, Object> fromData;

    /**
     * 定义任务变量，完成任务的时候传入
     */
    @Schema(description = "定义任务变量，完成任务的时候传入")
    EasyMap<String, Object> taskVariables;

    /**
     * 分配人
     */
    @Schema(description = "分配人")
    String assignee;

    /**
     * 发起人
     */
    @Schema(description = "发起人")
    String initiator;

    /**
     * 审批结果
     */
    @Schema(description = "审批结果")
    String approvalResult;

    /**
     * 审批建议
     */
    @Schema(description = "审批建议")
    String approvalComment;
}
