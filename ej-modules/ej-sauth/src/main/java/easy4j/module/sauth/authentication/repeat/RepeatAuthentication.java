package easy4j.module.sauth.authentication.repeat;

import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.module.sauth.authentication.AuthenticationContext;

import java.util.Map;

/**
 * 重复会话策略选择
 *
 * @author bokun.li
 * @date 2025/7/31
 */
public class RepeatAuthentication {

    private static final Map<String, RepeatAuthenticationStrategy> authenticationStrategyMap = Maps.newHashMap();

    static {
        authenticationStrategyMap.put("default", new PublicSessionStrategy());
        authenticationStrategyMap.put("new", new NewSessionStrategy());
        authenticationStrategyMap.put("reject", new RejectSessionStrategy());
        authenticationStrategyMap.put("public", new PublicSessionStrategy());
        authenticationStrategyMap.put("kick", new KickSessionStrategy());
    }

    public static void register(String name, RepeatAuthenticationStrategy strategy) {
        authenticationStrategyMap.putIfAbsent(name, strategy);
    }


    public static boolean check(AuthenticationContext ctx) {
        String type = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_SESSION_REPEAT_STRATEGY, "default");
        if (!ctx.isCheckSession()) {
            type = "default";
        }
        RepeatAuthenticationStrategy repeatAuthenticationStrategy = authenticationStrategyMap.get(type);
        return repeatAuthenticationStrategy.checkRepeat(ctx);
    }
}
