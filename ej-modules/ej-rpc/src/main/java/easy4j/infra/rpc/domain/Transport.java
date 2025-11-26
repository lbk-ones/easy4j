package easy4j.infra.rpc.domain;

import easy4j.infra.rpc.enums.FrameType;
import lombok.Builder;
import lombok.Data;

/**
 * frameType 和 body 是必填的
 */
@Data
@Builder
public class Transport {

    /**
     * 魔术字
     */
    private int magic;
    /**
     * 版本
     */
    private byte version;
    /**
     * 消息类型
     */
    private byte frameType;
    /**
     * body的数据长度
     */
    private int dataLength;
    /**
     * 校验和2字节
     */
    private short checkSum;

    /**
     * body体
     */
    private byte[] body;

    public static Transport of(FrameType frameType, byte[] body){
        return Transport.builder()
                .frameType(frameType.getFrameType())
                .body(body)
                .build();
    }

}
