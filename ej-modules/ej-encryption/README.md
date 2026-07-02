# ej-encryption 模块

ej-encryption 是一个处理接口整体加密、解密和关键字段脱敏的模块。通过 `@EnableEasy4jEncryption` 注解引入，仅在 Spring MVC 环境下生效。

## 功能特性

- ✅ 接口请求/响应加密解密
- ✅ 字段脱敏处理
- ✅ 可扩展的加密方式（默认支持 RSA）
- ✅ 灵活的注解配置
- ✅ 无需修改业务代码
- ✅ 后端只使用私钥，安全高效

## 加密流程

```
前端 (使用公钥)                    后端 (使用私钥)
  |                                 |
  +-- 加密请求 -----> HTTP -------> 解密过滤器
  |                                 |
  +<-- 解密响应 <---- HTTP <------- 加密 ResponseBodyAdvice
```

## 快速开始

### 1. 在启动类上添加注解

```java
@SpringBootApplication
@EnableEasy4jEncryption
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. 配置 application.yml

```yaml
easy4j:
  encryption:
    enabled: true
    encryption-type: rsa
    disabled-api-enc: false
    rsa-block-size: 245
    private-key: MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDTrxxxxxx  # 基于 Base64 编码的私钥
    public-key: MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDTrxxxxxx  # 基于 Base64 编码的私钥
```

## 配置说明

| 配置项                                  | 说明                                     | 必需     | 默认值         |
|--------------------------------------|----------------------------------------|--------|-------------|
| `easy4j.encryption.enabled`          | 是否启用加解密功能                              | 否      | false       |
| `easy4j.encryption.encryption-type`  | 加解密方式                                  | 否      | rsa-private |
| `easy4j.encryption.private-key`      | RSA 私钥（Base64编码）                       | 是（启用时） | -           |
| `easy4j.encryption.public-key`       | RSA 公钥（Base64编码）                       | 否（启用时） | -           |
| `easy4j.encryption.disabled-api-enc` | 禁用接口加解密，只保留接口响应字段脱敏                    | 否      | true        |
| `easy4j.encryption.rsa-block-size`   | rsa算法分块加解密每一块儿大小,RSA-1024和RSA-2048值不一样 | 是      | 245         |
| `easy4j.encryption.skip-list`        | 跳过加解密的包                                | 否      | -           |

## 注解使用

### @NoEncrypt

在 Controller 类或方法上添加 `@NoEncrypt` 注解，表示该接口跳过加解密处理。

```java
@RestController
@RequestMapping("/api")
public class UserController {

    // 这个接口不会进行加解密处理
    @GetMapping("/public")
    @NoEncrypt
    public String publicEndpoint() {
        return "This is public";
    }

    // 这个接口会进行加解密处理
    @PostMapping("/private")
    public User createUser(@RequestBody User user) {
        return user;
    }
}
```

或者在类上添加注解，整个类的所有方法都不会进行加解密：

```java
@RestController
@RequestMapping("/api")
@NoEncrypt
public class PublicController {
    // 所有方法都不会进行加解密处理
}
```

### @MaskField

在响应实体的字段上添加 `@MaskField` 注解，表示该字段进行脱敏处理。

```java
public class UserResponse {
    
    private String username;
    
    // 保留前 2 位，后 2 位，中间用 * 代替
    @MaskField(prefixLength = 2, suffixLength = 2)
    private String phone;
    
    // 保留前 3 位，其余用 * 代替
    @MaskField(prefixLength = 3)
    private String email;
    
    // 完全脱敏（保留前 0 位，后 0 位）
    @MaskField
    private String idCard;
}
```

## 加密方式扩展

### 实现自定义加密提供者

如果需要支持其他加密方式，实现 `EncryptionProvider` 接口：

```java
public class AesEncryptionProvider implements EncryptionProvider {

    @Override
    public void setPrivateKey(String key) {
        // 密钥赋值回调
    }

    @Override
    public void setPublicKey(String key) {
        // 密钥赋值回调
    }
    
    @Override
    public String getName() {
        return "aes";
    }

    @Override
    public String encrypt(String plaintext) {
        // 实现 AES 加密逻辑
        return encryptedData;
    }

