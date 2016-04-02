/*
 * (C) 2016 Tomas Kraus
 */
package mc.installer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.ListIterator;
import mc.config.LoaderConfig;
import mc.log.LogLevel;
import mc.log.Logger;
import mc.ui.loader.DownloadListener;

/**
 * Download all game modules targeted for being downloaded.
 */
public class DownloadModules implements Downloader, Runnable {

    /** Thread name. */
    private static final String THREAD_NAME = "CM164.Download";

    /** Game installation path. */
    private final String path;

    /** Modules to download. */
    private final LinkedList<LoaderConfig.Mod> mods;

    /** Download change listener. */
    private final DownloadListener progress;

    /** Thread internal execution control. */
    private boolean execute;

    /** External thread execution notification. */
    private boolean isRunning;

    private Thread thread;
    /**
     * Creates an instance of modules download.
     * @param path     Game installation path.
     * @param mods     Modules to download.
     * @param progress Download change listener.
     */
    public DownloadModules(final String path, final LinkedList<LoaderConfig.Mod> mods, final DownloadListener progress) {
        this.path = path;
        this.mods = mods;
        this.progress = progress;
        this.execute = false;
        this.isRunning = false;
        this.thread = null;
    }

    /**
     * Execute download in parallel thread.
     */
    @Override
    public void start() {
        this.execute = true;
        this.isRunning = true;
        this.thread = new Thread(this, THREAD_NAME);
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
     * Downloading thread main method.
     */
    @Override
    public void run() {
        progress.begin();
        // Better to work with own copy in parallel thread.
        @SuppressWarnings("unchecked")
        final LinkedList<LoaderConfig.Mod> downloadMods = (LinkedList<LoaderConfig.Mod>)mods.clone();
        final ListIterator<LoaderConfig.Mod> iterator = downloadMods.listIterator();
        while (execute = execute && iterator.hasNext()) {
            final LoaderConfig.Mod mod = iterator.next();
            final File targetModule = new File(mod.buildLocalPath(path));
            DownloadModule download;
            try {
                download = new DownloadModule(mod.getUrl(), targetModule, progress);
            } catch (MalformedURLException ex) {
                Logger.log(LogLevel.WARNING, "Invalid URL for %s: ", mod.getFile());
                download = null;
            }
            if (download != null) {
                Logger.log(LogLevel.INFO, "Downloading %s: ", targetModule.getAbsolutePath());
                try {
                    download.download();
                } catch (IOException ex) {
                    Logger.log(LogLevel.WARNING, "Error downloading %s: ", mod.getFile());
                }
            }
            progress.moduleDone(mod);

        }
        progress.end();
        this.isRunning = false;
    }


}
