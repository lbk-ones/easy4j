package easy4j.infra.rpc.server;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 全局管理：端口 -> 该端口对应的客户端 Channel 集合
 */
public class ServerPortChannelManager {
    private static final Map<Integer, CopyOnWriteArraySet<Channel>> PORT_CHANNEL_MAP = new ConcurrentHashMap<>();

    public static void initPortChannelSet(int port) {
        PORT_CHANNEL_MAP.computeIfAbsent(port, k -> new CopyOnWriteArraySet<>());
    }

    public static void addChannel(Channel channel) {
        if (channel instanceof NioSocketChannel) {
            int serverPort = ((NioSocketChannel) channel).localAddress().getPort();
            CopyOnWriteArraySet<Channel> channelSet = PORT_CHANNEL_MAP.get(serverPort);
            if (channelSet != null) {
                channelSet.add(channel);
            }
        }
    }

    public static void removeChannel(Channel channel) {
        if (channel instanceof NioSocketChannel) {
            int serverPort = ((NioSocketChannel) channel).localAddress().getPort();
            CopyOnWriteArraySet<Channel> channelSet = PORT_CHANNEL_MAP.get(serverPort);
            if (channelSet != null) {
                channelSet.remove(channel);
            }
        }
    }

    public static int countChannelByPort(int port) {
        CopyOnWriteArraySet<Channel> channelSet = PORT_CHANNEL_MAP.get(port);
        return channelSet == null ? 0 : channelSet.size();
    }
}