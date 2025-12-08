package easy4j.infra.rpc.enums;

// 枚举：执行阶段
public enum ExecutorPhase {
    ALL,
    // 客户端发送请求前 / 服务端接收请求前
    REQUEST_BEFORE,
    // 服务端返回响应前 / 客户端接收响应后
    RESPONSE_BEFORE
}