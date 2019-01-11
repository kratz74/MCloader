/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.kratz.mc.log.LogLevel;
import org.kratz.mc.log.Logger;

/**
 * Password manipulation utilities.
 */
public class PasswordUtils {

    /** Characters are always encoded and decoded as UTF-8. */
    private static final String ENCODING = "UTF-8";

    /** UTF-8 {@link Charset} instance. */
    private static final Charset CH_UTF8 = Charset.forName(ENCODING);

    // TODO: Better key handling!
    /** Encryption key 256 bits. */ //     12345678901234567890123456789012
//    private static final byte[] ENC_KEY = "kR4t_6jEx93m_OwTicHla6llw9Shlc3D".getBytes(CH_UTF8);
    private static final byte[] ENC_KEY = "kR4t_6jEx93m_OwT".getBytes(CH_UTF8);

    /** Encryption algorithm. */
    private static final String ENC_ALG = "AES";

    // TODO: Real encryption.
    /**
     * Encrypt password character array.
     * @param password Password character array to be encrypted.
     * @return Encrypted password encoded as {@link Base64} {@link String}.
     */
    public static String encrypt(final char[] password) {
        final CharBuffer cb = CharBuffer.wrap(password);
        final ByteBuffer bb = CH_UTF8.encode(cb);
        final byte[] bytes = Arrays.copyOfRange(bb.array(), bb.position(), bb.limit());
        String encodedPw;
        try {
            final Cipher c = Cipher.getInstance(ENC_ALG);
            final SecretKeySpec keySpec = new SecretKeySpec(ENC_KEY, ENC_ALG);
            c.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encBytes = c.doFinal(bytes);
            encodedPw = Base64.getEncoder().encodeToString(encBytes);
            Arrays.fill(encBytes, (byte)0);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            Logger.log(LogLevel.WARNING, "Could not initialize encryption for %s: %s", ENC_ALG, ex.getLocalizedMessage());
            encodedPw = Base64.getEncoder().encodeToString(bytes);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            Logger.log(LogLevel.WARNING, "Could not encrypt password using %s: %s", ENC_ALG, ex.getLocalizedMessage());
            encodedPw = Base64.getEncoder().encodeToString(bytes);
        }
        Arrays.fill(bb.array(), (byte)0);
        Arrays.fill(bytes, (byte)0);
        return encodedPw;
    }

    // TODO: Real encryption.
    /**
     * Encrypt password character array.
     * @param password Password character array to be encrypted.
     * @return Encrypted password encoded as {@link Base64} {@link String}.
     */
    public static char[] decrypt(final String password) {
        if (password == null) {
            return null;
        }
        final byte[] encBytes = Base64.getDecoder().decode(password);
        byte[] bytes;
        try {
            final Cipher c = Cipher.getInstance(ENC_ALG);
            final SecretKeySpec keySpec = new SecretKeySpec(ENC_KEY, ENC_ALG);
            c.init(Cipher.DECRYPT_MODE, keySpec);
            bytes = c.doFinal(encBytes);
            Arrays.fill(encBytes, (byte)0);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            Logger.log(LogLevel.WARNING, "Could not initialize decryption for %s: %s", ENC_ALG, ex.getLocalizedMessage());
            bytes = encBytes;
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            Logger.log(LogLevel.WARNING, "Could not decrypt password using %s: %s", ENC_ALG, ex.getLocalizedMessage());
            bytes = encBytes;
        }
        final ByteBuffer bb = ByteBuffer.wrap(bytes);
        final CharBuffer cb = CH_UTF8.decode(bb);
        final char[] pwBytes = Arrays.copyOfRange(cb.array(), cb.position(), cb.limit());
        Arrays.fill(bytes, (byte)0);
        Arrays.fill(cb.array(), '\u0000');
        return pwBytes;
    }

    
}
