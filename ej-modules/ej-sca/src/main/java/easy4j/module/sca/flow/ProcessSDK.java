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
     */
    @Desc("完成任务")
    void completeTask(String taskId, String processKey, EasyMap<String, Object> variables);

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
}
