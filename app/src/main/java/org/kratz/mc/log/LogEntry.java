/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.log;

import java.util.Formatter;

/**
 * Logging message with all required attributes.
 */
public class LogEntry {

    /** Message string. */
    private final String message;

    /** Exception to be logged. */
    private final Exception ex;

    /** Message indentation (number of spaces). */
    private final int indent;

    /** Message arguments array. */
    private final Object[] args;

    /**
     * Creates an instance of logging message with arguments.
     * @param message    Message string.
     * @param indent     Message indentation.
     * @param args       Message arguments.
     */
    public LogEntry(final String message, final int indent, final Object[] args) {
        this.message = message;
        this.indent = indent;
        this.args = args;
        this.ex = null;
    }

    /**
     * Creates an instance of logging message with exception.
     * @param message Message string.
     * @param indent  Message indentation.
     * @param ex      Message {@link Exception}.
     */
    public LogEntry(final String message, final int indent, final Exception ex) {
        this.message = message;
        this.indent = indent;
        this.args = null;
        this.ex = ex;
    }

    /**
     * Format message for writing to logger output.
     * @return Message formated for writing.
     */
    public String format() {
        final StringBuilder sb = new StringBuilder();
        final Formatter f = new Formatter(sb);
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        if (args != null) {
            f.format(message, args);
        } else if (ex != null) {
            f.format(message, ex.getLocalizedMessage());
        } else {
            sb.append(message);
        }
        return sb.toString();
    }
}
