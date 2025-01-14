package easy4j.module.dubbo3;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.header.EasyResult;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

@Activate(
    group = {CommonConstants.PROVIDER},
    order = 10
)
public class ProviderExceptionFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(ProviderExceptionFilter.class);

    public ProviderExceptionFilter() {
    }

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String i18n1 = invocation.getAttachment("i18n");
        Locale locale = null;
        if (StrUtil.isNotBlank(i18n1)) {
            String[] s = i18n1.split("_");
            locale = new Locale(s[0], s[1]);
        }
        String rpcMethod = invoker.getInterface().getName() + ":" + invocation.getMethodName();
        try {
            Result result = invoker.invoke(invocation);
            if (result.hasException() && GenericService.class != invoker.getInterface()) {
                Throwable throwable = result.getException().getCause();
                if (throwable == null) {
                    throwable = result.getException();
                }

                EasyResult<Object> i18n = EasyResult.toI18n(throwable, locale);
                i18n.setRpcMethod(rpcMethod);
                String message = throwable.getMessage();
                if(throwable instanceof NullPointerException){
                    message = "java.lang.NullPointerException";
                }
                i18n.setErrorInfo(message);
                return AsyncRpcResult.newDefaultAsyncResult(new AppResponse(i18n),invocation);
            } else {
                return result;
            }
        } catch (Throwable var7) {
            EasyResult<Object> i18n = EasyResult.toI18n(var7, locale);
            i18n.setRpcMethod(rpcMethod);
            return AsyncRpcResult.newDefaultAsyncResult(new AppResponse(i18n),invocation);
        }
    }
}
