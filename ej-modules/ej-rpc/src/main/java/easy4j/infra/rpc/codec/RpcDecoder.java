package easy4j.infra.rpc.codec;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.FrameType;
import easy4j.infra.rpc.exception.DecodeRpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.CRC32;

/**
 * rpc解码器
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
public class RpcDecoder extends ReplayingDecoder<RpcDecoder.State> {

    public RpcDecoder() {
        super(State.MSGID);
    }

    enum State {
        MSGID,
        MAGIC,
        VERSION,
        MSGTYPE,
        DATALENGTH,
        CHECKSUM,
        BODY
    }

    int magic;
    int dataLength;
    short checkSum;
    byte version;
    byte frameType;
    long msgId;
    byte[] body;

    /**
     * 解析一个包 这个decode 会被多次调用 多次回放
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case MSGID:
                msgId = in.readLong();
                checkpoint(State.MAGIC);
                break;
            case MAGIC:
                magic = in.readInt();
                if (Codec.MAGIC_NUMBER != magic) {
                    throw new DecodeRpcException("illegal packet [magic] " + magic).setMsgId(msgId);
                }
                checkpoint(State.VERSION);
                break;
            case VERSION:
                version = in.readByte();
                if (Codec.VERSION != version) {
                    throw new DecodeRpcException("illegal packet [version] " + version).setMsgId(msgId);
                }
                checkpoint(State.MSGTYPE);
                break;
            case MSGTYPE:
                frameType = in.readByte();
                FrameType byFrameType = FrameType.getByFrameType(frameType);
                if (null == byFrameType) {
                    throw new DecodeRpcException("illegal packet [frameType] " + frameType).setMsgId(msgId);
                }
                checkpoint(State.DATALENGTH);
                break;
            case DATALENGTH:
                dataLength = in.readInt();
                if (dataLength < 0 || dataLength > Codec.MAX_BODY_LENGTH) {
                    throw new DecodeRpcException("illegal packet [dataLength] " + dataLength).setMsgId(msgId);
                }
                checkpoint(State.CHECKSUM);
                break;
            case CHECKSUM:
                checkSum = in.readShort();
                checkpoint(State.BODY);
                break;
            case BODY:
                body = new byte[dataLength];
                if (dataLength > 0) {
                    in.readBytes(body);
                }
                short b = Codec.getCheckSum(msgId, magic, version, frameType, body.length, body);
                if (checkSum != b) {
                    throw new DecodeRpcException("illegal packet [checksum]" + checkSum).setMsgId(msgId);
                }
                Transport build = new Transport()
                        .setMagic(magic)
                        .setVersion(version)
                        .setMsgId(msgId)
                        .setFrameType(frameType)
                        .setDataLength(dataLength)
                        .setCheckSum(checkSum)
                        .setBody(body);
                // fireChannelRead
                out.add(build);
                checkpoint(State.MSGID);
                break;
            default:
                break;
        }
    }
}