package easy4j.infra.rpc.codec;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.Transport;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.CRC32;

/**
 * rpc编码器
 *
 * @author bokun
 * @since 2.0.1
 */
public class RpcEncoder extends MessageToByteEncoder<Transport> {


    /**
     * 请求头长度为12字节
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToByteEncoder} belongs to
     * @param msg the message to encode
     * @param out the {@link ByteBuf} into which the encoded message will be written
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Transport msg, ByteBuf out) throws Exception {
        byte[] body = msg.getBody();
        if (body == null) {
            body = new byte[0];
        }
        byte frameType = msg.getFrameType();
        short checkSum = calculateCheckSum(Codec.MAGIC_NUMBER, Codec.VERSION, frameType, body.length, body);
        // 4. 按协议格式写入 ByteBuf（顺序：魔术字→版本→消息类型→数据长度→数据→校验和）
        out.writeInt(Codec.MAGIC_NUMBER);
        out.writeByte(Codec.VERSION);
        out.writeByte(frameType);
        out.writeInt(body.length);
        out.writeShort(checkSum);
        out.writeBytes(body);
    }

    // 计算校验和（与解码器逻辑一致）
    private short calculateCheckSum(int magic, byte version, byte frameType, int dataLength, byte[] body) {
        CRC32 crc32 = new CRC32();
        // 写入 magic（4字节）
        crc32.update((magic >> 24) & 0xFF);
        crc32.update((magic >> 16) & 0xFF);
        crc32.update((magic >> 8) & 0xFF);
        crc32.update(magic & 0xFF);
        // 写入 version（1字节）
        crc32.update(version);
        // 写入 frameType（1字节）
        crc32.update(frameType);
        // 写入 dataLength（4字节）
        crc32.update((dataLength >> 24) & 0xFF);
        crc32.update((dataLength >> 16) & 0xFF);
        crc32.update((dataLength >> 8) & 0xFF);
        crc32.update(dataLength & 0xFF);
        // 写入 body
        crc32.update(body);
        // 转为 16 位 CRC
        return (short) (crc32.getValue() & 0xFFFF);
    }
}