/*
 * (C) 2016 Tomas Kraus
 */
package mc.installer;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import mc.log.LogLevel;
import mc.log.Logger;
import mc.ui.loader.DownloadListener;

/**
 * Abstract game component downloading class.
 */
public abstract class AbstractDownload implements Downloader, Runnable {

    /** Temporary extension for file being downloaded. */    
    public static final String TMP_EXT = ".part";

    /**
     * Get remote file size.
     * @param source Source URL.
     * @return Size of remote content length or {@code -1} if content length could not be got.
     */
    public static long getContentLength(final URL source) {
        long size = -1;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)source.openConnection();
            conn.setRequestMethod("HEAD");
            conn.getInputStream();
            size = conn.getContentLength();
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Could not fetch remote content length: %s", ex.getLocalizedMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return size;
    }

    /**
     * Close provided {@link Closeable}.
     * @param c {@link Closeable} to close.
     */
    public static void close(final Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "Could not close socket: %s", ex.getLocalizedMessage());
            }
        }
    }

    /** Game installation path. */
    protected final String path;

    /** Download change listener. */
    protected final DownloadListener progress;

    /** Thread internal execution control. */
    protected boolean execute;

    /** External thread execution notification. */
    protected boolean isRunning;

    /** Downloading thread instance. */
    protected Thread thread;

   /**
    * Creates an instance of game component downloading class.
     * @param path     Game installation path.
     * @param progress Download change listener
    */
    protected AbstractDownload(final String path, final DownloadListener progress) {
        this.path = path;
        this.progress = progress;
        this.execute = false;
        this.isRunning = false;
        this.thread = null;
    }

    /**
     * Get thread name of this downloading thread.
     * @return thread name of this downloading thread.
     */
    protected abstract String threadName();

    /**
     * Thread main method.
     */
    protected abstract void thread();

    /**
     * Downloading thread main method.
     */
    @Override
    public void run() {
        progress.begin();
        thread();
        progress.end();
        this.isRunning = false;
    }

    /** 
     * Execute download in parallel thread.
     */
    @Override
    public void start() {
        this.execute = true;
        this.isRunning = true;
        this.thread = new Thread(this, threadName());
        this.thread.start();
    }

    /**
     * Get components download handler parallel thread status.
     * @return Value of {@code true} when parallel downloading thread is running or {@code false} otherwise.
     */
    @Override
    public boolean isRunning() {
        return isRunning;
    }

}
