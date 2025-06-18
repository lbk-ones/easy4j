package easy4j.module.sca.config;

import easy4j.infra.context.ContextChannel;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.sca.Easy4jNacosInvokerApi;

public class ScaContextChannel implements ContextChannel {

    @Override
    public <T> T listener(String name, Class<T> aclass) {
        if (Easy4jNacosInvokerApi.class == aclass || getDefaultName(Easy4jNacosInvokerApi.class).equals(name)) {
            return aclass.cast(NamingServerInvoker.createByEnv(null));
        }
        return null;
    }

    @Override
    public void init(Easy4jContext easy4JContextThread) {

    }
}
