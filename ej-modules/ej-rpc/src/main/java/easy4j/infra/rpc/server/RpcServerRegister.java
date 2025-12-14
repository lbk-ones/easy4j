package easy4j.infra.rpc.server;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class RpcServerRegister {

    private String serverName;

    private String interfaceClassName;

    private String implClassName;

    private String methodName;

    private String[] argTypes;

    private Object[] args;

    private String returnType;

    public void register(String serverName,Method method){

    }


}
