package com.dev.notebook;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {
    public static String generateSecretKey(int length) {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[length];
        random.nextBytes(keyBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
    }

    public static void main(String[] args) {
        String secretKey = generateSecretKey(32);
        System.out.println("Generated Secret Key: " + secretKey);
    }
}
