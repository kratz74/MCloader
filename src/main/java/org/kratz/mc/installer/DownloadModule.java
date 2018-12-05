/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.installer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.kratz.mc.ui.loader.DownloadListener;

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

    /** HTTP proxy configuration. */
    private final Proxy proxy;

    /**
     * Creates an instance of game module download handler.
     * @param source   Source URL.
     * @param target   Target file.
     * @param progress Download progress event listener.
     * @param proxy    HTTP PROXY configuration.
     * @throws java.net.MalformedURLException when source argument is not a valid URL.
     */
    public DownloadModule(final String source, final File target, final DownloadListener progress, final Proxy proxy)
            throws MalformedURLException {
        this.source = new URL(source);
        this.target = target;
        this.progress = progress;
        this.proxy = proxy;
    }

    /**
     * Download file from source URL to target file.
     * @throws java.io.IOException when problem with transfer occurs.
     */
    public void download() throws IOException {
        if (!AbstractDownload.mkParentDir(target)) {
            return;
        }
        AbstractDownload.transfer(source, target, progress, proxy);
    }

}
