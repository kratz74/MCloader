/*
 * (C) 2016 Tomas Kraus
 */
package mc.installer;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import mc.log.LogLevel;
import mc.log.Logger;
import mc.ui.loader.DownloadListener;

/**
 * Abstract game component downloading class.
 */
public abstract class AbstractDownload implements Downloader, Runnable {

    /** Internal buffer size. */
    protected static final int BUFFER_SIZE = 0x7FFF;    

    /** HTTP PROXY configuration. */
    protected static final Proxy PROXY = initProxy();

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

    /**
     * Initialize HTTP PROXY to be used for modules download.
     * @return New {@link Proxy} instance or {@code null} if no PROXY is set.
     */
    private static Proxy initProxy() {
        String proxyStr = System.getenv("http_proxy");
        //String proxyStr = "www-proxy-ukc1.uk.oracle.com:80";
        if (proxyStr == null) {
            proxyStr = System.getenv("HTTP_PROXY");
            
        }
        if (proxyStr != null) {
            try {
                if (!proxyStr.startsWith("http://")) {
                    proxyStr = "http://" + proxyStr;
                }
                final URL url = new URL(proxyStr);
                Logger.log(LogLevel.FINE, "HTTP proxy: %s:%d", url.getHost(), url.getPort());
                return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url.getHost(), url.getPort()));
            } catch (MalformedURLException ex) {
                Logger.log(LogLevel.WARNING, "Invalid URL: %s", proxyStr);
                return Proxy.NO_PROXY;
            }
        }
        Logger.log(LogLevel.FINE, "No HTTP proxy is set");
        return Proxy.NO_PROXY;
    }

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
     * @return Value of {@code true} if transfer was finished successfully or {@code false} otherwise.
     */
    protected static boolean transfer(final URL source, final File target, final DownloadListener progress) {
        progress.name(target.getName());
        long size = AbstractDownload.getContentLength(source);
        if (size < 0) {
            if (target.exists()) {
                size = target.length();
            } else {
                size = 1;
            }
        }
        Logger.log(LogLevel.FINE, 1, "Opening %s: ", source.toString());
        final File tmpPath = new File(target.getAbsolutePath() + TMP_EXT);
        InputStream in = null;
        OutputStream out = null;
        boolean transferOk = true;
        try {
            in = source.openConnection(AbstractDownload.PROXY).getInputStream();
            //in = source.openStream();
            out = new FileOutputStream(tmpPath);
            int transfered = 0;
            int len;
            final byte[] buff = new byte[BUFFER_SIZE];
            while((len = in.read(buff)) >= 0) {
                out.write(buff, 0, len);
                transfered += len;
                long percent = transfered * 100 / size;
                progress.progress(percent <= 100 ? (int)percent : 100);
                Logger.log(LogLevel.FINEST, 2, "Progress: %d ", percent <= 100 ? (int)percent : 100);
            }
        } catch (FileNotFoundException fne) {
            transferOk = false;
            Logger.log(LogLevel.WARNING, 0, "Could not create %s: %s", tmpPath, fne.getLocalizedMessage());
        } catch (IOException ioe) {
            transferOk = false;
            Logger.log(LogLevel.WARNING, 0, "Could not write %s: %s", tmpPath, ioe.getLocalizedMessage());                        
        } finally {
            AbstractDownload.close(in);
            AbstractDownload.close(out);
        }
        if (transferOk) {
            tmpPath.renameTo(target);
            Logger.log(LogLevel.FINE, 1, "Downloaded: %s -> %s", tmpPath.getAbsolutePath(), target.getName());
            return true;
        } else {
            tmpPath.delete();
            Logger.log(LogLevel.WARNING, 0, "Failed: %s", tmpPath.getAbsolutePath());
            return false;
        }
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
