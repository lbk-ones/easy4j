package io.github.lbkones.encryption.util;

import java.security.*;
import java.util.Base64;

/**
 * RSA 密钥生成工具
 * 用于生成 RSA 密钥对，前端使用公钥加密，后端使用私钥解密和加密
 */
public class RsaKeyGenerator {

    private static final int KEY_SIZE = 1024;

    /**
     * 生成 RSA 密钥对
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        return generateKeyPair(KEY_SIZE);
    }

    /**
     * 生成指定大小的 RSA 密钥对
     */
    public static KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 获取 Base64 编码的公钥（前端使用）
     */
    public static String getPublicKeyBase64(KeyPair keyPair) {
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }

    /**
     * 获取 Base64 编码的私钥（后端使用）
     */
    public static String getPrivateKeyBase64(KeyPair keyPair) {
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        return Base64.getEncoder().encodeToString(privateKeyBytes);
    }

    /**
     * 生成并打印 RSA 密钥对（用于初始化）
     */
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPair keyPair = generateKeyPair(1024);
        String publicKey = getPublicKeyBase64(keyPair);
        String privateKey = getPrivateKeyBase64(keyPair);

        System.out.println("============ RSA Key Pair ============");
        System.out.println("Public Key (Base64 - 前端使用):");
        System.out.println(publicKey);
        System.out.println("\nPrivate Key (Base64 - 后端使用):");
        System.out.println(privateKey);
        System.out.println("====================================");

        // 用于 application.yml 配置
        System.out.println("\n# For application.yml (backend server)");
        System.out.println("easy4j:");
        System.out.println("  encryption:");
        System.out.println("    enabled: true");
        System.out.println("    encryption-type: rsa");
        System.out.println("    private-key: " + privateKey);
    }
}

