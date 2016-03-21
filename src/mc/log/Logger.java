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

    /** Current logging level. */
    private LogLevel level;

    /** Indentation size (default 2). */
    private int indentSize;

    /**
     * Get logger instance.
     * @return Logger instance.
     */
    public static Logger getInstance() {
        return INSTANCE;
    }

    /**
     * Creates an instance of logger.
     */
    private Logger() {
        level = LogLevel.ALL;
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
     * Log message with given logging level and no arguments.
     * @param level   Logging level of the message.
     * @param message Message to be logged.
     */
    public void log(final LogLevel level, final String message) {
        if (this.level.shouldLog(level)) {
            log(new LogEntry(message, 0, (Object[])null));
        }
    }

    /**
     * Log message with given logging level and arguments.
     * @param level   Logging level of the message.
     * @param message Message to be logged.
     * @param args    Message arguments.
     */
    public void log(final LogLevel level, final String message, final Object... args) {
        if (this.level.shouldLog(level)) {
            log(new LogEntry(message, 0, args));
        }
    }

    /**
     * Log message with given logging level and arguments.
     * @param level   Logging level of the message.
     * @param message Message to be logged.
     * @param ex      Message exception.
     */
    public void log(final LogLevel level, final String message, final Exception ex) {
        if (this.level.shouldLog(level)) {
            log(new LogEntry(message, 0, ex));
        }
    }

    /**
     * Log message with given logging level, indentation and no arguments.
     * @param level   Logging level of the message.
     * @param indent  Message indentation.
     * @param message Message to be logged.
     */
    public void log(final LogLevel level, final int indent, final String message) {
        if (this.level.shouldLog(level)) {
            log(new LogEntry(message, indent * indentSize, (Object[])null));
        }
    }

    /**
     * Log message with given logging level, indentation and arguments.
     * @param level   Logging level of the message.
     * @param indent  Message indentation.
     * @param message Message to be logged.
     * @param args    Message arguments.
     */
    public void log(final LogLevel level, final int indent, final String message, final Object... args) {
        if (this.level.shouldLog(level)) {
            log(new LogEntry(message, indent * indentSize, args));
        }
    }

    /**
     * Log message with given logging level, indentation and arguments.
     * @param level   Logging level of the message.
     * @param indent  Message indentation.
     * @param message Message to be logged.
     * @param ex      Message exception.
     */
    public void log(final LogLevel level, final int indent, final String message, final Exception ex) {
        if (this.level.shouldLog(level)) {
            log(new LogEntry(message, indent * indentSize, ex));
        }
    }

    /**
     * Log message stored in logging entry.
     * @param entry Logging entry to be logged.
     */
    private void log(final LogEntry entry) {
        System.out.println(entry.format());
    }

}
