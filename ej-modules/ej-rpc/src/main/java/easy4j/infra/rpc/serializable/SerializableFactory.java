package easy4j.infra.rpc.serializable;

import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.enums.SerializableType;
import easy4j.infra.rpc.integrated.IntegratedFactory;

public class SerializableFactory {

    private static final class JacksonSerializableHolder {
        private static final JacksonSerializable jacksonSerializable = new JacksonSerializable();
    }

    public static ISerializable get() {
        E4jRpcConfig config = IntegratedFactory.getRpcConfig().getConfig();
        SerializableType serializableType = config.getSerializableType();
        if (serializableType == SerializableType.JACKSON) {
            return JacksonSerializableHolder.jacksonSerializable;
        } else if (serializableType == SerializableType.HESSION) {
            return new HessionSerializable();
        }
        throw new IllegalArgumentException("serializable type is not support" + serializableType);
    }

    public static ISerializable getJackson() {
        return JacksonSerializableHolder.jacksonSerializable;
    }


}
