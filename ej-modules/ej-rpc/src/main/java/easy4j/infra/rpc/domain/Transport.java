package easy4j.infra.rpc.domain;

import lombok.Builder;
import lombok.Data;

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

}
