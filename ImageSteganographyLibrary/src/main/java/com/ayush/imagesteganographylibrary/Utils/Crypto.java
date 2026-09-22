package com.ayush.imagesteganographylibrary.Utils;

/**
 * Kept for source compatibility - the implementation lives in the platform independent
 * {@link com.ayush.imagesteganographylibrary.core.Crypto} so that images encoded outside Android
 * decode inside it.
 */
public class Crypto {

    public static String encryptMessage(String message, String secret_key) throws Exception {
        return com.ayush.imagesteganographylibrary.core.Crypto.encryptMessage(message, secret_key);
    }

    public static String decryptMessage(String encrypted_message, String secret_key) throws Exception {
        return com.ayush.imagesteganographylibrary.core.Crypto.decryptMessage(encrypted_message, secret_key);
    }
}
