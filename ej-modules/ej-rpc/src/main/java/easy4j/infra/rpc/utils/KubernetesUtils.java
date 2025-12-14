package easy4j.infra.rpc.utils;

import cn.hutool.core.util.StrUtil;

public class KubernetesUtils {

    public static final Boolean KUBERNETES_MODE = !StrUtil.isEmpty(System.getenv("KUBERNETES_SERVICE_HOST"))
            && !StrUtil.isEmpty(System.getenv("KUBERNETES_SERVICE_PORT"));

    public static boolean isKubernetesMode() {
        return KUBERNETES_MODE;
    }

}