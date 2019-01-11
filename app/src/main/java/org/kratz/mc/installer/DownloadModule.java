/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.installer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import org.kratz.mc.common.http.HTTPDownloadListener;
import org.kratz.mc.common.http.HttpDownload;
import org.kratz.mc.ui.loader.DownloadListener;

/**
 * Downloads game module.
 */
public class DownloadModule extends HttpDownload {

    /** Download progress event listener. */
    private final DownloadListener progress;

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
        super(new URL(source), target, new HTTPProgressOnlyListener(progress), proxy);
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
        progress.name(target.getName());
        transfer();
    }

}
