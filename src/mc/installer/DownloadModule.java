/*
 * (C) 2016 Tomas Kraus
 */
package mc.installer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import mc.ui.loader.DownloadListener;

/**
 * Downloads game module.
 */
public class DownloadModule {

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
        if (!AbstractDownload.mkParentDir(target)) {
            return;
        }
        AbstractDownload.transfer(source, target, progress);
    }

}
