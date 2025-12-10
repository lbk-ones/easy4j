package easy4j.infra.rpc.serializable;

import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.enums.SerializableType;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.serializable.kryo.KryoSerializable;

public class SerializableFactory {

    private static final class JacksonSerializableHolder {
        private static final JacksonSerializable jacksonSerializable = new JacksonSerializable();
    }
    private static final class HessionSerializableHolder {
        private static final HessionSerializable hessionSerializable = new HessionSerializable();
    }
    private static final class KryoSerializableHolder {
        private static final KryoSerializable kryoSerializable = new KryoSerializable();
    }

    public static ISerializable get() {
        E4jRpcConfig config = IntegratedFactory.getConfig();
        SerializableType serializableType = config.getSerializableType();
        if (serializableType == SerializableType.JACKSON) {
            return JacksonSerializableHolder.jacksonSerializable;
        } else if (serializableType == SerializableType.HESSION) {
            return HessionSerializableHolder.hessionSerializable;
        } else if (serializableType == SerializableType.KRYO) {
            return KryoSerializableHolder.kryoSerializable;
        }
        throw new IllegalArgumentException("serializable type is not support" + serializableType);
    }

    public static ISerializable getJackson() {
        return JacksonSerializableHolder.jacksonSerializable;
    }
    public static ISerializable getHession() {
        return HessionSerializableHolder.hessionSerializable;
    }

    public static ISerializable getKryo() {
        return KryoSerializableHolder.kryoSerializable;
    }

}
