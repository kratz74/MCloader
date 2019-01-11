/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.loader;

import java.util.Properties;
import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Loader properties.
 */
public class Loader {

    /** Version property. */
    public static final String VERSION = "version";
    
    /** Messages file name prefix. */
    private static final String FILE_PATH = "/config/loader.properties";

    /** Messages properties for current locale. */
    private static final Properties PROPERTIES = loadMessages(FILE_PATH);

    /**
     * Load messages properties.
     */
    private static Properties loadMessages(final String file) {
        try (InputStream is = Loader.class.getResourceAsStream(file)) {
            if (is == null) {
                return null;
            }
            try (Reader r = new InputStreamReader(is)) {
                Properties p = new Properties();
                p.load(r);
                return p;
            }
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Get loader property for provided key.
     * @param key Property key.
     * @return Property value for provided key.
     */
    public static final String getProperty(final String key) {
        return PROPERTIES.getProperty(key);
    }

    /**
     * Get current Loader version {@code String}.
     * @return Current Loader version {@code String}.
     */
    public static final String getVersion() {
        return PROPERTIES.getProperty(VERSION);
    }

}
