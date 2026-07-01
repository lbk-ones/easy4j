package io.github.lbkones.encryption.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RSA 加密提供者测试
 */
public class RsaEncryptionProviderTest {

    private RsaEncryptionProvider provider;
    private String testPrivateKey;

    @BeforeEach
    public void setUp() throws Exception {
        KeyPair keyPair = io.github.lbkones.encryption.util.RsaKeyGenerator.generateKeyPair(512);
        testPrivateKey = io.github.lbkones.encryption.util.RsaKeyGenerator.getPrivateKeyBase64(keyPair);
        provider = new RsaEncryptionProvider(testPrivateKey);
    }

    @Test
    public void testGetName() {
        assertEquals("rsa", provider.getName());
    }

    @Test
    public void testEncryptNotNull() {
        String plaintext = "Hello World";
        String encrypted = provider.encrypt(plaintext);
        System.out.println(encrypted);
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);
        assertTrue(encrypted.length() > 0);
    }

    @Test
    public void testEncryptNull() {
        String result = provider.encrypt(null);
        assertNull(result);
    }

    @Test
    public void testDecryptNull() {
        String result = provider.decrypt(null);
        assertNull(result);
    }

    @Test
    public void testEncryptEmpty() {
        String result = provider.encrypt("");
        assertEquals("", result);
    }

    @Test
    public void testDecryptEmpty() {
        String result = provider.decrypt("");
        assertEquals("", result);
    }
}


