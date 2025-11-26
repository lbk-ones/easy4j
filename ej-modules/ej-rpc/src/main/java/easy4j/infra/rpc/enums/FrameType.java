package easy4j.infra.rpc.enums;

import lombok.Getter;

/**
 * 消息类型
 *
 * @author bokun
 * @since 2.0.1
 */
@Getter
public enum FrameType {
    REQUEST((byte) 1, "请求"),
    REQUEST_HEART((byte) 2, "心跳请求"),
    RESPONSE((byte) 3, "请求响应");
    private final byte frameType;
    private final String frameTypeDesc;

    FrameType(byte frameType, String frameTypeDesc) {
        this.frameType = frameType;
        this.frameTypeDesc = frameTypeDesc;
    }

}
