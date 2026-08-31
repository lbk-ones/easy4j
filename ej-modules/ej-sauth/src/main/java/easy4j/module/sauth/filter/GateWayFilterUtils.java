package easy4j.module.sauth.filter;

import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.context.api.user.UserContext;
import io.github.lbkones.pure.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static easy4j.module.sauth.filter.Easy4jSecurityFilterInterceptor.bindUserContext;

/**
 * 网关用户信息提取工具
 *
 * @author bokun.li
 */
public class GateWayFilterUtils {

    /**
     * 给网关过来的用户信息单独处理,取header中的信息重新组装成 UserContext 并放到上下文和请求属性去
     *
     * @param supplier
     * @see io.github.lbkones.cloud.openfeign.PublicHeaders#initHeader(Object)
     */
    public static void bindUserCtxFromGateway(Supplier<UserContext> supplier,String userContextKey,HttpServletRequest request) {
        if (supplier == null || request == null) return;
        // 如果是从网关过来之后，再调了其他服务，如果通过网关掉的，那么未作header头限制的规则则会自动转发，不通过网关调用的，也会直接携带过去
        // 只在网关哪里获取一次用户信息之后，用户信息则放到请求头一直转递
        UserContext userContext = fromRequest(request);
        if (userContext == null) {
            userContext = supplier.get();
        }
        if (userContext != null) {
            bindUserContext(userContext);
            request.setAttribute(StrUtil.blankToDefault(userContextKey, SysConstant.SESSION_USER), userContext);
        }
    }


    public static UserContext fromRequest(HttpServletRequest request) {
        UserContext userContext = new UserContext();
        AtomicBoolean has = new AtomicBoolean(false);
        userContext.keysVisitor((s, s2) -> {
            String header = request.getHeader(s);
            if (StrUtil.isNotBlank(header)) {
                if(!has.get()){
                    has.set(true);
                }
                userContext.set(s, header);
            }
        });
        return has.get() ? userContext : null;
    }


}
