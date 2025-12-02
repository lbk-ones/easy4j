package easy4j.infra.rpc.exception;

import java.sql.SQLException;

/**
 * SQL 运行时异常（包装 SQLException，转为非受检异常）
 */
public class SqlRuntimeException extends RuntimeException {

    // 无参构造
    public SqlRuntimeException() {
        super();
    }

    // 带错误消息
    public SqlRuntimeException(String message) {
        super(message);
    }

    // 带错误消息 + 原始 SQLException（核心：保留异常链）
    public SqlRuntimeException(String message, SQLException cause) {
        super(message, cause); // 传递原始异常作为 cause，保留堆栈
    }

    // 仅带原始 SQLException
    public SqlRuntimeException(SQLException cause) {
        super(cause); // 直接包装原始异常
    }

    // 带错误消息 + 原始异常 + 抑制异常 + 是否 writableStackTrace
    public SqlRuntimeException(String message, SQLException cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}