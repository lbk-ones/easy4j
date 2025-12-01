package easy4j.infra.rpc.server;

import easy4j.infra.rpc.heart.NodeHeartbeatInfo;
import easy4j.infra.rpc.utils.Host;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Node {

    private Host host;
    private boolean enabled;
    // 当前最后一次心跳信息
    private NodeHeartbeatInfo nodeHeartbeatInfo;

    public Node(Host host, boolean enabled) {
        this.host = host;
        this.enabled = enabled;
    }
}
