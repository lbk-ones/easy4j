package easy4j.infra.log.operate;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.ThreadPoolUtils;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.context.Easy4jContextFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import easy4j.infra.dbaccess.domain.OperationLogs;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.*;

/**
 * 操作日志切面
 * 自动拦截标注 @OpLog 的方法，记录操作信息
 *
 * @author easy4j
 * @date 2026/7/10
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    /**
     * 用户ID(不是用户账号)
     */
    public static final String HTTP_HEADER_USER_ID = "X-USER-ID";
    // 用户姓名
    public static final String HTTP_HEADER_USER_NAME_CN = "X-USER-NAME";
    // 业务流水号
    public static final String HTTP_HEADER_BNO = "X-BN";

    private static final ExpressionParser expressionParser = new SpelExpressionParser();

    OperateLogRepository operateLogRepository;

    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public static final String POOL_NAME = "log-thread";

    public OperationLogAspect() {
        operateLogRepository = new DbOperate();
        operateLogRepository.init();
        threadPoolTaskExecutor = getThreadPool();
    }

    public static ThreadPoolTaskExecutor getThreadPool(){
        return ThreadPoolUtils.getThreadPoolTaskExecutor(POOL_NAME, 4, 8, 10);
    }

    /**
     * 切入点：拦截所有标注 @OpLog 的方法
     */
    @Pointcut("@annotation(easy4j.infra.log.operate.OperateLog)")
    public void operationLogPointcut() {
    }

    public Optional<HttpServletRequest> getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }
        return Optional.ofNullable(request);
    }

    /**
     * 环绕通知：记录操作日志
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        if (!Easy4j.getProperty(SysConstant.EASY4J_ENABLE_OPERATE_LOG, boolean.class)) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        OperationLogs operationLogs = null;
        Object result = null;
        Exception exception = null;
        try {
            // 获取方法和注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            OperateLog annotation = method.getAnnotation(OperateLog.class);
            // 先从header中读取 业务流水号 这个优先级 略低于 注解属性
            String bn = getRequest().map(e -> e.getHeader(OperationLogAspect.HTTP_HEADER_BNO)).orElse(null);
            String s = annotation.businessNo();
            s = StrUtil.blankToDefault(s, bn);
            String traceId = Easy4jContextFactory.getContext()
                    .getThreadHashValue(SysConstant.TRACE_ID_NAME, SysConstant.TRACE_ID_NAME)
                    .map(Object::toString)
                    .orElse("");


            // 初始化日志对象
            operationLogs = OperationLogBuilder.create()
                    .autoFillRequest()
                    .module(annotation.module())
                    .action(annotation.action())
                    .description(annotation.description())
                    .businessType(annotation.businessType())
                    .businessNo(
                            StrUtil.blankToDefault(
                                    s,
                                    StrUtil.blankToDefault(
                                            traceId, UUID.randomUUID().toString().replaceAll("-", "")
                                    ))
                    )
                    .build();

            // 解析并设置 businessId
            if (StrUtil.isNotBlank(annotation.businessIdExpression())) {
                String businessId = parseBusinessId(annotation.businessIdExpression(), joinPoint, signature);
                if (StrUtil.isNotBlank(businessId)) {
                    operationLogs.setBusinessId(businessId);
                }
            }

            if (annotation.recordParams()) {
                // 记录请求参数
                recordRequestParams(operationLogs, joinPoint, signature);
            }

            // 执行目标方法
            result = joinPoint.proceed();

            // 记录响应结果
            operationLogs.setResponseCode(200);
            operationLogs.setSuccess(1);

            if (annotation.recordResult()) {
                recordResponseData(operationLogs, result);
            }

        } catch (Exception e) {
            if (operationLogs != null) {
                exception = e;
                operationLogs.setResponseCode(500);
                operationLogs.setSuccess(0);
                operationLogs.setErrorMsg(e.getMessage());
                log.error("操作日志记录异常", e);
            }
            throw e;

        } finally {
            if (operationLogs != null) {
                // 记录执行耗时
                long costTime = System.currentTimeMillis() - startTime;
                operationLogs.setCostTime((int) costTime);
                operationLogs.setUpdatedAt(new Date());

                // 保存日志到数据库（异步）
                saveOperationLogAsync(operationLogs);
            }
        }
        return result;
    }

    /**
     * 解析 businessId 表达式
     * 支持：
     * 1. 参数名称：orderId
     * 2. SpEL 表达式：#order.id, #order.id + '-' + #order.status
     * 3. 自定义方法调用：#getBusinessId(#order)
     */
    private String parseBusinessId(String expression, ProceedingJoinPoint joinPoint, MethodSignature signature) {
        try {
            String[] paramNames = signature.getParameterNames();
            Object[] paramValues = joinPoint.getArgs();

            // 情况1：直接参数名（如："orderId"）
            if (!expression.startsWith("#") && !expression.contains(".") && !expression.contains("+")) {
                for (int i = 0; i < paramNames.length; i++) {
                    if (paramNames[i].equals(expression)) {
                        Object value = paramValues[i];
                        return value != null ? value.toString() : null;
                    }
                }
                return null;
            }

            // 情况2：SpEL 表达式（如："#order.id"）
            EvaluationContext context = new StandardEvaluationContext();

            // 绑定所有方法参数到上下文
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], paramValues[i]);
            }

            // 如果需要，也可以注册自定义函数
            // context.registerFunction("getBusinessId", MyUtil.class.getDeclaredMethod("getBusinessId", Order.class));

            Expression spelExpression = expressionParser.parseExpression(expression);
            Object result = spelExpression.getValue(context);

            return result != null ? result.toString() : null;

        } catch (Exception e) {
            log.warn("解析 businessId 表达式失败: {}", expression, e);
            return null;
        }
    }

    /**
     * 记录请求参数
     */
    private void recordRequestParams(OperationLogs operationLogs, ProceedingJoinPoint joinPoint,
                                     MethodSignature signature) {
        try {
            String[] paramNames = signature.getParameterNames();
            Object[] paramValues = joinPoint.getArgs();
            Class<?>[] paramTypes = signature.getParameterTypes();

            if (paramNames != null && paramNames.length > 0) {
                List<String> recordParams = new ArrayList<>();
                List<Object> recordValues = new ArrayList<>();

                for (int i = 0; i < paramNames.length; i++) {
                    Object paramValue = paramValues[i];
                    Class<?> paramType = paramTypes[i];

                    // 过滤Spring MVC自动注入的参数类型
                    if (isIgnoredParameter(paramValue, paramType)) {
                        continue;
                    }

                    recordParams.add(paramNames[i]);
                    recordValues.add(paramValue);
                }

                // 设置格式化的参数字符串
                if (!recordParams.isEmpty()) {
                    String paramsStr = formatParams(recordParams, recordValues);
                    operationLogs.setRequestParams(paramsStr);

                    // 尝试序列化为JSON格式的请求体
                    if (!recordValues.isEmpty()) {
                        String json = JacksonUtil.toJson(recordValues);
                        try {
                            operationLogs.setRequestBody(json);
                        } catch (Exception e) {
                            log.debug("序列化请求体为JSON失败，使用字符串格式", e);
                            operationLogs.setRequestBody(paramsStr);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("记录请求参数失败", e);
        }
    }

    /**
     * 判断是否为需要忽略的参数类型
     * 这些参数由Spring MVC框架自动注入，不需要记录
     */
    private boolean isIgnoredParameter(Object paramValue, Class<?> paramType) {
        if (paramValue == null) {
            return true;
        }

        // Servlet相关
        if (paramValue instanceof HttpServletRequest) {
            return true;
        }
        if (paramValue instanceof HttpServletResponse) {
            return true;
        }
        if (paramValue instanceof ServletRequest) {
            return true;
        }
        if (paramValue instanceof ServletResponse) {
            return true;
        }

        // Session相关
        if (paramValue instanceof HttpSession) {
            return true;
        }

        // 认证/授权相关
        if (paramValue instanceof Principal) {
            return true;
        }
        if (paramValue instanceof Authentication) {
            return true;
        }

        // 国际化相关
        if (paramValue instanceof Locale) {
            return true;
        }
        if (paramValue instanceof TimeZone) {
            return true;
        }

        // Model相关
        if (paramValue instanceof Model) {
            return true;
        }
        if (paramValue instanceof ModelMap) {
            return true;
        }
        if (paramValue instanceof RedirectAttributes) {
            return true;
        }
        if (paramValue instanceof ModelAndView) {
            return true;
        }

        // 验证相关
        if (paramValue instanceof BindingResult) {
            return true;
        }
        if (paramValue instanceof Errors) {
            return true;
        }

        // 流相关
        if (paramValue instanceof InputStream) {
            return true;
        }
        if (paramValue instanceof OutputStream) {
            return true;
        }
        if (paramValue instanceof Reader) {
            return true;
        }
        if (paramValue instanceof Writer) {
            return true;
        }

        // 其他Web请求相关
        if (paramValue instanceof WebRequest) {
            return true;
        }
        if (paramValue instanceof ServletContext) {
            return true;
        }
        if (paramValue instanceof HttpHeaders) {
            return true;
        }

        // 按类型名称检查（兼容某些类不在classpath中的情况）
        String paramClassName = paramType.getName();
        if (paramClassName.startsWith("org.springframework")) {
            return isSpringFrameworkIgnoredType(paramClassName);
        }

        return false;
    }

    /**
     * 检查Spring框架的特殊类型
     */
    private boolean isSpringFrameworkIgnoredType(String className) {
        return className.contains("org.springframework.web.context.request") ||
                className.contains("org.springframework.web.servlet") ||
                className.contains("org.springframework.ui") ||
                className.contains("org.springframework.validation") ||
                className.contains("org.springframework.security.core");
    }

    /**
     * 格式化参数为可读的字符串
     */
    private String formatParams(List<String> paramNames, List<Object> paramValues) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < paramNames.size(); i++) {
            if (i > 0) {
                params.append(", ");
            }
            String paramName = paramNames.get(i);
            Object paramValue = paramValues.get(i);
            params.append(paramName).append("=").append(formatValue(paramValue));
        }
        return params.toString();
    }

    /**
     * 格式化单个参数值
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }

        // 大对象截断处理
        String strValue = value.toString();
        if (strValue.length() > 500) {
            return strValue.substring(0, 500) + "...（已截断）";
        }

        return strValue;
    }

    /**
     * 记录响应数据
     */
    private void recordResponseData(OperationLogs operationLogs, Object result) {
        try {
            if (result == null) {
                operationLogs.setResponseData("null");
                return;
            }

            try {
                String json = JacksonUtil.toJson(result);
                // 限制响应数据大小（防止日志过大）
                if (json.length() > 5000) {
                    operationLogs.setResponseData(json.substring(0, 5000) + "...");
                } else {
                    operationLogs.setResponseData(json);
                }
            } catch (Exception e) {
                operationLogs.setResponseData(result.toString());
            }
        } catch (Exception e) {
            log.warn("记录响应数据失败", e);
        }
    }

    /**
     * 异步保存操作日志
     */
    private void saveOperationLogAsync(OperationLogs operationLogs) {
        threadPoolTaskExecutor.submit(() -> {
            try {
                operateLogRepository.save(operationLogs);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        });
    }


}
