/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.log;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

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

    /**
     * Initialize UI logging. This causes transfer of log output from system output to UI.
     * @param document UI logger document instance.
     */
    public static void initUi(final Document document) {
        INSTANCE.initUI(document);
    }

    /**
     * Close UI logging. This causes transfer of log output from UI back to system output.
     */
    public static void closeUi() {
        INSTANCE.closeUI();
    }

    /** Current logging level. */
    private LogLevel level;

    /** Indentation size (default 2). */
    private int indentSize;

    /** Log lines buffer used to store log lines before UI components are initialized. */
    private LinkedList<String> linesBuffer;

    /** UI logging component used to store log lines after UI components are initialized.. **/
    private Document uiBuffer;

    /**
     * Creates an instance of logger.
     */
    private Logger() {
        level = LogLevel.FINEST;
        indentSize = 2;
        linesBuffer = new LinkedList<>();
        uiBuffer = null;
    }

    /**
     * Initialize UI logging. This causes transfer of log output from system output to UI.
     * @param document UI logger document instance.
     */
    public synchronized void initUI(final Document document) {
        linesBuffer.forEach((line) -> {
            try {
                document.insertString(document.getLength(), line, null);
                document.insertString(document.getLength(), "\n", null);
            } catch (BadLocationException e) {
                LogEntry lineEntry = new LogEntry("Could not add log line to UI buffer: ", 0, new String[] {e.getLocalizedMessage()});
                System.out.println(lineEntry.format());
            }
        });
        uiBuffer = document;
        linesBuffer = null;
    }

    public synchronized void closeUI() {
        uiBuffer = null;
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
     *
     * @param entry Logging entry to be logged.
     */
    private synchronized void log(final LogEntry entry) {
        final String line = entry.format();
        if (uiBuffer == null) {
            if (linesBuffer != null) {
                linesBuffer.add(line);
            }
            System.out.println(entry.format());
        } else {
            try {
                uiBuffer.insertString(uiBuffer.getLength(), line, null);
                uiBuffer.insertString(uiBuffer.getLength(), "\n", null);
            } catch (BadLocationException e) {
                LogEntry lineEntry = new LogEntry("Could not add log line to UI buffer: ", 0, new String[]{e.getLocalizedMessage()});
                System.out.println(lineEntry.format());
            }
        }
    }

}
