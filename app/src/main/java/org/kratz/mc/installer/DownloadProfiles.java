/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.installer;

import java.io.File;
import java.net.Proxy;
import java.net.URL;
import org.kratz.mc.common.http.HttpDownload;
import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;
import org.kratz.mc.ui.loader.DownloadListener;
/**
 * Download game profiles list.
 */
public class DownloadProfiles extends AbstractDownload {

    /** Thread name. */
    private static final String THREAD_NAME = "CM.UpdateProfiles";

    /** Game profiles file URL {@link String}. */
    private final String profilesUrlStr;

    /**
     * Creates an instance of base installation download.
     * @param path        Application data path for launcher.
     * @param profilesUrl Game profiles file URL.
     * @param progress    Download change listener.
     * @param proxy       HTTP PROXY configuration.
     */
    public DownloadProfiles(final String path, final String profilesUrl, final DownloadListener progress, final Proxy proxy) {
        super(path, progress, proxy);
        this.profilesUrlStr = profilesUrl;
    }

    @Override
    protected String threadName() {
        return THREAD_NAME;
    }

    /**
     * Downloading thread main method.
     * @return Value of {@code true} if thread execution was finished successfully or {@code false} otherwise.
     */
    @Override
    protected boolean thread() {
        final File target = new File(path);
        if (!AbstractDownload.mkParentDir(target)) {
            return false;
        }
        final URL source = toURL(profilesUrlStr);
        if (source == null) {
            return false;
        }
        Logger.log(LogLevel.FINE, "Downloading %s: ", profilesUrlStr);       
        return AbstractDownload.transfer(source, target, progress, proxy);
    }
    
}
