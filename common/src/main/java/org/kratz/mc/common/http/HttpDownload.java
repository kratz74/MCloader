/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.common.http;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;

/**
 * Download file using HTTP protocol.
 */
public class HttpDownload {

    /** Internal buffer size. */
    public static final int BUFFER_SIZE = 0x7FFF;    

    /** Temporary extension for file being downloaded. */    
    public static final String TMP_EXT = ".part";

    /**
     * Get remote file size.
     * @param source Source URL.
     * @param proxy  HTTP proxy configuration.
     * @return Size of remote content length or {@code -1} if content length could not be got.
     */
    public static long getContentLength(final URL source, final Proxy proxy) {
        long size = -1;
        HttpURLConnection conn = null;
        try {
            conn = proxy == null ? (HttpURLConnection)source.openConnection() : (HttpURLConnection)source.openConnection(proxy);
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
     * Open {@link InputStream} from provided source URL.
     * @param source Source URL.
     * @param proxy  HTTP proxy configuration.
     * @return {@link InputStream} from provided source URL.
     * @throws IOException if an I/O exception occurs.
     */
    public static InputStream openConnection(final URL source, final Proxy proxy) throws IOException {
        return proxy != null
                    ? source.openConnection(proxy).getInputStream()
                    : source.openConnection().getInputStream();
    }

    /**
     * Close provided {@link Closeable}.
     * @param c {@link Closeable} to close.
     */
    public static void closeConnection(final Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "Could not close socket: %s", ex.getLocalizedMessage());
            }
        }
    }

    /** Local installation path. */
    protected final File target;

    /** Source URL. */
    protected final URL source;

    /** Download progress events listener. */
    protected final HTTPDownloadListener listener;

    /** HTTP proxy (optional). */
    protected final Proxy proxy;

    /**
     * Creates an instance of HTTP download.
     * @param source Source URL.
     * @param target Local installation path.
     * @param listener Download progress events listener.
     * @param proxy HTTP proxy (optional).
     */
    public HttpDownload(final URL source, final File target, final HTTPDownloadListener listener, final Proxy proxy) {
        this.source = source;
        this.target = target;
        this.listener = listener;
        this.proxy = proxy;
    }


    /**
     * Download {@code source} {@link URL} and store it as {@code target} {@link File}.
     * @return Value of {@code true} if transfer was finished successfully or {@code false} otherwise.
     */
    public boolean transfer() {
        listener.begin();
        long size = getContentLength(source, proxy);
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
            in = openConnection(source, proxy);
            //in = source.openStream();
            out = new FileOutputStream(tmpPath);
            int transfered = 0;
            int len;
            final byte[] buff = new byte[BUFFER_SIZE];
            while((len = in.read(buff)) >= 0) {
                out.write(buff, 0, len);
                transfered += len;
                long percent = transfered * 100 / size;
                listener.progress(percent <= 100 ? (int)percent : 100);
                Logger.log(LogLevel.FINEST, 2, "Progress: %d ", percent <= 100 ? (int)percent : 100);
            }
        } catch (FileNotFoundException fne) {
            transferOk = false;
            Logger.log(LogLevel.WARNING, 0, "Could not create %s: %s", tmpPath, fne.getLocalizedMessage());
        } catch (IOException ioe) {
            transferOk = false;
            Logger.log(LogLevel.WARNING, 0, "Could not write %s: %s", tmpPath, ioe.getLocalizedMessage());                        
        } finally {
            closeConnection(in);
            closeConnection(out);
        }
        if (transferOk) {
            if (target.exists()) {
                if (!target.delete()) {
                    Logger.log(LogLevel.WARNING, 1, "Cold not delete %s, keeping old file", target.getName());
                }
            }
            tmpPath.renameTo(target);
            Logger.log(LogLevel.FINE, 1, "Downloaded: %s -> %s", tmpPath.getAbsolutePath(), target.getName());
            listener.end(true);
            return true;
        } else {
            tmpPath.delete();
            Logger.log(LogLevel.WARNING, 0, "Failed: %s", tmpPath.getAbsolutePath());
            listener.end(false);
            return false;
        }
    }

}
