package easy4j.infra.rpc.codec;

import easy4j.infra.rpc.domain.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
/**
 * rpc编码器
 * @author bokun
 * @since 2.0.1
 */
public class RpcEncoder extends MessageToByteEncoder<Object> {


    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 1. 序列化消息体（RpcRequest/RpcResponse）
        byte[] dataBytes = serialize(msg);

        // 2. 确定消息类型（0=请求，1=响应）
        byte msgType = (byte) ((msg instanceof RpcRequest) ? 0 : 1);

        // 3. 计算校验和
        byte checkSum = calculateCheckSum(dataBytes.length, msgType, dataBytes);

        // 4. 按协议格式写入 ByteBuf（顺序：魔术字→版本→消息类型→数据长度→数据→校验和）
        out.writeInt(Codec.MAGIC_NUMBER); // 4字节
        out.writeByte(Codec.VERSION); // 1字节
        out.writeByte(msgType); // 1字节
        out.writeInt(dataBytes.length); // 4字节（数据长度）
        out.writeBytes(dataBytes); // N字节（数据）
        out.writeByte(checkSum); // 1字节（校验和）
    }

    // 序列化工具
    private byte[] serialize(Object obj) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }

    // 计算校验和（与解码器逻辑一致）
    private byte calculateCheckSum(int dataLength, byte msgType, byte[] data) {
        int sum = 0;
        sum += (Codec.MAGIC_NUMBER >> 24) & 0xFF;
        sum += (Codec.MAGIC_NUMBER >> 16) & 0xFF;
        sum += (Codec.MAGIC_NUMBER >> 8) & 0xFF;
        sum += Codec.MAGIC_NUMBER & 0xFF;
        sum += Codec.VERSION & 0xFF;
        sum += msgType & 0xFF;
        sum += (dataLength >> 24) & 0xFF;
        sum += (dataLength >> 16) & 0xFF;
        sum += (dataLength >> 8) & 0xFF;
        sum += dataLength & 0xFF;
        for (byte b : data) {
            sum += b & 0xFF;
        }
        return (byte) (sum % 256);
    }
}