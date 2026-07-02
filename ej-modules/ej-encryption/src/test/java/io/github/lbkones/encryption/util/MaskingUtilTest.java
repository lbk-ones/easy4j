package io.github.lbkones.encryption.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字段脱敏工具测试
 */
public class MaskingUtilTest {

    @Test
    public void testMaskString_Normal() {
        String value = "13800138000";
        String result = MaskingUtil.maskString(value, 3, 4, "*");
        assertEquals("138****8000", result);
    }

    @Test
    public void testMaskString_OnlyPrefix() {
        String value = "13800138000";
        String result = MaskingUtil.maskString(value, 3, 0, "*");
        assertEquals("138********", result);
    }

    @Test
    public void testMaskString_OnlySuffix() {
        String value = "13800138000";
        String result = MaskingUtil.maskString(value, 0, 4, "*");
        assertEquals("*******8000", result);
    }

    @Test
    public void testMaskString_NoMask() {
        String value = "13800138000";
        String result = MaskingUtil.maskString(value, 0, 0, "*");
        assertEquals("***********", result);
    }

    @Test
    public void testMaskString_InsufficientLength() {
        String value = "138";
        String result = MaskingUtil.maskString(value, 2, 2, "*");
        // 位数不够，返回原值
        assertEquals("138", result);
    }

    @Test
    public void testMaskString_Empty() {
        String value = "";
        String result = MaskingUtil.maskString(value, 3, 4, "*");
        assertEquals("", result);
    }

    @Test
    public void testMaskString_Null() {
        String result = MaskingUtil.maskString(null, 3, 4, "*");
        assertNull(result);
    }

    @Test
    public void testMaskString_Email() {
        String value = "user@example.com";
        String result = MaskingUtil.maskString(value, 1, 4, "*");
        assertEquals("u***********.com", result);
    }
}
