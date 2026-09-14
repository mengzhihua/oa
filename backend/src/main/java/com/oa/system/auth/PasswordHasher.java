package com.oa.system.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.Base64;

public final class PasswordHasher {
    private static final int ITERATIONS = 65536;
    private PasswordHasher() {}
    public static String hash(String plain) {
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(derive(plain, salt, ITERATIONS));
    }
    public static boolean verify(String plain, String stored) {
        try { String[] p = stored.split("\\$"); if (p.length != 4) return false; return MessageDigest.isEqual(Base64.getDecoder().decode(p[3]), derive(plain, Base64.getDecoder().decode(p[2]), Integer.parseInt(p[1]))); } catch (RuntimeException e) { return false; }
    }
    private static byte[] derive(String plain, byte[] salt, int it) {
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(plain.toCharArray(), salt, it, 256)).getEncoded(); } catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
    }
}
