package easy4j.infra.rpc.serializable;

public class HessionSerializable implements ISerializable {

    @Override
    public byte[] serializable(Object object) {
        return new byte[0];
    }

    @Override
    public <T> T deserializable(byte[] object, Class<T> tClass) {
        return null;
    }
}
