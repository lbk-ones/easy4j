package easy4j.infra.rpc.codec;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.zip.CRC32;

/**
 * rpc解码器
 *
 * @author bokun
 * @since 2.0.1
 */
public class RpcDecoder extends ReplayingDecoder<RpcDecoder.State> {

    public RpcDecoder() {
        super(State.MAGIC);
    }

    enum State {
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
            case MAGIC:
                magic = in.readInt();
                if (Codec.MAGIC_NUMBER != magic) {
                    throw new IllegalArgumentException("illegal packet [magic]" + magic);
                }
                checkpoint(State.VERSION);
                break;
            case VERSION:
                version = in.readByte();
                if (Codec.VERSION != version) {
                    throw new IllegalArgumentException("illegal packet [version]" + version);
                }
                checkpoint(State.MSGTYPE);
                break;
            case MSGTYPE:
                frameType = in.readByte();
                checkpoint(State.DATALENGTH);
                break;
            case DATALENGTH:
                dataLength = in.readInt();
                if (dataLength < 0 || dataLength > Codec.MAX_BODY_LENGTH) {
                    throw new IllegalArgumentException("illegal packet [dataLength] " + dataLength);
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
                short b = calculateCheckSum();
                if (checkSum != b) {
                    throw new IllegalArgumentException("illegal packet [checksum]" + checkSum);
                }
                Transport build = new Transport()
                        .setMagic(magic)
                        .setVersion(version)
                        .setFrameType(frameType)
                        .setDataLength(dataLength)
                        .setCheckSum(checkSum)
                        .setBody(body);
                // fireChannelRead
                out.add(build);
                break;
            default:
                break;
        }
    }

    private short calculateCheckSum() {
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