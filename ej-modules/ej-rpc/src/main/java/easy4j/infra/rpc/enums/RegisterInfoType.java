package easy4j.infra.rpc.enums;

import cn.hutool.core.text.StrPool;
import easy4j.infra.rpc.utils.Host;
import lombok.Getter;


@Getter
public enum RegisterInfoType {

    /**
     * /e4j/node/{ServerName}/{Ip:Port}
     */
    NODE("节点信息", "/e4j/node"),
    /**
     * /e4j/service/{ServerName}/{Ip:Port}
     */
    SERVICE("服务信息", "/e4j/service");
    private final String name;
    private final String registerPath;

    RegisterInfoType(String name, String registerPath) {
        this.name = name;
        this.registerPath = registerPath;
    }

    public String wrap(String element, Host host) {
        String registerPath1 = this.getRegisterPath();
        if (host == null) {
            return registerPath1 + StrPool.SLASH + element;
        } else {
            return registerPath1 + StrPool.SLASH + element + StrPool.SLASH + host.getAddress();
        }
    }

}
