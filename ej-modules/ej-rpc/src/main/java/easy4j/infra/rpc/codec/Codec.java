package easy4j.infra.rpc.codec;

/**
 * 编码常量
 *
 * @author bokun
 * @since 2.0.1
 */
public class Codec {

    // 魔术字
    public static final int MAGIC_NUMBER = 0x65346A;

    // 版本
    public static final byte VERSION = 1;

    // 头部总长度（不含数据）
    public static final int HEADER_LENGTH = 4 + 1 + 1 + 4 + 1;

    public static final int MAX_BODY_LENGTH = 10 * 1024 * 1024; // 10MB

}
