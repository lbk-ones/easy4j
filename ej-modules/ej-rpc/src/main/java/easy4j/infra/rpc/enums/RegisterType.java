package easy4j.infra.rpc.enums;

import lombok.Getter;


@Getter
public enum RegisterType {

    /**
     * /e4j/node/{ServerName}/{Ip:Port}/{HeartJson}
     */
    NODE("节点信息","/e4j/node");
    private final String name;
    private  final String registerPath;

    RegisterType(String name, String registerPath) {
        this.name = name;
        this.registerPath = registerPath;
    }

}
