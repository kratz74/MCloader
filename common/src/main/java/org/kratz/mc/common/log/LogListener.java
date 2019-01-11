/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.common.log;

/**
 * Logger event listener.
 */
public interface LogListener {

    /**
     * Send log line event.
     * @param line Log line (formatted).
     */
    void logLine(final String line);

}