    @Override
    public String decrypt(String ciphertext) {
        // 实现 AES 解密逻辑
        return decryptedData;
    }
}
```

### 注册自定义加密提供者

```java
@Configuration
public class CustomEncryptionConfig {
    
    @Bean
    public AesEncryptionProvider aesEncryptionProvider() {
        AesEncryptionProvider provider = new AesEncryptionProvider();
        EncryptionProviderFactory.register("aes", provider);
        return provider;
    }
}
```

## 使用 API

### EncryptionUtil 工具类

提供了便捷的加密和脱敏工具方法：

```java
@Service
public class UserService {
    
    @Autowired
    private EncryptionProperties encryptionProperties;
    
    public void example() {
        EncryptionProvider provider = EncryptionProviderFactory.get("rsa-private");
        
        // 加密对象
        User user = new User();
        EncryptedResponse<User> encrypted = EncryptionUtil.encryptObject(user, provider,true);
        
        // 解密对象
        EncryptedRequest<User> request = new EncryptedRequest<>(encryptedData);
        User decrypted = EncryptionUtil.decryptObject(request, User.class, provider);
        
        // 脱敏字段
        user = MaskingUtil.maskFields(user);
        
        // 脱敏字符串
        String phone = "13800138000";
        String masked = MaskingUtil.maskString(phone, 3, 4);  // 138****8000
    }
}
```

## 请求和响应格式

### 加密请求

```json
{
  "data": "RSA_ENCRYPTED_BASE64_DATA"
}
```

### 加密响应

```json
{
  "data": "RSA_ENCRYPTED_BASE64_DATA"
}
```

## 生成 RSA 密钥对

### 使用 Java 代码生成

运行 `RsaKeyGenerator` 的 main 方法：

```java
void main() {
    io.github.lbkones.encryption.util.RsaKeyGenerator.gen(1024 || 2048 || null);
}
```

输出示例：
```
============ RSA Key Pair ============
Public Key (Base64 - 前端使用):
MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANOvxxxxxxxx...

Private Key (Base64 - 后端使用):
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDTrxxxxxx...
====================================

# For application.yml (backend server)
easy4j:
  encryption:
    enabled: true
    encryption-type: rsa
    private-key: MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDTrxxxxxx...
```

### 使用 OpenSSL 生成

```bash
# 生成私钥
openssl genrsa -out private_key.pem 1024

# 生成公钥（前端使用）
openssl rsa -in private_key.pem -pubout -out public_key.pem

# 转换为 PKCS8 格式（后端使用）
openssl pkcs8 -topk8 -inform PEM -outform PEM -in private_key.pem -out private_key_pkcs8.pem -nocrypt

# 用 Base64 编码
base64 -w 0 private_key_pkcs8.pem  # 后端配置文件使用
base64 -w 0 public_key.pem         # 前端使用
```

## 完整示例

### Entity 定义

```java
public class UserRequest {
    private String username;
    private String password;
    // getters and setters
}

public class UserResponse {
    private Long id;
    private String username;
    
    @MaskField(prefixLength = 2, suffixLength = 2)
    private String phone;
    
    @MaskField(prefixLength = 1)
    private String email;
    // getters and setters
}
```

### Controller 定义

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping("/register")
    public UserResponse register(@RequestBody UserRequest request) {
        // 业务处理...
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername(request.getUsername());
        response.setPhone("13800138000");
        response.setEmail("user@example.com");
        return response;
    }
    
    @GetMapping("/{id}")
    @NoEncrypt
    public UserResponse getUser(@PathVariable Long id) {
        // 不加密的接口
        return new UserResponse();
    }
}
```

## 注意事项

1. **密钥安全**：应将 RSA 密钥存储在安全的地方，不要硬编码在代码中
2. **性能影响**：加密/解密会增加处理时间和 CPU 消耗，应合理使用
3. **兼容性**：该模块仅支持 Spring MVC 环境，Spring WebFlux 暂不支持
4. **脱敏降级**：当字符串长度不足以满足 prefixLength + suffixLength 时，返回原值
5. **扩展性**：通过实现 `EncryptionProvider` 接口可以支持任意加密方式
