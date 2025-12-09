package easy4j.infra.rpc.integrated;

import cn.hutool.core.util.ReflectUtil;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.server.ServerMethodInvoke;

public class DefaultServerInstanceInit implements ServerInstanceInit {

    public static final DefaultServerInstanceInit INSTANCE = new DefaultServerInstanceInit();


    public DefaultServerInstanceInit() {
    }

    @Override
    public Object instance(RpcRequest request) {
        String classIdentify = request.getClassIdentify();
        return ReflectUtil.newInstanceIfPossible(ServerMethodInvoke.getClassByClassIdentify(classIdentify));
    }
}