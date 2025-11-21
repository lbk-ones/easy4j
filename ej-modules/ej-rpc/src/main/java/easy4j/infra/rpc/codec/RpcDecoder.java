package easy4j.infra.rpc.codec;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * rpc解码器
 * @author bokun
 * @since 2.0.1
 */
public class RpcDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1. 先判断：已接收数据是否足够“头部长度”（11字节），不足则等待
        if (in.readableBytes() < Codec.HEADER_LENGTH) {
            return;
        }

        // 2. 标记读指针（防止拆包时读错，后续可重置）
        in.markReaderIndex();

        // 3. 解析头部字段，验证协议合法性
        int magic = in.readInt(); // 魔术字（4字节）
        byte version = in.readByte(); // 协议版本（1字节）
        byte msgType = in.readByte(); // 消息类型（1字节）
        int dataLength = in.readInt(); // 数据长度（4字节）
        byte checkSum = in.readByte(); // 校验和（1字节）

        // 校验魔术字和版本（非法协议直接关闭连接）
        if (magic != Codec.MAGIC_NUMBER || version != 1) {
            ctx.close();
            return;
        }

        // 4. 判断：已接收数据是否足够“头部+数据长度”，不足则重置读指针，等待后续数据
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        // 5. 读取数据字段（N字节）
        byte[] dataBytes = new byte[dataLength];
        in.readBytes(dataBytes);

        // 6. 校验和验证（简单示例：基于头部+数据的字节和取模，实际用CRC8/CRC16）
        byte calculatedCheckSum = calculateCheckSum(magic, version, msgType, dataLength, dataBytes);
        if (calculatedCheckSum != checkSum) {
            ctx.close(); // 校验失败，关闭连接
            return;
        }

        // 7. 反序列化数据，转为 RpcRequest 或 RpcResponse
        Object obj;
        if (msgType == 0) { // 0=请求
            obj = deserialize(dataBytes, RpcRequest.class);
        } else if (msgType == 1) { // 1=响应
            obj = deserialize(dataBytes, RpcResponse.class);
        } else {
            ctx.close();
            return;
        }

        // 8. 将解析后的对象交给后续业务Handler
        out.add(obj);
    }

    // 序列化工具（这里用Java序列化，实际推荐Protobuf）
    private <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) ois.readObject();
        }
    }

    // 计算校验和（示例逻辑：所有字段字节和取模256）
    private byte calculateCheckSum(int magic, byte version, byte msgType, int dataLength, byte[] data) {
        int sum = 0;
        sum += (magic >> 24) & 0xFF;
        sum += (magic >> 16) & 0xFF;
        sum += (magic >> 8) & 0xFF;
        sum += magic & 0xFF;
        sum += version & 0xFF;
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