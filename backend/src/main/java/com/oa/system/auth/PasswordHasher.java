package com.oa.system.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {
    private static final int ITERATIONS = 65536;
    private static final int KEY_BITS = 256;

    private PasswordHasher() {
    }

    public static String hash(String plain) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] value = derive(plain, salt, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(value);
    }

    public static boolean verify(String plain, String stored) {
        try {
            if (plain == null || stored == null) {
                return false;
            }
            String[] parts = stored.split("\\$");
            if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(plain, salt, Integer.parseInt(parts[1]));
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static byte[] derive(String plain, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(plain.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("密码处理失败", exception);
        }
    }
}
