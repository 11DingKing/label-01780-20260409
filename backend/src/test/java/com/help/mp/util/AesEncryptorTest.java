package com.help.mp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptorTest {

    private AesEncryptor encryptor;
    private static final String TEST_KEY = "test-aes-key-32bytes-for-testing!!";

    @BeforeEach
    void setUp() {
        encryptor = new AesEncryptor();
        encryptor.setKey(TEST_KEY);
    }

    @Test
    void encrypt_thenDecrypt_returnsOriginal() {
        String plaintext = "Hello, World!";
        String ciphertext = encryptor.encrypt(plaintext);
        String decrypted = encryptor.decrypt(ciphertext);

        assertNotNull(ciphertext);
        assertNotEquals(plaintext, ciphertext);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_null_returnsNull() {
        assertNull(encryptor.encrypt(null));
    }

    @Test
    void encrypt_emptyString_returnsEmpty() {
        assertEquals("", encryptor.encrypt(""));
    }

    @Test
    void decrypt_null_returnsNull() {
        assertNull(encryptor.decrypt(null));
    }

    @Test
    void decrypt_emptyString_returnsEmpty() {
        assertEquals("", encryptor.decrypt(""));
    }

    @Test
    void encrypt_shortString_works() {
        String plaintext = "A";
        String ciphertext = encryptor.encrypt(plaintext);
        String decrypted = encryptor.decrypt(ciphertext);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_longString_works() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("测试数据TestData");
        }
        String plaintext = sb.toString();
        String ciphertext = encryptor.encrypt(plaintext);
        String decrypted = encryptor.decrypt(ciphertext);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_chineseCharacters_works() {
        String plaintext = "紧急求助小程序";
        String ciphertext = encryptor.encrypt(plaintext);
        String decrypted = encryptor.decrypt(ciphertext);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_specialCharacters_works() {
        String plaintext = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        String ciphertext = encryptor.encrypt(plaintext);
        String decrypted = encryptor.decrypt(ciphertext);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_emoji_works() {
        String plaintext = "Hello 🌍🌎🌏 World 🚀";
        String ciphertext = encryptor.encrypt(plaintext);
        String decrypted = encryptor.decrypt(ciphertext);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_multipleTimes_differentCiphertext() {
        String plaintext = "same text";
        String ciphertext1 = encryptor.encrypt(plaintext);
        String ciphertext2 = encryptor.encrypt(plaintext);

        assertNotEquals(ciphertext1, ciphertext2);
        assertEquals(plaintext, encryptor.decrypt(ciphertext1));
        assertEquals(plaintext, encryptor.decrypt(ciphertext2));
    }

    @Test
    void decrypt_nonBase64_returnsOriginal() {
        String nonBase64 = "not-a-base64-string!@#$";
        assertEquals(nonBase64, encryptor.decrypt(nonBase64));
    }

    @Test
    void decrypt_shortBase64_returnsOriginal() {
        String shortBase64 = Base64.getEncoder().encodeToString("ab".getBytes());
        assertEquals(shortBase64, encryptor.decrypt(shortBase64));
    }

    @Test
    void decrypt_wrongKey_returnsCiphertext() {
        String plaintext = "secret data";
        String ciphertext = encryptor.encrypt(plaintext);

        AesEncryptor wrongKeyEncryptor = new AesEncryptor();
        wrongKeyEncryptor.setKey("wrong-key-32bytes-for-testing!!!");

        String result = wrongKeyEncryptor.decrypt(ciphertext);
        assertEquals(ciphertext, result);
    }

    @Test
    void decrypt_tamperedCiphertext_returnsOriginal() {
        String plaintext = "original data";
        String ciphertext = encryptor.encrypt(plaintext);

        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        decoded[decoded.length - 1] ^= 0xFF;
        String tampered = Base64.getEncoder().encodeToString(decoded);

        String result = encryptor.decrypt(tampered);
        assertEquals(tampered, result);
    }

    @Test
    void setKey_shortKey_paddedTo32Bytes() {
        AesEncryptor shortKeyEncryptor = new AesEncryptor();
        shortKeyEncryptor.setKey("short");

        String plaintext = "test";
        String ciphertext = shortKeyEncryptor.encrypt(plaintext);
        String decrypted = shortKeyEncryptor.decrypt(ciphertext);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void setKey_longKey_truncatedTo32Bytes() {
        AesEncryptor longKeyEncryptor = new AesEncryptor();
        longKeyEncryptor.setKey("this-is-a-very-long-key-that-exceeds-32-bytes-for-sure");

        String plaintext = "test";
        String ciphertext = longKeyEncryptor.encrypt(plaintext);
        String decrypted = longKeyEncryptor.decrypt(ciphertext);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_phoneNumber_typicalUseCase() {
        String phone = "13812345678";
        String ciphertext = encryptor.encrypt(phone);
        String decrypted = encryptor.decrypt(ciphertext);

        assertEquals(phone, decrypted);
        assertNotEquals(phone, ciphertext);
    }

    @Test
    void encrypt_contactName_typicalUseCase() {
        String name = "张三";
        String ciphertext = encryptor.encrypt(name);
        String decrypted = encryptor.decrypt(ciphertext);

        assertEquals(name, decrypted);
    }

    @Test
    void decrypt_legacyUnencryptedData_returnsAsIs() {
        String legacyData = "old_unencrypted_data";
        assertEquals(legacyData, encryptor.decrypt(legacyData));
    }

    @Test
    void decrypt_legacyShortBase64_returnsAsIs() {
        String shortData = "abc";
        String base64Short = Base64.getEncoder().encodeToString(shortData.getBytes());
        assertEquals(base64Short, encryptor.decrypt(base64Short));
    }

    @Test
    void encrypt_whitespaceOnly_works() {
        String plaintext = "   \t\n\r   ";
        String ciphertext = encryptor.encrypt(plaintext);
        String decrypted = encryptor.decrypt(ciphertext);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_unicodeSupplementary_works() {
        String plaintext = "𝄞𝄢𝄫𝄪𝄩";
        String ciphertext = encryptor.encrypt(plaintext);
        String decrypted = encryptor.decrypt(ciphertext);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_withoutKey_throwsException() {
        AesEncryptor noKeyEncryptor = new AesEncryptor();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            noKeyEncryptor.encrypt("test");
        });

        assertEquals("加密失败", exception.getMessage());
    }
}
