package easy4j.infra.rpc.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;

@Data
@Accessors(chain = true)
public class FilterAttributes {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 超时时间
     */
    long timeOut;


    /**
     * 是否泛化调用
     */
    boolean isGeneralizedInvoke = false;


    /**
     * 代理对象
     */
    private Object proxy;

    /**
     * 代理方法
     */
    private Method proxyMethod;

    /**
     * 代理方法参数 传参
     */
    private Object[] proxyMethodArgs;


    /**
     * 服务端传输层数据
     */
    Transport transport;

    /**
     * 广播所有服务
     */
    boolean broadcast;
    /**
     * 异步广播所有服务
     */
    boolean broadcastAsync;

    /**
     * 直连地址
     */
    String url;

    /**
     * 最大重试次数
     */
    Integer invokeRetryMaxCount;
}
