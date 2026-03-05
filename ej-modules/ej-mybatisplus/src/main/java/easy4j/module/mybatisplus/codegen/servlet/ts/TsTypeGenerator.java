package easy4j.module.mybatisplus.codegen.servlet.ts;

import cn.hutool.core.util.ReflectUtil;
import easy4j.module.mybatisplus.audit.AutoAudit;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
/**
 * Java 对象转 TypeScript 类型声明工具类
 */
public class TsTypeGenerator {
    // Java 基础类型 → TS 类型 映射表
    private static final Map<Class<?>, String> BASIC_TYPE_MAPPING = new HashMap<>();
    // 缓存已生成的 TS 接口，避免重复定义
    private static final Set<String> GENERATED_INTERFACES = new HashSet<>();
    // 缩进符（美化 TS 代码）
    private static final String INDENT = "  ";
    // JDK 内置包前缀，这些包下的类不生成 TS 接口
    private static final String JDK_PACKAGE_PREFIX = "java.";

    static {
        // 初始化基础类型映射
        BASIC_TYPE_MAPPING.put(String.class, "string");
        BASIC_TYPE_MAPPING.put(Integer.class, "number");
        BASIC_TYPE_MAPPING.put(int.class, "number");
        BASIC_TYPE_MAPPING.put(Long.class, "number");
        BASIC_TYPE_MAPPING.put(long.class, "number");
        BASIC_TYPE_MAPPING.put(Boolean.class, "boolean");
        BASIC_TYPE_MAPPING.put(boolean.class, "boolean");
        BASIC_TYPE_MAPPING.put(Float.class, "number");
        BASIC_TYPE_MAPPING.put(float.class, "number");
        BASIC_TYPE_MAPPING.put(Double.class, "number");
        BASIC_TYPE_MAPPING.put(double.class, "number");
        BASIC_TYPE_MAPPING.put(Void.class, "void");
        BASIC_TYPE_MAPPING.put(void.class, "void");
        BASIC_TYPE_MAPPING.put(byte.class, "number");
        BASIC_TYPE_MAPPING.put(Byte.class, "number");
        BASIC_TYPE_MAPPING.put(short.class, "number");
        BASIC_TYPE_MAPPING.put(Short.class, "number");
        BASIC_TYPE_MAPPING.put(Date.class, "string");
        BASIC_TYPE_MAPPING.put(LocalDate.class, "string");
        BASIC_TYPE_MAPPING.put(LocalDateTime.class, "string");
        BASIC_TYPE_MAPPING.put(Number.class, "number");
    }

    /**
     * 生成单个 Java 类对应的 TS 接口（包含依赖的自定义类型）
     * @param clazz 要转换的 Java 类
     * @param isOptional 是否全部可选
     * @return TS 类型声明字符串
     */
    public static String generateTsInterface(Class<?> clazz,boolean isOptional) {
        // 清空缓存（每次生成新的类型时重置）
        GENERATED_INTERFACES.clear();
        // 递归生成当前类的接口 + 所有依赖的自定义接口
        return generateInterfaceRecursive(clazz,isOptional);
    }

