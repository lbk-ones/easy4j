package easy4j.infra.rpc.server.handlers;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import easy4j.infra.rpc.server.RpcServer;
import easy4j.infra.rpc.utils.ChannelUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 解析请求信息
 *
 * @author bokun
 * @since 2.0.1
 */
@ChannelHandler.Sharable
public class RequestHandler extends SimpleChannelInboundHandler<Transport> {

    private final RpcServer rpcServer;

    public RequestHandler(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Transport transport) throws Exception {
        byte[] body = transport.getBody();
        if (body.length > 0) {
            ISerializable iSerializable = SerializableFactory.get();
            RpcRequest deserializable = iSerializable.deserializable(body, RpcRequest.class);
            channelHandlerContext.channel().attr(ChannelUtils.REQUEST_INFO).set(deserializable);
        }
        channelHandlerContext.fireChannelRead(transport);
    }
}
