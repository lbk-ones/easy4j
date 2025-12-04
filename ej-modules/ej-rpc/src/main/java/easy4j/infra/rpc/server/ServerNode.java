package easy4j.infra.rpc.server;

import easy4j.infra.rpc.enums.LbType;
import easy4j.infra.rpc.heart.NodeHeartbeatManager;
import easy4j.infra.rpc.utils.Host;

import java.util.List;

/**
 * 服务节点信息操作
 * 1、根据服务名称获取所有节点IP信息同时缓存
 * 1、根据服务名称负载均衡获取一个节点信息
 * 1、注册节点信息
 * 1、节点心跳信息上报
 *
 * @author  bokun
 * @since 2.0.1
 */
public interface ServerNode {

    /**
     * 根据服务名称获取所有节点IP信息同时缓存
     * @param serverName 服务名称
     * @return 所有节点信息
     */
    List<Node> getNodesByServerName(String serverName);

    /**
     * 根据服务名称负载均衡获取一个节点信息
     * @param serverName 服务名称
     * @param lbType 负载均衡方式
     * @return 选中的节点
     */
    Node selectNodeByServerName(String serverName, LbType lbType);

    /**
     * 清除指定主机
     * @param host
     */
    void invalidHost(Host host);

    /**
     * 定时上报
     */
    void registry(NodeHeartbeatManager nodeHeartbeatManager,String serviceName);


}
