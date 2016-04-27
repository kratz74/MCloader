/*
 * (C) 2016 Tomas Kraus
 */
package mc.utils;

import mc.log.Logger;
import mc.log.LogLevel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test password encryption and decryption.
 */
public class PasswordUtilsTest {

    /** Passwords to test. */
    private static final char[][] PASSWORDS = {
        {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'},
        {' ', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd'},
        {'p', 'a', 's', 's', 'w', 'o', 'r', 'd', ' '},
        {' ', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', ' '}
    };

    /**
     * Test of encrypt method, of class PasswordUtils.
     */
    @Test
    public void testEncrypt() {
        Logger.log(LogLevel.INFO, "Running test: testEncrypt");
        for (char[] srcPw : PASSWORDS) {
            final String encPw = PasswordUtils.encrypt(srcPw);
            final char[] dstPw = PasswordUtils.decrypt(encPw);
            boolean match = srcPw.length == dstPw.length;
            if (match) {
                for (int i = 0; i < srcPw.length ; i++) {
                    if (Character.compare(srcPw[i], dstPw[i]) != 0) {
                        match = false;
                    }
                }
            }
            assertTrue(String.format("Passwords \"%s\" and \"%s\" do not match", new String(srcPw), new String(dstPw)), match);
        }
    }

}
