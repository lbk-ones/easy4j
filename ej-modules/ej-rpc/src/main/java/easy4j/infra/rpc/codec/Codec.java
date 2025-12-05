package easy4j.infra.rpc.codec;

import cn.hutool.core.util.ByteUtil;

import java.nio.ByteOrder;
import java.util.zip.CRC32;

/**
 * 编码常量
 *
 * @author bokun
 * @since 2.0.1
 */
public class Codec {

    /**
     * 魔术字
     */
    public static final int MAGIC_NUMBER = 0x65346A;

    /**
     * 版本
     */
    public static final byte VERSION = 1;

    /**
     * 头部总长度（不含数据）
     */
    public static final int HEADER_LENGTH = 20;

    /**
     * 最大有效数据长度
     */
    public static final int MAX_BODY_LENGTH = 10 * 1024 * 1024; // 10MB

    /**
     * 最大包长度
     */
    public static final int MAX_FRAME_LENGTH = MAX_BODY_LENGTH + HEADER_LENGTH;

    /**
     * 获取校验和
     *
     * @param msgId      消息ID
     * @param magic      魔术字
     * @param version    版本
     * @param frameType  消息类型
     * @param dataLength body长度
     * @param body       数据体
     * @return 32位转16位
     * @author bokun
     * @since 2.0.1
     */
    public static short getCheckSum(long msgId, int magic, byte version, byte frameType, int dataLength, byte[] body) {
        CRC32 crc32 = new CRC32();
        crc32.update(ByteUtil.longToBytes(msgId, ByteOrder.BIG_ENDIAN));
        // 写入 magic（4字节）
        crc32.update(ByteUtil.intToBytes(magic, ByteOrder.BIG_ENDIAN));
        // 写入 version（1字节）
        crc32.update(version);
        // 写入 frameType（1字节）
        crc32.update(frameType);
        // 写入 dataLength（4字节）
        crc32.update(ByteUtil.intToBytes(dataLength, ByteOrder.BIG_ENDIAN));
        // 写入 body
        crc32.update(body);
        // 转为 16 位 CRC
        return (short) (crc32.getValue() & 0xFFFF);
    }
}
