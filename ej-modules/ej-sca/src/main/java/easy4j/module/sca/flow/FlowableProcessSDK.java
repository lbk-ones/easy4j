package easy4j.module.sca.flow;

import cn.hutool.core.convert.Convert;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.context.api.sca.Easy4jNacosInvokerApi;
import easy4j.infra.context.api.sca.NacosInvokeDto;
import lombok.Setter;

import java.util.List;

/**
 * flowable远程服务调用
 *
 * @author bokun
 */
public class FlowableProcessSDK implements ProcessSDK {

    Easy4jNacosInvokerApi easy4jNacosInvokerApi;
    @Setter
    String serverName;
    @Setter
    String group;
    public static final String PATH = "/flow/flowable";
    public static final String START_PROCESS = PATH + SP.SLASH + "startProcess";
    public static final String START_PROCESS_AND_EXE_FIRST_TASK = PATH + SP.SLASH + "startProcessAndExeFirstTask";
    public static final String END_PROCESS = PATH + SP.SLASH + "endProcess";
    public static final String COMPLETE_TASK = PATH + SP.SLASH + "completeTask";
    public static final String QUERY_PENDING_TASKS = PATH + SP.SLASH + "queryPendingTasks";
    public static final String QUERY_TASK_HISTORY = PATH + SP.SLASH + "queryTaskHistory";
    public static final String QUERY_PROCESS_INSTANCE = PATH + SP.SLASH + "queryProcessInstance";

    public static ProcessSDK get(String group, String serverName) {
        FlowableProcessSDK flowableProcessSDK = new FlowableProcessSDK();
        flowableProcessSDK.setGroup(group);
        flowableProcessSDK.setServerName(serverName);
        return flowableProcessSDK;
    }

    public static ProcessSDK get() {
        return new FlowableProcessSDK();
    }


    private FlowableProcessSDK() {
        easy4jNacosInvokerApi = Easy4j.getContext().get(Easy4jNacosInvokerApi.class);
    }

    public String getServerName() {
        if (this.serverName != null) return this.serverName;
        return Easy4j.getRequiredProperty("easy4j.flow-server-name");
    }

    public String getGroup() {
        if (this.group != null) return this.group;
        return Easy4j.getRequiredProperty("easy4j.flow-group-name");
    }

    @Override
    public String startProcess(ProcessReq processReq) {
        NacosInvokeDto build = NacosInvokeDto.builder()
                .group(getGroup())
                .serverName(getServerName())
                .body(processReq)
                .path(START_PROCESS)
                .build();
        EasyResult<Object> objectEasyResult = easy4jNacosInvokerApi.post(build);
        CheckUtils.checkRpcRes(objectEasyResult);
        return Convert.toStr(objectEasyResult.getData());
    }

    @Override
    public TaskRes startProcessAndExeFirstTask(ProcessReq processReq) {
        NacosInvokeDto build = NacosInvokeDto.builder()
                .group(getGroup())
                .serverName(getServerName())
                .body(processReq)
                .path(START_PROCESS_AND_EXE_FIRST_TASK)
                .build();
        EasyResult<Object> objectEasyResult = easy4jNacosInvokerApi.post(build);
        CheckUtils.checkRpcRes(objectEasyResult);
        Object data = objectEasyResult.getData();
        return JacksonUtil.toObject(JacksonUtil.toJson(data), TaskRes.class);
    }

    @Override
    public void endProcess(String processInstanceId,String endReason) {
        ProcessReq processReq = new ProcessReq();
        processReq.setInstanceId(processInstanceId);
        processReq.setEndInstanceReason(endReason);
        NacosInvokeDto build = NacosInvokeDto.builder()
                .group(getGroup())
                .serverName(getServerName())
                .body(processReq)
                .path(END_PROCESS)
                .build();
        EasyResult<Object> objectEasyResult = easy4jNacosInvokerApi.post(build);
        CheckUtils.checkRpcRes(objectEasyResult);
    }

    @Override
    public void completeTask(String taskId, String processKey, EasyMap<String, Object> variables) {
        ProcessReq processReq = new ProcessReq();
        processReq.setTaskId(taskId);
        processReq.setProcessKey(processKey);
        processReq.setTaskVariables(variables);
        NacosInvokeDto build = NacosInvokeDto.builder()
                .group(getGroup())
                .serverName(getServerName())
                .body(processReq)
                .path(COMPLETE_TASK)
                .build();
        EasyResult<Object> objectEasyResult = easy4jNacosInvokerApi.post(build);
        CheckUtils.checkRpcRes(objectEasyResult);
    }

    @Override
    public List<TaskRes> queryPendingTasks(String assignee, String processKey) {
        ProcessReq processReq = new ProcessReq();
        processReq.setAssignee(assignee);
        processReq.setProcessKey(processKey);
        NacosInvokeDto build = NacosInvokeDto.builder()
                .group(getGroup())
                .serverName(getServerName())
                .body(processReq)
                .path(QUERY_PENDING_TASKS)
                .build();
        EasyResult<Object> objectEasyResult = easy4jNacosInvokerApi.post(build);
        CheckUtils.checkRpcRes(objectEasyResult);
        Object data = objectEasyResult.getData();
        return JacksonUtil.toList(JacksonUtil.toJson(data), TaskRes.class);
    }

    @Override
    public List<TaskRes> queryTaskHistory(String instanceId) {
        ProcessReq processReq = new ProcessReq();
        processReq.setInstanceId(instanceId);
        NacosInvokeDto build = NacosInvokeDto.builder()
                .group(getGroup())
                .serverName(getServerName())
                .body(processReq)
                .path(QUERY_TASK_HISTORY)
                .build();
        EasyResult<Object> objectEasyResult = easy4jNacosInvokerApi.post(build);
        CheckUtils.checkRpcRes(objectEasyResult);
        Object data = objectEasyResult.getData();
        return JacksonUtil.toList(JacksonUtil.toJson(data), TaskRes.class);
    }

    @Override
    public ProcessInstanceRes queryProcessInstance(String processInstanceId) {
        ProcessReq processReq = new ProcessReq();
        processReq.setInstanceId(processInstanceId);
        NacosInvokeDto build = NacosInvokeDto.builder()
                .group(getGroup())
                .serverName(getServerName())
                .body(processReq)
                .path(QUERY_PROCESS_INSTANCE)
                .build();
        EasyResult<Object> objectEasyResult = easy4jNacosInvokerApi.post(build);
        CheckUtils.checkRpcRes(objectEasyResult);
        Object data = objectEasyResult.getData();
        return JacksonUtil.toObject(JacksonUtil.toJson(data), ProcessInstanceRes.class);
    }
}
