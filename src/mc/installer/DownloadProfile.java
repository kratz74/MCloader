/*
 * (C) 2017 Tomas Kraus
 */
package mc.installer;

import java.io.File;
import java.net.URL;
import mc.log.LogLevel;
import mc.log.Logger;
import mc.ui.loader.DownloadListener;

/**
 * Download game profile.
 */
public class DownloadProfile extends AbstractDownload {
    
    /** Thread name. */
    private static final String THREAD_NAME = "CM.UpdateProfile";

    /** Game profile file URL {@link String}. */
    private final String profileUrlStr;

    /**
     * Creates an instance of base installation download.
     * @param path        Application data path for launcher.
     * @param profileUrl  Game profile file URL.
     * @param progress    Download change listener.
    */
    public DownloadProfile(final String path, final String profileUrl, final DownloadListener progress) {
        super(path, progress);
        this.profileUrlStr = profileUrl;
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
        Logger.log(LogLevel.FINE, "Downloading %s: ", profileUrlStr);
        final URL source = toURL(profileUrlStr);
        if (source == null) {
            return false;
        }
        return AbstractDownload.transfer(source, target, progress);
    }

}
