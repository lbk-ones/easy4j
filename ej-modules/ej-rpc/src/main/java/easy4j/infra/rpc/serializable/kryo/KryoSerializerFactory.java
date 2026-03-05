package easy4j.infra.rpc.serializable.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.serializable.SerializerSecurityRegistry;

public class KryoSerializerFactory {
    private static final KryoSerializerFactory FACTORY = new KryoSerializerFactory();
    private final Pool<Kryo> pool = new Pool<Kryo>(true, true, IntegratedFactory.getConfig().getKryoPoolMaxNum()) {
        protected Kryo create() {
            Kryo kryo = new Kryo();
            // 启用对象引用跟踪
            kryo.setReferences(true);
            // 必须注册
            kryo.setRegistrationRequired(true);
            SerializerSecurityRegistry.getAllowClassType().forEach(kryo::register);
            return kryo;
        }
    };

    private KryoSerializerFactory() {
    }

    public static KryoSerializerFactory getInstance() {
        return FACTORY;
    }

    public KryoInnerSerializer get() {
        return new KryoInnerSerializer(this.pool.obtain());
    }

    public void returnKryo(KryoInnerSerializer kryoSerializer) {
        if (kryoSerializer == null) {
            throw new IllegalArgumentException("kryoSerializer is null");
        } else {
            this.pool.free(kryoSerializer.getKryo());
        }
    }
}