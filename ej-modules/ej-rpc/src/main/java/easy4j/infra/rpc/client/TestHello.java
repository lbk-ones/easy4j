package easy4j.infra.rpc.client;

import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.serializable.ISerializable;
import easy4j.infra.rpc.serializable.SerializableFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public class TestHello {

    public Map<String, Object> testHello(String input) {
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("hello", input);
        objectObjectHashMap.put("hello2", input);
        return objectObjectHashMap;
    }


    public void testVoid(String input) {
        System.out.println("test void "+input);
    }


    public static String getCodeBase(Class<?> cls) {
        if (cls == null) {
            return null;
        } else {
            ProtectionDomain domain = cls.getProtectionDomain();
            if (domain == null) {
                return null;
            } else {
                CodeSource source = domain.getCodeSource();
                if (source == null) {
                    return null;
                } else {
                    URL location = source.getLocation();
                    return location == null ? null : location.getFile();
                }
            }
        }
    }
}
