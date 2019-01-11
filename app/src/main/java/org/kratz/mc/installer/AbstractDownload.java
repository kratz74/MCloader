/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.installer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.LinkedList;
import org.kratz.mc.common.http.HttpDownload;
import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;
import org.kratz.mc.ui.loader.DownloadListener;

/**
 * Abstract game component downloading class.
 */
public abstract class AbstractDownload implements Downloader, Runnable {

    /** Whether some downloading thread is already running. */
    private static boolean running = false;

    /** Waiting threads queue. */
    private static final LinkedList<AbstractDownload> threadQueue = new LinkedList<>();

    /**
     * Create directory structure for provided path.
     * @param dir Directory structure to be created.
     * @return Value of {@code true} when directory exists or was successfully created or {@code false} otherwise.
     */
    protected static boolean mkDir(final File dir) {
        if (!dir.exists()) {
            final boolean dirCreated;
            if (dirCreated = dir.mkdirs()) {
                Logger.log(LogLevel.FINE, 1, "Created %s", dir.getAbsolutePath());
            } else {
                Logger.log(LogLevel.WARNING, 0, "Could not create %s", dir.getAbsolutePath());
            }
            return dirCreated;
        } else {
            return dir.isDirectory();
        }
    }

    /**
     * Create parent directory structure for provided {@link File}.
     * @param file File which parent directory should be created.
     * @return Value of {@code true} when directory exists or was successfully created or {@code false} otherwise.
     */
    protected static boolean mkParentDir(final File file) {
        final File dir = file.getParentFile();
        if (dir == null) {
            Logger.log(LogLevel.WARNING, 0, "Could not create parent directory for %s", file.getAbsolutePath());
            return false;
        }
        return mkDir(dir);
    }

    /**
     * Download {@code source} {@link URL} and store it as {@code target} {@link File}.
     * @param source   Source URL.
     * @param target   Target file.
     * @param progress Download progress event listener.
     * @param proxy    HTTP proxy configuration.
     * @return Value of {@code true} if transfer was finished successfully or {@code false} otherwise.
     */
    protected static boolean transfer(final URL source, final File target, final DownloadListener progress, final Proxy proxy) {
        final HttpDownload download = new HttpDownload(source, target, new HTTPProgressOnlyListener(progress), proxy);
        progress.name(target.getName());
        return download.transfer();
    }

    /** Installation path. */
    protected final String path;

    /** Download change listener. */
    protected final DownloadListener progress;

    /** Thread internal execution control. */
    protected boolean execute;

    /** External thread execution notification. */
    protected boolean isRunning;

    /** Downloading thread instance. */
    protected Thread thread;

    /** HTTP proxy configuration. */
    protected final Proxy proxy;

    /**
     * Creates an instance of game component downloading class.
     * @param path     Game installation path.
     * @param progress Download change listener
     * @param proxy    HTTP proxy configuration.
     */
    protected AbstractDownload(final String path, final DownloadListener progress, final Proxy proxy) {
        this.path = path;
        this.progress = progress;
        this.execute = false;
        this.isRunning = false;
        this.thread = null;
        this.proxy = proxy;
    }

    /**
     * Get thread name of this downloading thread.
     * @return thread name of this downloading thread.
     */
    protected abstract String threadName();

    /**
     * Thread main method.
     * @return Value of {@code true} if thread execution was finished successfully or {@code false} otherwise.
     */
    protected abstract boolean thread();

    /**
     * Downloading thread main method.
     */
    @Override
    public void run() {
        progress.begin();
        final boolean result = thread();
        progress.end(result);
        this.isRunning = false;
        synchronized(AbstractDownload.class) {
            if (threadQueue.isEmpty()) {
                running = false;
            } else {
                Logger.log(LogLevel.FINE, "Starting delayed thread %s", this.threadName());
                AbstractDownload toStart = threadQueue.removeFirst();
                toStart.thread.start();
            }
        }
        Logger.log(LogLevel.FINE, "Finishing thread %s", this.threadName());
    }

    /** 
     * Execute download in parallel thread.
     */
    @Override
    public void start() {
        this.execute = true;
        this.isRunning = true;
        this.thread = new Thread(this, threadName());
        synchronized(AbstractDownload.class) {
            if (threadQueue.isEmpty() && running == false) {
                Logger.log(LogLevel.FINE, "Starting thread %s", this.threadName());
                this.thread.start();
                running = true;
            } else {
                threadQueue.addLast(this);
            }
        }
        
    }

    /**
     * Get components download handler parallel thread status.
     * @return Value of {@code true} when parallel downloading thread is running or {@code false} otherwise.
     */
    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Convert {@link String} to {@link URL}.
     * @param url {@link URL} source {@link String}.
     * @return {@link URL} built from source {@link String} or {@code null} if source {@link String}
     *         could not be converted to {@link URL}.
     */
    protected URL toURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            Logger.log(LogLevel.WARNING, "Invalid URL: %s", url);
            return null;
        }
    }

}
