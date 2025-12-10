package easy4j.infra.rpc.enums;

import lombok.Getter;


@Getter
public enum RegisterInfoType {

    /**
     * /e4j/node/{ServerName}/{Ip:Port}
     */
    NODE("节点信息", "/e4j/node"),
    /**
     * /e4j/service/{ServerName}
     */
    SERVICE("服务信息", "/e4j/service");
    private final String name;
    private final String registerPath;

    RegisterInfoType(String name, String registerPath) {
        this.name = name;
        this.registerPath = registerPath;
    }

}
