package easy4j.infra.rpc.config;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public abstract class NettyBootStrap {

    public boolean isClient = false;

    public NettyBootStrap(boolean isClient) {
        this.isClient = isClient;
    }

    /**
     * 服务端主通道获取
     *
     * @return 返回主通道
     */
    public Class<? extends ServerSocketChannel> getMainServerChannel() {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel.class;
        } else {
            return NioServerSocketChannel.class;
        }
    }

    /**
     * 客户端主通道
     *
     * @return 返回主通道
     */
    public Class<? extends SocketChannel> getMainClientChannel() {
        if (Epoll.isAvailable()) {
            return EpollSocketChannel.class;
        } else {
            return NioSocketChannel.class;
        }
    }

    public boolean isEpoll() {
        return Epoll.isAvailable();
    }

    public EventLoopGroup getEventLoop(Integer threadNums) {
        if (isEpoll()) {
            return new EpollEventLoopGroup(threadNums == null ? 0 : threadNums);
        } else {
            return new NioEventLoopGroup(threadNums == null ? 0 : threadNums);

        }

    }

}
