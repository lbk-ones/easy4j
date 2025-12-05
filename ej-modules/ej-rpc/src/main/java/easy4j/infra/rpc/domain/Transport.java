package easy4j.infra.rpc.domain;

import easy4j.infra.rpc.enums.FrameType;
import easy4j.infra.rpc.utils.Sequence;
import easy4j.infra.rpc.utils.SequenceUtils;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * frameType 和 body 是必填的
 */
@Data
@Accessors(chain = true)
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
     * 消息ID
     */
    private long msgId;

    /**
     * body体
     */
    private byte[] body = new byte[0];

    public static Transport of(FrameType frameType, byte[] body) {
        return new Transport()
                .setMsgId(SequenceUtils.gen())
                .setFrameType(frameType.getFrameType())
                .setBody(body);
    }

    public static Transport of(FrameType frameType) {
        return new Transport()
                .setMsgId(SequenceUtils.gen())
                .setFrameType(frameType.getFrameType());
    }

    @Override
    public String toString() {
        return "Transport{" +
                "msgId=" + msgId +
                ",magic=" + magic +
                ", version=" + version +
                ", frameType=" + frameType +
                ", dataLength=" + dataLength +
                ", checkSum=" + checkSum +
                '}';
    }
}
