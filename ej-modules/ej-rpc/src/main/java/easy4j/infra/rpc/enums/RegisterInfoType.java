package easy4j.infra.rpc.enums;

import lombok.Getter;


@Getter
public enum RegisterInfoType {

    /**
     * /e4j/node/{ServerName}/{Ip:Port}/{HeartJson}
     */
    NODE("节点信息","/e4j/node");
    private final String name;
    private  final String registerPath;

    RegisterInfoType(String name, String registerPath) {
        this.name = name;
        this.registerPath = registerPath;
    }

}
