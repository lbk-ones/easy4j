package easy4j.infra.rpc.serializable.kryo;


import easy4j.infra.rpc.serializable.ISerializable;

/**
 * Kryo 5.x 支持 JDK 8~17+，4.x 仅支持 JDK 8+
 * Kryo 序列化（两端版本必须一致）
 * Java 专属，性能顶尖（比 Hessian 快 2~3 倍），体积极小，支持循环引用；
 * 要求：
 * 1、无参构造器
 * 2、getset
 */
public class KryoSerializable implements ISerializable {

    @Override
    public byte[] serializable(Object object) {
        if (object == null) {
            return new byte[0];
        }
        try (KryoInnerSerializer kryoInnerSerializer = KryoSerializerFactory.getInstance().get()) {
            return kryoInnerSerializer.serialize(object);
        }
    }

    @Override
    public <T> T deserializable(byte[] object, Class<T> tClass) {
        if (object == null || object.length == 0) {
            return null;
        }
        try (KryoInnerSerializer kryoInnerSerializer = KryoSerializerFactory.getInstance().get()) {
            return kryoInnerSerializer.deserialize(object);
        }
    }
}
