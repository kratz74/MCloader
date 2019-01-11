/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.locale;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import java.util.Properties;

import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;

/**
 * UI messages.
 */
public class Messages {

    /** Messages file name prefix. */
    private static final String FILE_NAME_PREFIX = "messages";

    /** Messages file name suffix. */
    private static final String FILE_NAME_SUFFIX = ".properties";

    /** Messages file name language code separator. */
    private static final String FILE_NAME_SEP = "_";

    /** Default language code. */
    private static final String DEF_CODE = Locale.ENGLISH.getLanguage();

    /** Current language code. */
    //private static final String LOC_CODE = Locale.getDefault() != null ? Locale.getDefault().getLanguage() : null;
    private static final String LOC_CODE = "cz";

    /** Package with messages properties files. */
    private static final String PACKAGE = "/locale";

    /** Package path separator. */
    private static final String PACKAGE_PATH_SEP = "/";

    /** Messages properties for current locale. */
    private static final Properties PROPERTIES = loadMessages();

    /**
     * Get message property for provided key.
     * @param key Message key.
     * @return Message property for provided key.
     */
    public static final String get(final String key) {
        return PROPERTIES.getProperty(key);
    }

    /**
     * Build file name of messages properties for provided locales code.
     * @param locCode Locales code used to build messages properties file name.
     * @return File name of messages properties for provided locales code.
     */
    private static String buildMessagesFileName(final String locCode) {
        final int len = FILE_NAME_PREFIX.length() + FILE_NAME_SEP.length() + FILE_NAME_SUFFIX.length() + locCode.length();
        StringBuilder sb = new StringBuilder(len);
        sb.append(FILE_NAME_PREFIX);
        sb.append(FILE_NAME_SEP);
        sb.append(locCode);
        sb.append(FILE_NAME_SUFFIX);
        return sb.toString();
    }

    /**
     * Build JAR resource path from given file name and package path prefix.
     * @param file Messages properties file name.
     * @return JAR resource path.
     */
    private static String buildJarResourcePath(final String file) {
        final boolean addSep = !PACKAGE.endsWith(PACKAGE_PATH_SEP) && !file.startsWith(PACKAGE_PATH_SEP);
        final int len = PACKAGE_PATH_SEP.length() + file.length() + (addSep ? PACKAGE_PATH_SEP.length() : 0);
        StringBuilder sb = new StringBuilder(len);
        sb.append(PACKAGE);
        if (addSep) {
            sb.append(PACKAGE_PATH_SEP);
        }
        sb.append(file);
        return sb.toString();
    }

    /**
     * Load messages properties from given file.
     * @param file Messages properties JAR resource path.
     * @return Messages properties read from given file
     */
    private static Properties loadMessagesFromFile(final String file) {
        try (InputStream is = Messages.class.getResourceAsStream(file)) {
            if (is == null) {
                return null;
            }
            try (Reader r = new InputStreamReader(is)) {
                Properties p = new Properties();
                p.load(r);
                return p;
            }
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Could not read messages properties from %s resource.", file);
            return null;
        }
    }

    /**
     * Load messages properties.
     */
    private static Properties loadMessages() {
        Properties p;
        if (LOC_CODE != null) {
            p = loadMessagesFromFile(buildJarResourcePath(buildMessagesFileName(LOC_CODE)));
            if (p != null) {
                Logger.log(LogLevel.INFO, "Reading messages properties for %s locale.", LOC_CODE);
                return p;
            }
        }
        Logger.log(LogLevel.INFO, "Reading messages properties for defsult %s locale.", LOC_CODE);
        p = loadMessagesFromFile(buildJarResourcePath(buildMessagesFileName(DEF_CODE)));
        return p != null ? p : new Properties();
    }

}
