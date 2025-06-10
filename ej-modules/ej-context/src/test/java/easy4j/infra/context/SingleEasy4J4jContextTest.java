package easy4j.infra.context;

import easy4j.infra.common.exception.EasyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SingleEasy4J4jContextTest {

    private SingleEasy4J4jContext context;

    @BeforeEach
    void setUp() {
        context = new SingleEasy4J4jContext();
        // 清除静态变量，确保每次测试环境干净
        clearStaticFields();
    }

    // 清除静态字段的工具方法
    private void clearStaticFields() {
        // 使用反射清除静态字段
        try {
            Method clear = SingleEasy4J4jContext.class.getDeclaredMethod("clear");
            clear.setAccessible(true);
            clear.invoke(context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear static fields", e);
        }
    }

    // 测试基本注册和获取功能
    @Test
    void testRegisterAndGet() {
        // 准备测试对象
        TestService testService = new TestServiceImpl();
        context.register(testService);

        // 通过类型获取
        TestService retrieved = context.get(TestService.class);
        assertNotNull(retrieved);
        assertSame(testService, retrieved);

        // 通过名称和类型获取
        TestService namedRetrieved = context.get("testServiceImpl", TestService.class);
        assertNotNull(namedRetrieved);
        assertSame(testService, namedRetrieved);

        // 通过名称获取
        Object objectRetrieved = context.get("testServiceImpl");
        assertNotNull(objectRetrieved);
        assertSame(testService, objectRetrieved);
    }

    // 测试重复注册被静默处理
    @Test
    void testRepeatRegister() {
        TestService testService1 = new TestServiceImpl();
        TestService testService2 = new TestServiceImpl();

        // 第一次注册
        context.register(testService1);
        // 重复注册相同名称
        context.register("testServiceImpl", testService2);

        // 验证获取的是第一个注册的对象
        TestService retrieved = context.get(TestService.class);
        assertSame(testService1, retrieved);
    }

    // 测试注册时类型映射正确
    @Test
    void testTypeMapping() {
        TestServiceImpl impl = new TestServiceImpl();
        context.register(impl);

        // 验证接口类型映射
        String[] names = context.getNamesOfType(TestService.class);
        assertNotNull(names);
        assertEquals(1, names.length);
        assertEquals("testServiceImpl", names[0]);

        // 验证实现类类型映射
        String[] implNames = context.getNamesOfType(TestServiceImpl.class);
        assertNotNull(implNames);
        assertEquals(1, implNames.length);
        assertEquals("testServiceImpl", implNames[0]);

        // 验证父类类型映射
        String[] parentNames = context.getNamesOfType(AbstractService.class);
        assertNotNull(parentNames);
        assertEquals(1, parentNames.length);
        assertEquals("testServiceImpl", parentNames[0]);
    }

    // 测试获取同类型的所有对象
    @Test
    void testGetMapOfType() {
        TestService service1 = new TestServiceImpl();
        TestService service2 = new AnotherTestServiceImpl();

        context.register(service1);
        context.register(service2);

        Map<String, TestService> map = context.getMapOfType(TestService.class);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertTrue(map.containsKey("testServiceImpl"));
        assertTrue(map.containsKey("anotherTestServiceImpl"));
        assertSame(service1, map.get("testServiceImpl"));
        assertSame(service2, map.get("anotherTestServiceImpl"));
    }

    // 测试获取不存在的对象抛出异常
    @Test
    void testGetNonExistentObject() {
        // 测试通过类型获取不存在的对象
        assertThrows(EasyException.class, () -> context.get(NonExistentService.class));

        // 测试通过名称获取不存在的对象
        assertThrows(EasyException.class, () -> context.get("nonExistent"));

        // 测试通过名称和类型获取不存在的对象
        assertThrows(EasyException.class, () -> context.get("nonExistent", TestService.class));
    }

    // 测试当有多个实现时，默认名称优先
    @Test
    void testMultipleImplementationsWithDefaultName() {
        // name = testServiceImpl
        TestService testServiceImpl = new TestServiceImpl();
        // name = anotherTestServiceImpl
        TestService anotherService = new AnotherTestServiceImpl();

        context.register(testServiceImpl);
        context.register(anotherService);

        // 获取 名为 testServiceImpl 的 TestService
        TestService retrieved = context.get("testServiceImpl", TestService.class);
        assertNotNull(retrieved);
        assertSame(testServiceImpl, retrieved);
    }

    // 测试当只有一个实现时，即使名称不匹配也能获取
    @Test
    void testSingleImplementationWithNonDefaultName() {
        TestService customService = new CustomTestServiceImpl();
        context.register("customService", customService);

        // 通过类型获取，应该返回唯一的实现
        TestService retrieved = context.get(TestService.class);
        assertNotNull(retrieved);
        assertSame(customService, retrieved);
    }

    // 测试注册带名称的对象
    @Test
    void testRegisterWithCustomName() {
        TestService service = new TestServiceImpl();
        context.register("customName", service);

        // 通过自定义名称获取
        TestService retrieved = context.get("customName", TestService.class);
        assertNotNull(retrieved);
        assertSame(service, retrieved);

        // 验证默认名称仍然存在
        TestService defaultRetrieved = context.get("testServiceImpl", TestService.class);
        assertNotNull(defaultRetrieved);
        assertSame(service, defaultRetrieved);
    }

    // 测试获取父类类型的对象
    @Test
    void testGetBySuperclass() {
        TestServiceImpl impl = new TestServiceImpl();
        context.register(impl);

        // 通过父类类型获取
        AbstractService retrieved = context.get(AbstractService.class);
        assertNotNull(retrieved);
        assertSame(impl, retrieved);
    }

    // 测试注册 null 对象
    @Test
    void testRegisterNull() {
        context.register(null);
        context.register("test", null);

        // 验证上下文未被修改
        assertTrue(context.getMapOfType(TestService.class).isEmpty());
    }

    // 测试获取接口的父接口类型的对象
    @Test
    void testRegisterWithInterfaceHierarchy() {
        AdvancedTestService service = new AdvancedTestServiceImpl();
        context.register(service);

        // 通过父接口获取
        TestService retrieved = context.get(TestService.class);
        assertNotNull(retrieved);
        assertSame(service, retrieved);
    }

    // 测试多重继承关系
    @Test
    void testMultipleInheritance() {
        SubServiceImpl subService = new SubServiceImpl();
        context.register(subService);

        // 通过直接父类获取
        TestServiceImpl retrievedParent = context.get(TestServiceImpl.class);
        assertNotNull(retrievedParent);
        assertSame(subService, retrievedParent);

        // 通过祖父类获取
        AbstractService retrievedGrandparent = context.get(AbstractService.class);
        assertNotNull(retrievedGrandparent);
        assertSame(subService, retrievedGrandparent);

        // 通过接口获取
        TestService retrievedInterface = context.get(TestService.class);
        assertNotNull(retrievedInterface);
        assertSame(subService, retrievedInterface);
    }

    // 测试获取类型的所有名称
    @Test
    void testGetNamesOfType() {
        TestService service1 = new TestServiceImpl();
        TestService service2 = new AnotherTestServiceImpl();

        context.register(service1);
        context.register(service2);

        String[] names = context.getNamesOfType(TestService.class);
        assertNotNull(names);
        assertEquals(2, names.length);
        assertTrue(Arrays.asList(names).contains("testServiceImpl"));
        assertTrue(Arrays.asList(names).contains("anotherTestServiceImpl"));
    }

    // 测试当有多个实现且无默认名称时抛出异常
    @Test
    void testMultipleImplementationsWithoutDefaultName() {
        TestService service1 = new CustomTestServiceImpl();
        TestService service2 = new AnotherCustomTestServiceImpl();

        context.register("custom1", service1);
        context.register("custom2", service2);

        // 没有默认名称且有多个实现，应该抛出异常
        assertThrows(EasyException.class, () -> context.get(TestService.class));
    }

    // 测试类层次结构中的多个接口
    @Test
    void testMultipleInterfacesInHierarchy() {
        MultiInterfaceServiceImpl service = new MultiInterfaceServiceImpl();
        context.register(service);

        // 通过不同接口获取
        FirstInterface first = context.get(FirstInterface.class);
        SecondInterface second = context.get(SecondInterface.class);
        BaseInterface base = context.get(BaseInterface.class);

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(base);
        assertSame(service, first);
        assertSame(service, second);
        assertSame(service, base);
    }

    // 测试内部类注册
    @Test
    void testInnerClassRegistration() {
        // 创建内部类实例
        class InnerServiceImpl implements TestService {
            @Override
            public String getName() {
                return "InnerService";
            }
        }

        InnerServiceImpl innerService = new InnerServiceImpl();
        context.register(innerService);

        // 通过类型获取
        TestService retrieved = context.get(TestService.class);
        assertNotNull(retrieved);
        assertSame(innerService, retrieved);

        // 验证名称（内部类名称会包含$符号）
        String[] names = context.getNamesOfType(TestService.class);
        assertEquals(1, names.length);
//        assertTrue(names[0].startsWith("singleEasy4J4jContextTest$"));
    }

    // 测试注册后修改对象
    @Test
    void testModifyRegisteredObject() {
        MutableService mutableService = new MutableService();
        mutableService.setValue("initial");
        context.register(mutableService);

        // 修改对象状态
        mutableService.setValue("modified");

        // 获取对象并验证状态
        MutableService retrieved = context.get(MutableService.class);
        assertEquals("modified", retrieved.getValue());
    }

    // 测试注册不同类型但相同名称的对象（应该被忽略）
    @Test
    void testRegisterDifferentTypesWithSameName() {
        TestService testService = new TestServiceImpl();
        AnotherService anotherService = new AnotherServiceImpl();

        // 先注册 TestService
        context.register("sharedName", testService);
        // 尝试用相同名称注册 AnotherService
        assertThrows(EasyException.class, () -> context.register("sharedName", anotherService));

        // 验证获取的是第一个注册的对象
        TestService retrieved = context.get("sharedName", TestService.class);
        assertNotNull(retrieved);
        assertSame(testService, retrieved);

        // 尝试获取 AnotherService 应该失败
        assertThrows(EasyException.class, () -> context.get("sharedName", AnotherService.class));
    }

    // 测试获取对象的映射
    @Test
    void testGetMapOfTypeWithMultipleImplementations() {
        TestService service1 = new TestServiceImpl();
        TestService service2 = new AnotherTestServiceImpl();

        context.register(service1);
        context.register(service2);

        Map<String, TestService> map = context.getMapOfType(TestService.class);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertTrue(map.containsKey("testServiceImpl"));
        assertTrue(map.containsKey("anotherTestServiceImpl"));
    }

    // 测试获取对象的映射为空
    @Test
    void testGetMapOfTypeEmpty() {
        Map<String, NonExistentService> map = context.getMapOfType(NonExistentService.class);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    // 测试注册匿名类
    @Test
    void testAnonymousClassRegistration() {
        TestService anonymousService = () -> "AnonymousService";

        context.register(anonymousService);

        // 通过类型获取
        TestService retrieved = context.get(TestService.class);
        assertNotNull(retrieved);
        assertSame(anonymousService, retrieved);

        // 验证名称（匿名类名称会包含$数字）
        String[] names = context.getNamesOfType(TestService.class);
        assertEquals(1, names.length);
    }

    // 测试获取对象时类型不匹配
    @Test
    void testGetWithTypeMismatch() {
        TestService testService = new TestServiceImpl();
        context.register(testService);

        // 尝试以错误的类型获取
        assertThrows(EasyException.class, () -> context.get("testServiceImpl", AnotherService.class));
    }

    // 测试获取对象时使用原始类型
    @Test
    void testGetWithRawType() {
        List<String> list = new ArrayList<>();
        context.register("stringList", list);

        // 通过原始类型获取
        List retrieved = context.get("stringList", List.class);
        assertNotNull(retrieved);
        assertSame(list, retrieved);
    }

    // 测试获取对象时使用泛型类型（类型擦除）
    @Test
    void testGetWithGenericType() {
        List<String> stringList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();

        context.register("stringList", stringList);
        context.register("intList", intList);

        // 由于类型擦除，这里不能通过泛型类型精确区分
        List<String> retrievedStringList = context.get("stringList", List.class);
        List<Integer> retrievedIntList = context.get("intList", List.class);

        assertNotNull(retrievedStringList);
        assertNotNull(retrievedIntList);
        assertSame(stringList, retrievedStringList);
        assertSame(intList, retrievedIntList);
    }

    // 测试注册具有复杂继承关系的对象
    @Test
    void testComplexInheritanceRegistration() {
        ComplexServiceImpl complexService = new ComplexServiceImpl();
        context.register(complexService);

        // 通过不同层级的接口和类获取
        TopLevelInterface top = context.get(TopLevelInterface.class);
        MiddleInterface middle = context.get(MiddleInterface.class);
        BaseClass base = context.get(BaseClass.class);
        ComplexServiceImpl impl = context.get(ComplexServiceImpl.class);

        assertNotNull(top);
        assertNotNull(middle);
        assertNotNull(base);
        assertNotNull(impl);
        assertSame(complexService, top);
        assertSame(complexService, middle);
        assertSame(complexService, base);
        assertSame(complexService, impl);
    }

    // 测试注册多个不同类型但相同接口的对象
    @Test
    void testMultipleImplementationsSameInterface() {
        FirstServiceImpl firstService = new FirstServiceImpl();
        SecondServiceImpl secondService = new SecondServiceImpl();

        context.register(firstService);
        context.register(secondService);

        // 通过共同接口获取
        CommonInterface first = context.get("firstServiceImpl", CommonInterface.class);
        CommonInterface second = context.get("secondServiceImpl", CommonInterface.class);

        assertNotNull(first);
        assertNotNull(second);
        assertSame(firstService, first);
        assertSame(secondService, second);

        // 获取所有实现
        Map<String, CommonInterface> map = context.getMapOfType(CommonInterface.class);
        assertEquals(2, map.size());
    }

    // 测试注册带有多个接口的对象
    @Test
    void testObjectWithMultipleInterfaces() {
        MultiRoleServiceImpl service = new MultiRoleServiceImpl();
        context.register(service);

        // 通过不同接口获取
        Role1 role1 = context.get(Role1.class);
        Role2 role2 = context.get(Role2.class);
        BaseRole baseRole = context.get(BaseRole.class);

        assertNotNull(role1);
        assertNotNull(role2);
        assertNotNull(baseRole);
        assertSame(service, role1);
        assertSame(service, role2);
        assertSame(service, baseRole);
    }

    // 测试注册后通过不同方式获取对象
    @Test
    void testRetrieveObjectByDifferentWays() {
        TestService service = new TestServiceImpl();
        context.register("customName", service);

        // 通过默认名称获取
        TestService defaultRetrieved = context.get("testServiceImpl", TestService.class);
        assertNotNull(defaultRetrieved);
        assertSame(service, defaultRetrieved);

        // 通过自定义名称获取
        TestService customRetrieved = context.get("customName", TestService.class);
        assertNotNull(customRetrieved);
        assertSame(service, customRetrieved);

        // 通过类型获取
        TestService typeRetrieved = context.get(TestService.class);
        assertNotNull(typeRetrieved);
        assertSame(service, typeRetrieved);
    }

    // 测试注册后验证所有类型映射
    @Test
    void testVerifyAllTypeMappings() {
        SubServiceImpl subService = new SubServiceImpl();
        context.register(subService);

        // 验证所有可能的类型映射
        assertTrue(containsName("subServiceImpl", TestService.class));
        assertTrue(containsName("subServiceImpl", TestServiceImpl.class));
        assertTrue(containsName("subServiceImpl", SubServiceImpl.class));
        assertTrue(containsName("subServiceImpl", AbstractService.class));
//        assertTrue(containsName("subServiceImpl", BaseInterface.class));
    }

    // 辅助方法：验证某个类型的映射中是否包含指定名称
    private boolean containsName(String name, Class<?> type) {
        String[] names = context.getNamesOfType(type);
        for (String n : names) {
            if (n.equals(name)) {
                return true;
            }
        }
        return false;
    }

    // ===== 测试用接口和类定义 =====

    interface TestService {
        String getName();
    }

    interface AdvancedTestService extends TestService {
        String getAdvancedName();
    }

    interface AnotherService {
        String getAnotherName();
    }

    interface NonExistentService {
    }

    interface FirstInterface extends BaseInterface {
    }

    interface SecondInterface extends BaseInterface {
    }

    interface BaseInterface {
    }

    interface TopLevelInterface {
    }

    interface MiddleInterface extends TopLevelInterface {
    }

    interface CommonInterface {
    }

    interface Role1 extends BaseRole {
    }

    interface Role2 extends BaseRole {
    }

    interface BaseRole {
    }

    abstract static class AbstractService implements TestService {
    }

    static class TestServiceImpl extends AbstractService {
        @Override
        public String getName() {
            return "TestServiceImpl";
        }
    }

    static class AnotherTestServiceImpl implements TestService {
        @Override
        public String getName() {
            return "AnotherTestServiceImpl";
        }
    }

    static class CustomTestServiceImpl implements TestService {
        @Override
        public String getName() {
            return "CustomTestServiceImpl";
        }
    }

    static class AnotherCustomTestServiceImpl implements TestService {
        @Override
        public String getName() {
            return "AnotherCustomTestServiceImpl";
        }
    }

    static class AnotherServiceImpl implements AnotherService {
        @Override
        public String getAnotherName() {
            return "AnotherServiceImpl";
        }
    }

    static class AdvancedTestServiceImpl implements AdvancedTestService {
        @Override
        public String getName() {
            return "AdvancedTestServiceImpl";
        }

        @Override
        public String getAdvancedName() {
            return "Advanced";
        }
    }

    static class SubServiceImpl extends TestServiceImpl {
    }


    static class MultiInterfaceServiceImpl implements FirstInterface, SecondInterface {
        @Override
        public String toString() {
            return "MultiInterfaceServiceImpl";
        }
    }

    static class MutableService {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    static class BaseClass {
    }

    static class ComplexServiceImpl extends BaseClass implements MiddleInterface {
        @Override
        public String toString() {
            return "ComplexServiceImpl";
        }
    }

    static class FirstServiceImpl implements CommonInterface {
        @Override
        public String toString() {
            return "FirstServiceImpl";
        }
    }

    static class SecondServiceImpl implements CommonInterface {
        @Override
        public String toString() {
            return "SecondServiceImpl";
        }
    }

    static class MultiRoleServiceImpl implements Role1, Role2 {
        @Override
        public String toString() {
            return "MultiRoleServiceImpl";
        }
    }
}