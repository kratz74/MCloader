/*
 * (C) 2016 Tomas Kraus
 */
package mc.log;

import java.util.HashMap;

/**
 * Logging level.
 */
public enum LogLevel {
    
    /** Log all messages. */
    ALL(    (byte)0x0, "ALL"),
    /** Fine level. */
    FINEST( (byte)0x1, "FINEST"),
    /** Fine level. */
    FINE(   (byte)0x2, "FINE"),
    /** Informational level. */
    INFO(   (byte)0x3, "INFO"),
    /** Errors. */
    WARNING((byte)0x4, "WARN"),
    /** Fatal errors. */
    FATAL(  (byte)0x5, "CRIT"),
    /** Logging no messages. */
    OFF(    (byte)0x6, "OFF");

    /** Logging levels enumeration length. */
    public static final int length = LogLevel.values().length;

    /** {@link HashMap} for {@link String} to {@link LogLevel} case insensitive lookup. */
    private static final HashMap<String, LogLevel> valuesMap = new HashMap<>(2 * length);

    // Initialize String to LogLevel case insensitive lookup Map.
    static {
        for (LogLevel logLevel : LogLevel.values()) {
            valuesMap.put(logLevel.name.toUpperCase(), logLevel);
        }
    }

    /**
     * Returns {@link LogLevel} object corresponding to the value of the specified {@link String}.
     * @param name The {@link String} to be checked.
     * @return {@link LogLevel} object corresponding to the value of the string argument or {@code null} when
     *         there exists no corresponding {@link LogLevel} object to provided {@link String}.
     */
    public static final LogLevel toValue(final String name) {
        return name != null ? valuesMap.get(name.toUpperCase()) : null;
    }

    /** Level ID. Continuous sequence starting from 0. */
    private final byte id;

    /** Level name. */
    private final String name;

    /**
     * Creates an instance of logging level.
     * @param id   Level ID.
     * @param name Level name.
     */
    private LogLevel(final byte id, final String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Get level ID.
     * @return Level ID.
     */
    public byte getId() {
        return id;
    }

    /**
     * Get level name.
     * @return Level name.
     */
    public String getName() {
        return name;
    }

    /**
     * Check if the given level message would be logged under this level.
     * @param level Level to check.
     * @return Value of {@code true} if the given level will be logged or {@code false} otherwise.
     */
    public boolean shouldLog(final LogLevel level) {
        return this.id <= level.id;
    }

}
