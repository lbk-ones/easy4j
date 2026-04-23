package easy4j.module.sauth.core;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.context.api.sca.Easy4jNacosInvokerApi;
import easy4j.module.sauth.core.loaduser.LoadUserByRpcDefault;

public class NacosInvokerApi {

    static volatile Easy4jNacosInvokerApi easy4jNacosInvokerApi;

    public static Easy4jNacosInvokerApi getEasy4jNacosInvokerApi(){
        if(easy4jNacosInvokerApi == null){
            synchronized (LoadUserByRpcDefault.class){
                if(easy4jNacosInvokerApi == null){
                    easy4jNacosInvokerApi = Easy4j.getContext().get(Easy4jNacosInvokerApi.class);
                }
            }
        }
        return easy4jNacosInvokerApi;
    }
}
