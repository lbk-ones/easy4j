
package easy4j.infra.rpc.serializable.kryo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.Getter;


@Getter
public class KryoInnerSerializer implements AutoCloseable {

    private final Kryo kryo;

    public KryoInnerSerializer(Kryo kryo) {
        this.kryo = Objects.requireNonNull(kryo);
    }

    public <T> byte[] serialize(T t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(Output output = new Output(baos)){
            kryo.writeClassAndObject(output, t);
            output.flush();
            return baos.toByteArray();
        }
    }

    public <T> T deserialize(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Input input = new Input(bais);
        input.close();
        return (T) kryo.readClassAndObject(input);
    }

    @Override
    public void close() {
        KryoSerializerFactory.getInstance().returnKryo(this);
    }

}
