package easy4j.module.mybatisplus;

import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.context.ContextChannel;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.seed.LeafSeed;
import easy4j.infra.context.api.seed.MybatisPlusSnowSeed;
import easy4j.infra.context.api.seed.SnowSeed;

import java.util.Objects;

public class MybatisPlusContextChannel implements ContextChannel {
    public static Easy4jContext easy4jContext2;

    @Override
    public <T> T listener(String name, Class<T> aclass) {
        if (aclass == null) {
            return null;
        }
        if (MybatisPlusSnowSeed.class == aclass || Objects.equals(name, getDefaultName(MybatisPlusSnowSeed.class))) {
            IdGenner bean = SpringUtil.getBean(IdGenner.class);
            return aclass.cast(bean);
        }
        return null;
    }

    @Override
    public void init(Easy4jContext easy4JContextThread2) {
        easy4jContext2 = easy4JContextThread2;
    }
}
