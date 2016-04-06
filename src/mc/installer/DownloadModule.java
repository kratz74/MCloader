/*
 * (C) 2016 Tomas Kraus
 */
package mc.installer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import static mc.installer.AbstractDownload.TMP_EXT;
import mc.log.LogLevel;
import mc.log.Logger;
import mc.ui.loader.DownloadListener;

/**
 * Downloads game module.
 */
public class DownloadModule {

    /** Internal buffer size. */
    private static final int BUFFER_SIZE = 0x7FFF;    

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
     * Download file from source URL to target file.
     * @throws java.io.IOException when problem with transfer occurs.
     */
    public void download() throws IOException {
        File parentDir = target.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
           boolean dirCreated = parentDir.mkdirs();
            if (dirCreated) {
                Logger.log(LogLevel.FINE, 1, "Created %s", parentDir.getAbsolutePath());
            } else {
                Logger.log(LogLevel.WARNING, 0, "Could not create %s", parentDir.getAbsolutePath());
            }
            
        }
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
            in = source.openStream();
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
        } else {
            tmpPath.delete();
            Logger.log(LogLevel.WARNING, 0, "Failed: %s", tmpPath.getAbsolutePath());
        }
    }

}
