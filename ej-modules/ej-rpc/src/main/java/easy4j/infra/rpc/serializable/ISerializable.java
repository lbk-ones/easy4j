package easy4j.infra.rpc.serializable;

public interface ISerializable {


    byte[] serializable(Object object);

    <T> T deserializable(byte[] object, Class<T> tClass);

}
