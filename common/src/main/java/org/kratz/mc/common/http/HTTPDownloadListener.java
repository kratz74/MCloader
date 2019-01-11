/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.common.http;

/**
 * Download progress events listener.
 */
public interface HTTPDownloadListener {

    /**
     * Mark downloading as started.
     */
    public void begin();

    /**
     * Mark downloading as ended.
     * @param result HTTP download: Value of {@code true} if download was finished successfully
     *               or {@code false} otherwise.
     */
    public void end(final boolean result);

    /**
     * Set current progress.
     * @param progress Progress value in % (0-100).
     */
    public void progress(final int progress);

}