    /**
     * 递归生成 TS 接口（处理自定义对象的依赖）
     * 核心修复：只处理自定义业务类，排除 JDK 内置类、集合、Map、数组、Optional 等
     */
    private static String generateInterfaceRecursive(Class<?> clazz,boolean isOptional) {
        // 跳过以下类型，避免生成错误接口：
        // 1. 基础类型 2. 已生成的接口 3. JDK 内置类 4. 集合/Map 5. Optional 6. 数组
        if (BASIC_TYPE_MAPPING.containsKey(clazz)
                || GENERATED_INTERFACES.contains(clazz.getSimpleName())
                || isJdkBuiltInClass(clazz)
                || Collection.class.isAssignableFrom(clazz)
                || Map.class.isAssignableFrom(clazz)
                || isOptionalType(clazz)
                || clazz.isArray()) {
            return "";
        }

        // 标记当前类已生成，避免递归循环（如 A 包含 B，B 包含 A）
        GENERATED_INTERFACES.add(clazz.getSimpleName());

        // 1. 拼接接口头部
        StringBuilder tsCode = new StringBuilder();
        tsCode.append("export interface ").append(clazz.getSimpleName()).append(" {\n");

        // 2. 获取类的所有字段（过滤静态/最终字段）
        Field[] fields = ReflectUtil.getFields(clazz);
        for (Field field : fields) {
            // 跳过静态/最终字段
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            // 2.1 获取字段名（TS 字段名和 Java 保持一致）
            String fieldName = field.getName();
            // 2.2 获取字段对应的 TS 类型
            String tsType = getTsType(field.getGenericType());
            // 2.3 判断是否为可选字段（Optional 类型则加 ?）
            //isOptional = isOptionalType(field.getType()) || isOptional;
            String optionalMark = isOptional ? "?" : "";

            // 2.4 拼接字段行
            tsCode.append(INDENT)
                    .append(fieldName)
                    .append(optionalMark)
                    .append(": ")
                    .append(tsType)
                    .append(";\n");
        }

        // 3. 拼接接口尾部
        tsCode.append("}\n\n");

        // 4. 递归处理当前类中包含的自定义对象类型（确保依赖的接口也生成）
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Type fieldType = field.getGenericType();
            Class<?> rawType = getRawType(fieldType);

            // 仅递归处理：非基础类型 + 非JDK内置 + 非集合/Map + 非Optional + 非数组 + 未生成过的自定义类
            if (!BASIC_TYPE_MAPPING.containsKey(rawType)
                    && !isJdkBuiltInClass(rawType)
                    && !Collection.class.isAssignableFrom(rawType)
                    && !Map.class.isAssignableFrom(rawType)
                    && !isOptionalType(rawType)
                    && !rawType.isArray()
                    && !GENERATED_INTERFACES.contains(rawType.getSimpleName())) {
                tsCode.append(generateInterfaceRecursive(rawType,isOptional));
            }

            // 处理集合的泛型参数（如 List<User> → 生成 User 接口）
            if (fieldType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) fieldType;
                for (Type actualType : paramType.getActualTypeArguments()) {
                    Class<?> actualClass = getRawType(actualType);
                    // 泛型参数仅处理自定义业务类
                    if (!BASIC_TYPE_MAPPING.containsKey(actualClass)
                            && !isJdkBuiltInClass(actualClass)
                            && !GENERATED_INTERFACES.contains(actualClass.getSimpleName())) {
                        tsCode.append(generateInterfaceRecursive(actualClass,isOptional));
                    }
                }
            }
        }

        return tsCode.toString();
    }

    /**
     * 根据 Type 获取对应的 TS 类型字符串
     * 修复：优化 Optional 泛型参数获取逻辑，避免错误
     */
    private static String getTsType(Type type) {
        // 1. 基础类型直接返回映射值
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (BASIC_TYPE_MAPPING.containsKey(clazz)) {
                return BASIC_TYPE_MAPPING.get(clazz);
            }
            for (Class<?> aClass : BASIC_TYPE_MAPPING.keySet()) {
                if (aClass.isAssignableFrom((Class<?>) type)) {
                    return BASIC_TYPE_MAPPING.get(aClass);
                }
            }
            // 数组类型（如 String[] → string[]）
            if (clazz.isArray()) {
                Class<?> componentType = clazz.getComponentType();
                String componentTsType = BASIC_TYPE_MAPPING.getOrDefault(componentType, componentType.getSimpleName());
                return componentTsType + "[]";
            }
            // Optional 类型（如 Optional<String> → string）
            if (isOptionalType(clazz)) {
                // 修复：Optional 是参数化类型，直接获取其实际类型参数
                if (type instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) type;
                    Type[] actualTypes = paramType.getActualTypeArguments();
                    if (actualTypes.length > 0) {
                        return getTsType(actualTypes[0]);
                    }
                }
                return "any";
            }
            // 自定义对象类型（如 User → User）
            return clazz.getSimpleName();
        }

        // 2. 泛型类型（如 List<String>、Map<String, User>）
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) paramType.getRawType();
            Type[] actualTypes = paramType.getActualTypeArguments();

            // 集合类型（List/Set → TS 数组）
            if (Collection.class.isAssignableFrom(rawType)) {
                if (actualTypes.length > 0) {
                    String itemType = getTsType(actualTypes[0]);
                    return itemType + "[]";
                }
                return "any[]";
            }

            // Map 类型（Map<K, V> → { [key: string]: V }）
            if (Map.class.isAssignableFrom(rawType)) {
                if (actualTypes.length > 1) {
                    String valueType = getTsType(actualTypes[1]);
                    return "{ [key: string]: " + valueType + " }";
                }
                return "{ [key: string]: any }";
            }

            // 其他泛型类型（非集合/Map）
            return getTsType(actualTypes[0]);
        }

        // 兜底：无法识别的类型返回 any
        return "any";
    }

    /**
     * 判断是否为 Optional 类型
     */
    private static boolean isOptionalType(Class<?> clazz) {
        return Optional.class.isAssignableFrom(clazz);
    }

    /**
     * 判断是否为 JDK 内置类（以 java. 开头的包名）
     */
    private static boolean isJdkBuiltInClass(Class<?> clazz) {
        return clazz.getPackage() != null && clazz.getPackage().getName().startsWith(JDK_PACKAGE_PREFIX);
    }

    /**
     * 获取泛型类型的原始类（如 List<String> → List.class）
     */
    private static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return Object.class;
    }

    // 测试入口
    public static void main(String[] args) {
        // 生成 User 类对应的 TS 类型声明
        String tsTypes = generateTsInterface(User.class,true);
        System.out.println("生成的 TS 类型声明：");
        System.out.println(tsTypes);
    }
}

// -------------------------- 测试用的 Java 实体类 --------------------------
/**
 * 地址类（自定义对象）
 */
class Address {
    private String street;       // 街道
    private Integer zipCode;     // 邮编
    private String[] tags;       // 标签数组
}

/**
 * 订单类（自定义对象）
 */
class Order extends AutoAudit {
    private Long id;             // 订单ID
    private Double amount;       // 订单金额
    private boolean isPaid;      // 是否支付
}

/**
 * 用户类（包含各种类型字段）
 */
class User {
    private Integer id;                  // 基础类型（包装类）
    private String name;                 // 基础类型（String）
    private int age;                     // 基础类型（基本类型）
    private boolean isVip;               // 基础类型（boolean）
    private Address address;             // 自定义对象
    private List<Order> orders;          // 泛型集合
    private Map<String, String> extInfo; // Map 类型
    private Optional<String> nickname;   // 可选类型
    private BigDecimal num2;   // 可选类型
    private Double[] scores;             // 数组类型
}