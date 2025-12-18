package easy4j.infra.rpc.server;

import cn.hutool.core.date.SystemClock;
import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.config.E4jServerConfig;
import easy4j.infra.rpc.enums.RegisterType;
import easy4j.infra.rpc.heart.NodeHeartbeatInfo;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.utils.Host;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Node implements Cloneable {

    private Host host;
    private boolean enabled;
    // 当前最后一次心跳信息
    private NodeHeartbeatInfo nodeHeartbeatInfo;

    public Node(Host host, boolean enabled) {
        this.host = host;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        if (nodeHeartbeatInfo != null) {
            long registryJdbcCheckPeriod = 0;
            E4jRpcConfig config = IntegratedFactory.getConfig();
            if (config.getRegisterType() == RegisterType.JDBC) {
                registryJdbcCheckPeriod = config.getRegistryJdbcCheckPeriod();
            }
            E4jServerConfig server = config.getServer();
            Long heartInfoReportFixRateMilli = server.getHeartInfoReportFixRateMilli();
            if (heartInfoReportFixRateMilli != null) {
                if (registryJdbcCheckPeriod == 0) {
                    heartInfoReportFixRateMilli = heartInfoReportFixRateMilli * 3;
                } else {
                    // jdbc时间减少，因为还要加上数据库扫表的间隔
                    heartInfoReportFixRateMilli = heartInfoReportFixRateMilli * 2;
                }
                // 超过这个时间就算失效
                long l = heartInfoReportFixRateMilli + registryJdbcCheckPeriod;
                Long heartDeathFixRateMilli = server.getHeartDeathFixRateMilli();
                if (heartDeathFixRateMilli != null) {
                    l = heartDeathFixRateMilli;
                }
                long reportTime = nodeHeartbeatInfo.getReportTime();
                if (reportTime > 0 && (reportTime + l) < SystemClock.now()) {
                    return false;
                }
            }
            return nodeHeartbeatInfo.isDisabled();
        }
        return enabled;
    }

    @Override
    public Node clone() {
        try {
            return (Node) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
