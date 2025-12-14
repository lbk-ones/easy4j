package easy4j.infra.rpc.enums;

import easy4j.infra.rpc.codec.RpcDecoder;
import easy4j.infra.rpc.exception.DecodeRpcException;
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
    RESPONSE((byte) 3, "请求响应"),
    RESPONSE_HEART((byte) 4, "心跳回复");
    private final byte frameType;
    private final String frameTypeDesc;

    FrameType(byte frameType, String frameTypeDesc) {
        this.frameType = frameType;
        this.frameTypeDesc = frameTypeDesc;
    }

    public static FrameType getByFrameType(byte frameType){
        for (FrameType value : FrameType.values()) {
            byte frameType1 = value.getFrameType();
            if(frameType == frameType1){
                return value;
            }
        }
        return null;
    }

}
