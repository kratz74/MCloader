/*
 * (C) 2016 Tomas Kraus
 */
package mc.log;

/**
 * Logger.
 * Implemented as singleton.
 */
public class Logger {

    /** Logger singleton instance. */
    private static final Logger INSTANCE = new Logger();

    /**
     * Get logger instance.
     * @return Logger instance.
     */
    public static Logger getInstance() {
        return INSTANCE;
    }

    /**
     * Check if the given level message would be currently logged.
     * @param level Level to check.
     * @return Value of {@code true} if the given level will be logged or {@code false} otherwise.
     */
    public static boolean shouldLog(final LogLevel level) {
        return INSTANCE.level.shouldLog(level);
    }
   
    /**
     * Log message with given logging level and no arguments.
     * @param level   Logging level of the message.
     * @param message Message to be logged.
     */
    public static void log(final LogLevel level, final String message) {
        if (INSTANCE.level.shouldLog(level)) {
            INSTANCE.log(new LogEntry(message, 0, (Object[])null));
        }
    }

    /**
     * Log message with given logging level and arguments.
     * @param level   Logging level of the message.
     * @param message Message to be logged.
     * @param args    Message arguments.
     */
    public static void log(final LogLevel level, final String message, final Object... args) {
        if (INSTANCE.level.shouldLog(level)) {
            INSTANCE.log(new LogEntry(message, 0, args));
        }
    }

    /**
     * Log message with given logging level and arguments.
     * @param level   Logging level of the message.
     * @param message Message to be logged.
     * @param ex      Message exception.
     */
    public static void log(final LogLevel level, final String message, final Exception ex) {
        if (INSTANCE.level.shouldLog(level)) {
            INSTANCE.log(new LogEntry(message, 0, ex));
        }
    }

    /**
     * Log message with given logging level, indentation and no arguments.
     * @param level   Logging level of the message.
     * @param indent  Message indentation.
     * @param message Message to be logged.
     */
    public static void log(final LogLevel level, final int indent, final String message) {
        if (INSTANCE.level.shouldLog(level)) {
            INSTANCE.log(new LogEntry(message, indent * INSTANCE.indentSize, (Object[])null));
        }
    }

    /**
     * Log message with given logging level, indentation and arguments.
     * @param level   Logging level of the message.
     * @param indent  Message indentation.
     * @param message Message to be logged.
     * @param args    Message arguments.
     */
    public static void log(final LogLevel level, final int indent, final String message, final Object... args) {
        if (INSTANCE.level.shouldLog(level)) {
            INSTANCE.log(new LogEntry(message, indent * INSTANCE.indentSize, args));
        }
    }

    /**
     * Log message with given logging level, indentation and arguments.
     * @param level   Logging level of the message.
     * @param indent  Message indentation.
     * @param message Message to be logged.
     * @param ex      Message exception.
     */
    public static void log(final LogLevel level, final int indent, final String message, final Exception ex) {
        if (INSTANCE.level.shouldLog(level)) {
            INSTANCE.log(new LogEntry(message, indent * INSTANCE.indentSize, ex));
        }
    }

    /** Current logging level. */
    private LogLevel level;

    /** Indentation size (default 2). */
    private int indentSize;

     /**
     * Creates an instance of logger.
     */
    private Logger() {
        level = LogLevel.FINE;
        indentSize = 2;
    }

    /**
     * Set logging level to a new value.
     * @param level New value of logging level.
     */
    public void setLevel(final LogLevel level) {
        this.level = level;
    }

    /**
     * Set indentation size.
     * @param size New indentation size to set.
     */
    public void setIndent(final int size) {
        indentSize = size;
    }

    /**
     * Log message stored in logging entry.
     * @param entry Logging entry to be logged.
     */
    private void log(final LogEntry entry) {
        System.out.println(entry.format());
    }

}
