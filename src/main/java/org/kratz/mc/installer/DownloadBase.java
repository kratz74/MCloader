/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.installer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.kratz.mc.log.LogLevel;
import org.kratz.mc.log.Logger;
import org.kratz.mc.ui.loader.DownloadListener;
import org.kratz.mc.utils.FileUtils;

/**
 * Download game base file and extract it.
 */
public class DownloadBase extends AbstractDownload {

    /** Thread name. */
    private static final String THREAD_NAME = "CM.Install";
    
    /** Game base package URL {@link String}. */
    private final String gameUrlStr;

    /**
     * Creates an instance of base installation download.
     * @param path      Game installation path.
     * @param gameUrl   Game base package URL.
     * @param progress  Download change listener.
     * @param proxy     HTTP proxy configuration.
     */
    public DownloadBase(final String path, final String gameUrl, final DownloadListener progress, final Proxy proxy) {
        super(path, progress, proxy);
        this.gameUrlStr = gameUrl;
    }

    /** {@inheritDoc} */
    @Override
    protected String threadName() {
        return THREAD_NAME;
    }

    /**
     * Downloading thread main method.
     * @return Value of {@code true} if thread execution was finished successfully or {@code false} otherwise.
     */
    @Override
    public boolean thread() {
        Logger.log(LogLevel.FINE, "Downloading %s: ", gameUrlStr);
        final URL gameUrl = toURL(gameUrlStr);
        if (gameUrl == null) {
            return false;
        }
        progress.name("Game basic files");
        long size = AbstractDownload.getContentLength(gameUrl, proxy);
        if (size < 0) {
            size = 1;
        }
        Logger.log(LogLevel.FINE, "Length of %s: %d", gameUrlStr, size);
        ZipInputStream in = null;
        try {
            final byte[] buff = new byte[BUFFER_SIZE];
            in = new ZipInputStream(openConnection(gameUrl, proxy));
            long transfered = 0;
            ZipEntry entry;
            while((entry = in.getNextEntry()) != null) {
                final String name = entry.getName();
                final File fullPath = new File(FileUtils.fullPath(path, name));
                if (entry.isDirectory()) {
                    if (!fullPath.exists()) {
                        boolean dirCreated = fullPath.mkdirs();
                        if (dirCreated) {
                            Logger.log(LogLevel.FINE, 1, "Created %s", fullPath.getAbsolutePath());
                        } else {
                            Logger.log(LogLevel.WARNING, 0, "Could not create %s", fullPath.getAbsolutePath());
                        }
                    }
                } else {
                    final File tmpPath = new File(fullPath.getAbsolutePath() + TMP_EXT);
                    OutputStream out = null;
                    boolean transferOk = true;
                    int len;
                    try {
                        out = new FileOutputStream(tmpPath);
                        while((len = in.read(buff, 0, BUFFER_SIZE)) >= 0) {
                            out.write(buff, 0, len);
                        }
                    } catch (FileNotFoundException fne) {
                        transferOk = false;
                        Logger.log(LogLevel.WARNING, 0, "Could not create %s: %s", tmpPath, fne.getLocalizedMessage());
                    } catch (IOException ioe) {
                        transferOk = false;
                        Logger.log(LogLevel.WARNING, 0, "Could not write %s: %s", tmpPath, ioe.getLocalizedMessage());                        
                    } finally {
                        AbstractDownload.close(out);
                    }
                    if (transferOk) {
                        tmpPath.renameTo(fullPath);
                        Logger.log(LogLevel.FINE, 1, "Downloaded: %s -> %s", tmpPath.getAbsolutePath(), fullPath.getName());
                    } else {
                        tmpPath.delete();
                        Logger.log(LogLevel.WARNING, 0, "Failed: %s", tmpPath.getAbsolutePath());
                    }
                }
                transfered += entry.getCompressedSize();
                long percent = transfered * 100 / size;
                progress.progress(percent <= 100 ? (int)percent : 100);
            }
            progress.progress(100);
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Error downloading %s: ", gameUrlStr);
        } finally {
            AbstractDownload.close(in);
        }
        return true;
    }

}
