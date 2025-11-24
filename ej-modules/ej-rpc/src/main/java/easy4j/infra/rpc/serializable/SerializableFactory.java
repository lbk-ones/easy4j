package easy4j.infra.rpc.serializable;

import easy4j.infra.rpc.config.BaseConfig;
import easy4j.infra.rpc.enums.SerializableType;

public class SerializableFactory {


    public static ISerializable get(BaseConfig baseConfig) {
        SerializableType serializableType = baseConfig.getSerializableType();
        if (serializableType == SerializableType.JACKSON) {
            return new JacksonSerializable();
        } else if (serializableType == SerializableType.HESSION) {
            return new HessionSerializable();
        }
        throw new IllegalArgumentException("serializable type is not support" + serializableType);
    }


}
