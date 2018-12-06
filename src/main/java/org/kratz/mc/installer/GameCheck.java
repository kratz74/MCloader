/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.installer;

import java.io.File;
import java.util.LinkedList;
import java.util.zip.Adler32;
import org.kratz.mc.config.LoaderConfig;
import org.kratz.mc.log.LogLevel;
import org.kratz.mc.log.Logger;
import org.kratz.mc.utils.FileUtils;

/**
 * Check whether game is installed in specified path.
 */
public class GameCheck {

    /** Internal buffer size. */
    private static final int BUFFER_SIZE = 0x7FFF;    

    /**
     * Creates a new instance game check.
     */
    public GameCheck() {
    }

    /**
     * Check game modules.
     * @param path Game installation path.
     * @param modsPath Modules path under game installation root.
     * @param modules List of game modules.
     * @return List of missing or invalid modules.
     */
    public static final LinkedList<LoaderConfig.Mod> checkModules(
            final String path, final String modsPath, final LinkedList<LoaderConfig.Mod> modules) {
        final byte[] buff = new byte[BUFFER_SIZE];
        final LinkedList<LoaderConfig.Mod> failed = new  LinkedList<>();
        //long tsBeg = System.currentTimeMillis();
        // Lambdas (even parallel stream) are slover!
        for (LoaderConfig.Mod mod : modules) {
            final Adler32 ad32 = new Adler32();
            final String filePath = mod.buildLocalPath(path, modsPath);
            final File modFile = new File(filePath);
            if (modFile.isFile() && modFile.canRead()) {
                final long chkSum = FileUtils.adler32(modFile, buff);
                final long modChkSum = mod.getChkSum();
                if (chkSum != modChkSum) {
                    failed.add(mod);
                    Logger.log(LogLevel.FINE, "Invalid checksum for %s: %X :: %X", modFile.getName(), chkSum, modChkSum);
                } else {
                    Logger.log(LogLevel.FINE, "Checksum OK for %s: %X :: %X", modFile.getName(), chkSum, modChkSum);
                }
            } else {
                failed.add(mod);
                Logger.log(LogLevel.FINE, "Missing %s", modFile.getName());
            }
        }
        //Logger.log(LogLevel.INFO, "Time consumed: %d ms", System.currentTimeMillis() - tsBeg);
        return failed;
    }

    /**
     * Check existence of game installation root directory.
     * @param path Game installation path.
     * @return Value of {@code true} when game installation root directory exists or {@code false} otherwise.
     */
    public boolean checkInstallDir(final String path) {
        if (path == null) {
            return false;
        }
        final File fPath = new File(path);
        return fPath.isDirectory();
    }

    /**
     * Check existence of class path files.
     * @param path Game installation path.
     * @return Value of {@code true} when all class path files exist or {@code false} otherwise.
     */
    private boolean checkClassPath(final String path) {
        if (!LoaderConfig.isConfig()) {
            return false;
        }
        final LinkedList<String> cp = LoaderConfig.getClassPath();
        boolean cpOk = true;
        for (final String item : cp) {
            final File cpFile = new File(FileUtils.fullPath(path, item));
            if (!cpFile.isFile() || !cpFile.canRead()) {
                cpOk = false;
                break;
            }
        }
        return cpOk;
    }

    public boolean check(final String path) {
        return checkClassPath(path);
    }

}
