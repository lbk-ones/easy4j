package easy4j.infra.rpc.codec;

import cn.hutool.core.util.ByteUtil;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.utils.SequenceUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

/**
 * rpc编码器
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
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
        // 4. 按协议格式写入 ByteBuf（顺序：消息ID->魔术字→版本→消息类型→数据长度→校验和→数据）
        long msgId = msg.getMsgId();
        out.writeLong(msgId);
        out.writeInt(Codec.MAGIC_NUMBER);
        out.writeByte(Codec.VERSION);
        out.writeByte(frameType);
        out.writeInt(body.length);
        short checkSum = Codec.getCheckSum(msgId,Codec.MAGIC_NUMBER, Codec.VERSION, frameType, body.length, body);
        out.writeShort(checkSum);
        out.writeBytes(body);
    }
}