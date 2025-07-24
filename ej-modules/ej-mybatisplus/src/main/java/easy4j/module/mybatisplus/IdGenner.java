package easy4j.module.mybatisplus;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import easy4j.infra.context.AutoRegisterContext;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.seed.MybatisPlusSnowSeed;

public class IdGenner implements MybatisPlusSnowSeed, AutoRegisterContext {

    //public static final String idgenType = "snow";
    private static final DefaultIdentifierGenerator idGen = DefaultIdentifierGenerator.getInstance();

    public static IdentifierGenerator get(){
        return idGen;
    }

    @Override
    public String nextIdStr() {
        return String.valueOf(idGen.nextId(null));
    }

    @Override
    public long nextIdLong() {
        return idGen.nextId(null);
    }

    @Override
    public void registerToContext(Easy4jContext easy4jContext) {
        IdGenner bean = SpringUtil.getBean(IdGenner.class);
        easy4jContext.register(bean);
    }
}
