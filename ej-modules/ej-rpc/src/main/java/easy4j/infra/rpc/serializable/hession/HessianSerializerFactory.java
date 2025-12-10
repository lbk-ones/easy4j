
package easy4j.infra.rpc.serializable.hession;

import com.caucho.hessian.io.SerializerFactory;
import easy4j.infra.rpc.serializable.SerializerSecurityRegistry;

/*
 * hession  Serializer factory
 */
public class HessianSerializerFactory extends SerializerFactory {
    public static final SerializerFactory INSTANCE = new HessianSerializerFactory();

    private HessianSerializerFactory() {
        super();
        //Serialization whitelist
        super.getClassFactory().setWhitelist(true);
        //register allow types
        registerAllowTypes();
        //register deny types
        registerDenyTypes();
    }

    public static SerializerFactory getInstance() {
        return INSTANCE;
    }

    private void registerAllowTypes() {
        for (String pattern : SerializerSecurityRegistry.getAllowClassPattern()) {
            super.getClassFactory().allow(pattern);
        }
    }

    private void registerDenyTypes() {
        for (String pattern : SerializerSecurityRegistry.getDenyClassPattern()) {
            super.getClassFactory().deny(pattern);
        }
    }
}
