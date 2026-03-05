package easy4j.module.sca.flow;

import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.utils.EasyMap;

import java.util.List;

@Desc("流程SDK")
public interface ProcessSDK {

    /**
     * 开始流程
     */
    @Desc("开始流程，返回实例ID，实例ID需要在业务方保存")
    String startProcess(ProcessReq processReq);


    /**
     * 开始流程，返回实例ID，实例ID需要在业务方保存,并执行第一个任务
     * @param processReq
     * @return
     */
    @Desc("开始流程，返回实例ID，TASKID，实例ID需要在业务方保存,并执行第一个任务")
    TaskRes startProcessAndExeFirstTask(ProcessReq processReq);

    /**
     * 结束流程
     */
    @Desc("通过流程实例ID结束流程")
    void endProcess(String processInstanceId,String endReason);


    /**
     * 完成任务
     *
     * @param taskId 任务ID
     * @return 业务key: SDK会自动生成一个业务KEY来关联表单内容，就算没有传表单类容也会生成一个
     */
    @Desc("完成任务,processKey可以不传会自动推算")
    String completeTask(String taskId, String processKey, EasyMap<String, Object> variables);

    /**
     * 完成任务
     * 不需要传入表单ID 自动反查
     *
     * @param taskId 任务ID
     * @param processKey 流程定义ID 可以不传会自动推算
     * @param result 审批结果
     * @param comment 审批内容
     * @param formId  表单Id 可以不传 不传会推算这个任务节点对应的审批操作ID
     * @param formData 审批的时候动态表单对应的值
     * @param variables 传递给任务节点的变量
     * @return 业务key: SDK会自动生成一个业务KEY来关联表单内容，就算没有传表单类容也会生成一个
     */
    @Desc("完成任务,processKey可以不传会自动推算")
    String completeTask(String taskId, String processKey, String result,String comment,String formId, List<FormData> formData,EasyMap<String, Object> variables);

    /**
     * 查询代办任务信息集合
     *
     * @param assignee   要查询的人
     * @param processKey 流程定义的KEY
     * @return List<TaskRes>
     */
    @Desc("查询代办任务信息集合")
    List<TaskRes> queryPendingTasks(String assignee, String processKey);

    /**
     * 查询该流程任务信息列表
     * 审批信息列表
     *
     * @param instanceId 任务实例ID
     * @return List<TaskRes>
     */
    @Desc("查询该流程任务信息列表")
    List<TaskRes> queryTaskHistory(String instanceId);

    /**
     * 查询流程实例的历史信息（是否结束）
     *
     * @param processInstanceId 流程实例ID
     * @return 历史流程实例 ProcessInstanceRes
     */
    @Desc("查询流程实例的历史信息（是否结束）")
    ProcessInstanceRes queryProcessInstance(String processInstanceId);

    /**
     * 查询流程实例的历史信息（是否结束）
     *
     * @param processInstanceId 流程实例ID
     * @return 历史流程实例 ProcessInstanceRes
     */
    @Desc("批量查询流程实例的历史信息（是否结束）")
    List<ProcessInstanceRes> batchQueryProcessInstance(List<String> processInstanceId);
}
