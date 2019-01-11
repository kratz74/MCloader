/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.installer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;
import org.kratz.mc.config.LoaderConfig;
import org.kratz.mc.ui.loader.DownloadListener;
import org.kratz.mc.utils.FileUtils;

/**
 * Download all game modules targeted for being downloaded.
 */
public class DownloadModules extends AbstractDownload {

    /** Thread name. */
    private static final String THREAD_NAME = "CM.Update";

    /** Modules path under game installation root. */
    private final String modsPath;

    /** Whether to remove unregistered files. */
    private final boolean rmUnreg;

    /** Modules to download. */
    private final LinkedList<LoaderConfig.Mod> mods;

    /**
     * Creates an instance of modules download.
     * @param path      Game installation path.
     * @param modsPath  Modules path under game installation root.
     * @param mods      Modules to download.
     * @param rmUnreg   Whether to remove unregistered files.
     * @param progress  Download change listener.
     * @param proxy     HTTP PROXY configuration.
     */
    public DownloadModules(final String path, final String modsPath,
            final LinkedList<LoaderConfig.Mod> mods, final boolean rmUnreg,
            final DownloadListener progress, final Proxy proxy) {
        super(path, progress, proxy);
        this.modsPath = modsPath;
        this.mods = mods;
        this.rmUnreg = rmUnreg;
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
        if (!AbstractDownload.mkDir(new File(FileUtils.fullPath(path, modsPath)))) {
            return false;
        }
        // Better to work with own copy in parallel thread.
        @SuppressWarnings("unchecked")
        final LinkedList<LoaderConfig.Mod> downloadMods = (LinkedList<LoaderConfig.Mod>)mods.clone();
        final ListIterator<LoaderConfig.Mod> iterator = downloadMods.listIterator();
        while (execute = execute && iterator.hasNext()) {
            final LoaderConfig.Mod mod = iterator.next();
            final File targetModule = new File(mod.buildLocalPath(path, modsPath));
            DownloadModule download;
            try {
                download = new DownloadModule(mod.getUrl(), targetModule, progress, proxy);
            } catch (MalformedURLException ex) {
                Logger.log(LogLevel.WARNING, "Invalid URL: %s", mod.getFile());
                download = null;
            }
            if (download != null) {
                Logger.log(LogLevel.FINE, "Downloading %s: ", targetModule.getAbsolutePath());
                try {
                    download.download();
                } catch (IOException ex) {
                    Logger.log(LogLevel.WARNING, "Error downloading %s: ", mod.getFile());
                }
            }
            progress.moduleDone(mod);
        }
        // TODO: Extract to a separate method.
        if (rmUnreg) {
            Logger.log(LogLevel.FINE, "Removing unregistered mods");
            File modsDir = new File(FileUtils.fullPath(path, modsPath));
            File[] installedMods = modsDir.listFiles();
            LinkedList<LoaderConfig.Mod> allModsList = LoaderConfig.getMods();
            if (installedMods != null && installedMods.length > 0) {
                Set<String> allInstalledMods = new HashSet<>(installedMods.length);
                for (File file : installedMods) {
                    Logger.log(LogLevel.FINEST, " - addind mod to all: %s", file.getName());
                    allInstalledMods.add(file.getName());
                }
                for (LoaderConfig.Mod mod : allModsList) {
                    Logger.log(LogLevel.FINEST, " - already installed: %s", mod.getFile());
                    allInstalledMods.remove(mod.getFile());
                }
                for (String unregistered : allInstalledMods) {
                    File toDelete = new File(modsDir, unregistered);
                    if (toDelete.isFile() && toDelete.canWrite()) {
                        Logger.log(LogLevel.FINE, " - deleting unregistered mod file: %s", unregistered);
                        toDelete.delete();
                    } else {
                        Logger.log(LogLevel.WARNING, " - could not delete unregistered mod file: %s", unregistered);
                    }
                }
            }
        }
        return true;
    }

}
