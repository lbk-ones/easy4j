package easy4j.infra.rpc.enums;

public enum SerializableType {

    /**
     * json
     */
    JACKSON,
    /**
     * hession序列化
     */
    HESSION,
    /**
     * Kryo序列化
     */
    KRYO,
    /**
     * protobuf
     */
    PROTOBUF
}
