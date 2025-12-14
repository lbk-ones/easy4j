package easy4j.infra.rpc.utils;


import cn.hutool.core.net.NetUtil;

/**
 * 使用雪花算法生成msgId
 *
 * @author bokun
 * @since 2.0.1
 */
public class SequenceUtils {
    public static final Sequence seq = new Sequence(NetUtil.getLocalhost());


    public static long gen() {
        return seq.nextId();
    }

}
