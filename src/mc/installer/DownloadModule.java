/*
 * (C) 2016 Tomas Kraus
 */
package mc.installer;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import mc.log.LogLevel;
import mc.log.Logger;
import mc.ui.loader.DownloadListener;

/**
 * Downloads game module.
 */
public class DownloadModule {

    /** Internal buffer size. */
    private static final int BUFFER_SIZE = 0x7FFF;    

    /**
     * Close provided {@link Closeable}.
     * @param c {@link Closeable} to close.
     */
    private static void close(final Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "Could not close socket: %s", ex.getLocalizedMessage());
            }
        }
    }

    /** Source URL. */
    private final URL source;

    /** Target file. */
    private final File target;

    /** Download progress event listener. */
    private final DownloadListener progress;

    /**
     * Creates an instance of game module download handler.
     * @param source   Source URL.
     * @param target   Target file.
     * @param progress Download progress event listener.
     * @throws java.net.MalformedURLException when source argument is not a valid URL.
     */
    public DownloadModule(final String source, final File target, final DownloadListener progress)
            throws MalformedURLException {
        this.source = new URL(source);
        this.target = target;
        this.progress = progress;
    }

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
     * Download file from source URL to target file.
     * @throws java.io.IOException when problem with transfer occurs.
     */
    public void download() throws IOException {
        progress.name(target.getName());
        long size = getContentLength(source);
        if (size < 0) {
            if (target.exists()) {
                size = target.length();
            } else {
                size = 1;
            }
        }
        Logger.log(LogLevel.INFO, 1, "Opening %s: ", source.toString());
        InputStream in = null;
        OutputStream out = null;
        try {
            
            in = source.openStream();
            out = new FileOutputStream(target);
            int transfered = 0;
            int len;
            final byte[] buff = new byte[BUFFER_SIZE];
            while((len = in.read(buff)) >= 0) {
                out.write(buff, 0, len);
                transfered += len;
                long percent = transfered * 100 / size;
                progress.progress(percent <= 100 ? (int)percent : 100);
                Logger.log(LogLevel.INFO, 2, "Progress: %d ", percent <= 100 ? (int)percent : 100);
            }
            
        } finally {
            close(in);
            close(out);
        }
    }

}
