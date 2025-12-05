package easy4j.infra.rpc.client;

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
}
