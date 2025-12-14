package easy4j.infra.rpc.enums;

import lombok.Getter;

/**
 * 响应状态码 用ai小小的生成了一波
 *
 * @since 2.0.1
 */
@Getter
public enum RpcResponseStatus {

    // ===================== 成功状态 =====================
    SUCCESS(true, 200, "成功", "Success"),

    // ===================== 业务异常（4xx）=====================
    // 参数相关
    PARAM_ERROR(false, 400, "参数错误", "Parameter Error"),
    PARAM_FORMAT_ERROR(false, 4001, "参数格式不正确", "Parameter Format Error"),
    PARAM_MISSING(false, 4002, "缺少必填参数", "Missing Required Parameter"),
    // 权限相关
    UNAUTHORIZED(false, 401, "未授权", "Unauthorized"),
    TOKEN_EXPIRED(false, 4011, "令牌过期", "Token Expired"),
    TOKEN_INVALID(false, 4012, "令牌无效", "Invalid Token"),
    PERMISSION_DENIED(false, 403, "权限不足", "Permission Denied"),
    // 资源相关
    RESOURCE_NOT_FOUND(false, 404, "资源不存在", "Resource Not Found"),
    INSTANCE_NOT_FOUND(false, 405, "未找到被调用者实例", "The called instance was not found"),
    USER_NOT_FOUND(false, 4041, "用户不存在", "User Not Found"),
    DATA_NOT_FOUND(false, 4042, "数据不存在", "Data Not Found"),
    // 业务逻辑相关
    BUSINESS_ERROR(false, 409, "业务逻辑异常", "Business Logic Error"),
    DATA_DUPLICATE(false, 4091, "数据已存在", "Data Duplicate"),
    OPERATION_NOT_ALLOWED(false, 4092, "当前操作不允许", "Operation Not Allowed"),
    QUOTA_EXCEEDED(false, 429, "请求频率超限", "Request Quota Exceeded"),
    INVOKE_EXCEPTION(false, 430, "调用异常", "Request Invoke Error"),

    // ===================== 系统异常（5xx）=====================
    SYSTEM_ERROR(false, 500, "服务器内部错误", "Internal Server Error"),
    DB_ERROR(false, 501, "数据库操作异常", "Database Error"),
    DB_CONNECT_ERROR(false, 5011, "数据库连接失败", "Database Connection Failed"),
    DB_QUERY_ERROR(false, 5012, "数据库查询异常", "Database Query Error"),
    DB_UPDATE_ERROR(false, 5013, "数据库更新异常", "Database Update Error"),
    // 序列化/反序列化相关
    SERIALIZE_ERROR(false, 502, "序列化异常", "Serialize Error"),
    DESERIALIZE_ERROR(false, 5021, "反序列化异常", "Deserialize Error"),
    // 网络/通讯相关
    NETWORK_ERROR(false, 503, "网络通讯异常", "Network Error"),
    CONNECTION_TIMEOUT(false, 5031, "连接超时", "Connection Timeout"),
    REMOTE_SERVICE_UNAVAILABLE(false, 5032, "远程服务不可用", "Remote Service Unavailable"),
    // 其他系统异常
    UNSUPPORTED_OPERATION(false, 504, "不支持的操作", "Unsupported Operation"),
    CONFIG_ERROR(false, 505, "配置错误", "Configuration Error"),
    RESOURCE_EXHAUSTED(false, 506, "服务器资源耗尽", "Resource Exhausted"),
    DECODE_ERROR(false, 507, "解码异常", "Decode Error"),
    SERVICE_NAME_NOT_BE_NULL(false, 508, "ServiceName不能为空", "ServiceName cannot be empty"),
    CLIENT_ERROR(false, 509, "客户端出现未知异常", "Unknown exception occurred on the client side"),
    SERVER_ERROR(false, 510, "服务端出现未知异常", "Unknown exception occurred on the server"),
    SERVER_HANDLER_NOT_FOUND_ERROR(false, 511, "未找到handler或者出现异常", "Not found handler or handler appear exception!");

    /**
     * 是否成功（true=成功，false=失败）
     */
    private final boolean success;
    /**
     * 状态码（200=成功，4xx=业务异常，5xx=系统异常）
     */
    private final int code;
    /**
     * 中文名称（面向国内业务/日志）
     */
    private final String cnMsg;
    /**
     * 英文名称（面向多语言客户端/国际化）
     */
    private final String enMsg;

    /**
     * 构造方法（私有，枚举类只能内部实例化）
     */
    RpcResponseStatus(boolean success, int code, String cnMsg, String enMsg) {
        this.success = success;
        this.code = code;
        this.cnMsg = cnMsg;
        this.enMsg = enMsg;
    }

    // ===================== 工具方法（便捷使用）=====================

    /**
     * 根据状态码获取枚举项（用于客户端/服务端解析状态）
     *
     * @param code 状态码
     * @return 对应的枚举项，未找到返回 SYSTEM_ERROR
     */
    public static RpcResponseStatus getByCode(int code) {
        for (RpcResponseStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        // 未匹配到状态码时，默认返回系统错误
        return SYSTEM_ERROR;
    }

    /**
     * 判断状态码是否为成功状态
     *
     * @param code 状态码
     * @return true=成功，false=失败
     */
    public static boolean isSuccess(int code) {
        return getByCode(code).isSuccess();
    }

    /**
     * 判断状态码是否为业务异常（4xx）
     *
     * @param code 状态码
     * @return true=业务异常，false=其他
     */
    public static boolean isBusinessError(int code) {
        return code >= 400 && code < 500;
    }

    /**
     * 判断状态码是否为系统异常（5xx）
     *
     * @param code 状态码
     * @return true=系统异常，false=其他
     */
    public static boolean isSystemError(int code) {
        return code >= 500 && code < 600;
    }

    public String getMsg() {
        return this.enMsg;
    }
}