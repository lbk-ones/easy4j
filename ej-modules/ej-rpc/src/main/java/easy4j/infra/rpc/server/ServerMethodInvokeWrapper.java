package easy4j.infra.rpc.server;

import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.enums.RpcResponseStatus;
import easy4j.infra.rpc.utils.SpiUtils;

import java.util.Comparator;
import java.util.List;

public class ServerMethodInvokeWrapper {

    final ServerMethodInvoke serverMethodInvoke;
    List<ServerHandler> serverHandlerList;

    public ServerMethodInvokeWrapper(ServerMethodInvoke serverMethodInvoke) {
        this.serverMethodInvoke = serverMethodInvoke;
        serverHandlerList = SpiUtils.load(ServerHandler.class);
        serverHandlerList.sort(Comparator.comparing(ServerHandler::getPriority));
    }

    public RpcResponse invoke() {
        RpcResponse invoke = null;
        RpcRequest request = serverMethodInvoke.getRequest();
        Transport transport = serverMethodInvoke.getTransport();
        if (request.mayBeHandler()) {
            String msgError = "";
            for (ServerHandler serverHandler : serverHandlerList) {
                if (serverHandler.support(request, transport)) {
                    try {
                        invoke = serverHandler.handler(request, transport);
                    } catch (Throwable e2) {
                        msgError = e2.getMessage();
                    }
                    break;
                }
            }
            if (invoke == null) {
                invoke = RpcResponse.error(transport.getMsgId(), RpcResponseStatus.SERVER_HANDLER_NOT_FOUND_ERROR, msgError);
            }
        } else {
            invoke = serverMethodInvoke.invoke();
        }
        return invoke;
    }
}
