package com.help.mp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256-GCM 加解密工具。
 * 用于用户手机号、联系人姓名/电话等敏感字段的加密存储。
 * 密文格式: Base64( 12字节IV + ciphertext + 16字节tag )
 */
@Slf4j
@Component
public class AesEncryptor {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecretKeySpec keySpec;

    @Value("${app.encrypt.aes-key:help-mp-aes-key-32bytes-change!!}")
    public void setKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        // 确保 key 为 32 字节 (AES-256)
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        } else if (keyBytes.length > 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 加密明文，返回 Base64 编码的密文
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) return plaintext;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            // IV + encrypted (含 GCM tag)
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            log.error("AES encrypt failed", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密 Base64 编码的密文，返回明文
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) return ciphertext;
        try {
            byte[] decoded = Base64.getDecoder().decode(ciphertext);
            if (decoded.length < GCM_IV_LENGTH) {
                // 非加密数据，原样返回（兼容旧数据）
                return ciphertext;
            }

            byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // 非 Base64 数据，可能是旧的未加密数据
            log.debug("Not a Base64 string, returning as-is: {}", ciphertext.substring(0, Math.min(10, ciphertext.length())));
            return ciphertext;
        } catch (Exception e) {
            log.warn("AES decrypt failed, returning as-is", e);
            return ciphertext;
        }
    }
}
