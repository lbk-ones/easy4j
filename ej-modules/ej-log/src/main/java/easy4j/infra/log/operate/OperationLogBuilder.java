package easy4j.infra.log.operate;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.user.UserContext;
import easy4j.infra.webmvc.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import easy4j.infra.dbaccess.domain.OperationLogs;
/**
 * 操作日志构建器
 * 用于便捷地创建和填充OperationLog对象
 * 支持链式调用和自动获取请求信息
 *
 * @author easy4j
 * @since 2026-07-10
 */
public class OperationLogBuilder {

    private final OperationLogs operationLogs;

    public OperationLogBuilder() {
        this.operationLogs = new easy4j.infra.dbaccess.domain.OperationLogs();
        this.operationLogs.setCreatedAt(new Date());
        this.operationLogs.setUpdatedAt(new Date());
        this.operationLogs.setSuccess(1);
        this.operationLogs.setCostTime(0);
    }

    /**
     * 创建新的OperationLogBuilder
     */
    public static OperationLogBuilder create() {
        return new OperationLogBuilder();
    }

    /**
     * 设置操作人信息
     */
    public OperationLogBuilder operator(Long operatorId, String operatorName) {
        this.operationLogs.setOperatorId(operatorId);
        this.operationLogs.setOperatorName(operatorName);
        return this;
    }

    /**
     * 自动从当前请求获取操作人IP和User-Agent
     */
    public OperationLogBuilder autoFillRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userId = request.getHeader(OperationLogAspect.HTTP_HEADER_USER_ID);
                String userName = request.getHeader(OperationLogAspect.HTTP_HEADER_USER_NAME_CN);
                Long userIdLong = Convert.toLong(userId);
                UserContext userContext = getUserContext();
                if(userIdLong==null){
                    userIdLong = userContext.getUserId();
                }
                if(StrUtil.isBlank(userName)){
                    userName  = userContext.getUserNameCn();
                }
                operator(userIdLong,userName);
                this.operationLogs.setOperatorIp(IpUtils.getIpAddr(request));
                this.operationLogs.setOperatorUa(request.getHeader(HttpHeaders.USER_AGENT));
                this.operationLogs.setRequestMethod(request.getMethod());
                this.operationLogs.setRequestUrl(request.getRequestURI());
            }
        } catch (Exception e) {
            // 如果不在HTTP上下文中，忽略异常
        }
        return this;
    }

    /**
     * 设置请求方法
     */
    public OperationLogBuilder requestMethod(String requestMethod) {
        this.operationLogs.setRequestMethod(requestMethod);
        return this;
    }

    /**
     * 设置请求URL
     */
    public OperationLogBuilder requestUrl(String requestUrl) {
        this.operationLogs.setRequestUrl(requestUrl);
        return this;
    }

    /**
     * 设置请求参数（JSON格式）
     */
    public OperationLogBuilder requestParams(String requestParams) {
        this.operationLogs.setRequestParams(requestParams);
        return this;
    }

    /**
     * 设置请求Body（JSON格式）
     */
    public OperationLogBuilder requestBody(String requestBody) {
        this.operationLogs.setRequestBody(requestBody);
        return this;
    }

    /**
     * 设置操作人IP地址
     */
    public OperationLogBuilder operatorIp(String operatorIp) {
        this.operationLogs.setOperatorIp(operatorIp);
        return this;
    }

    /**
     * 设置User-Agent信息
     */
    public OperationLogBuilder operatorUa(String operatorUa) {
        this.operationLogs.setOperatorUa(operatorUa);
        return this;
    }

    /**
     * 设置操作模块
     */
    public OperationLogBuilder module(String module) {
        this.operationLogs.setModule(module);
        return this;
    }

    /**
     * 设置操作描述
     */
    public OperationLogBuilder description(String description) {
        this.operationLogs.setDescription(description);
        return this;
    }

    /**
     * 设置操作动作
     */
    public OperationLogBuilder action(String action) {
        this.operationLogs.setAction(action);
        return this;
    }

    /**
     * 设置业务类型
     */
    public OperationLogBuilder businessType(String businessType) {
        this.operationLogs.setBusinessType(businessType);
        return this;
    }

    /**
     * 设置业务主键
     */
    public OperationLogBuilder businessId(String businessId) {
        this.operationLogs.setBusinessId(businessId);
        return this;
    }

    /**
     * 设置业务流水号（自动生成UUID）
     */
    public OperationLogBuilder businessNo() {
        this.operationLogs.setBusinessNo(UUID.randomUUID().toString());
        return this;
    }

    /**
     * 设置业务流水号（指定值）
     */
    public OperationLogBuilder businessNo(String businessNo) {
        this.operationLogs.setBusinessNo(businessNo);
        return this;
    }

    /**
     * 设置操作成功
     */
    public OperationLogBuilder success() {
        this.operationLogs.setSuccess(1);
        return this;
    }

    /**
     * 设置操作失败并记录错误信息
     */
    public OperationLogBuilder fail(String errorMsg) {
        this.operationLogs.setSuccess(0);
        this.operationLogs.setErrorMsg(errorMsg);
        return this;
    }

    /**
     * 设置响应状态码
     */
    public OperationLogBuilder responseCode(Integer responseCode) {
        this.operationLogs.setResponseCode(responseCode);
        return this;
    }

    /**
     * 设置响应数据
     */
    public OperationLogBuilder responseData(String responseData) {
        this.operationLogs.setResponseData(responseData);
        return this;
    }

    /**
     * 设置执行耗时（毫秒）
     */
    public OperationLogBuilder costTime(long costTime) {
        this.operationLogs.setCostTime((int) costTime);
        return this;
    }

    /**
     * 设置创建时间
     */
    public OperationLogBuilder createdAt(Date createdAt) {
        this.operationLogs.setCreatedAt(createdAt);
        return this;
    }

    /**
     * 获取构建的OperationLog对象
     */
    public OperationLogs build() {
        this.operationLogs.setUpdatedAt(new Date());
        return this.operationLogs;
    }


    /**
     * 从上下文获取用户信息
     */
    public static UserContext getUserContext() {
        Easy4jContext context = Easy4j.getContext();
        Optional<Object> threadHashValue = context.getThreadHashValue(UserContext.USER_CONTEXT_NAME, UserContext.USER_CONTEXT_NAME);
        if (threadHashValue.isPresent()) {
            Object o = threadHashValue.get();
            return (UserContext) o;
        }
        return  new UserContext();
    }
}
