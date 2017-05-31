/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.utils;

import java.io.File;

/**
 * Various FS utilities.
 */
public class FileUtils {

    /** Modules subdirectory under game installation root directory. */
    //public static final String MODULES_DIR = "mods";

    /**
     * Create full path {@link String} from directory and file names.
     * @param dir  Directory to be prepended.
     * @param file File names to be appended.
     * @return Full path {@link String}.
     */
    public static String fullPath(final String dir, final String... file) {
        final boolean[] addSep = new boolean[file.length];
        int sbLen = dir.length();
        addSep[0] = !dir.endsWith(File.separator) && !file[0].startsWith(File.separator);
        sbLen += file[0].length();
        if (addSep[0]) {
            sbLen += OS.FSEP_LEN;
        }
        for (int i = 1; i < file.length; i++) {
            addSep[i] = !file[i - 1].endsWith(File.separator) && !file[i].startsWith(File.separator);
            sbLen += file[1].length();
            if (addSep[0]) {
                sbLen += OS.FSEP_LEN;
            }
        }
        final StringBuilder sb = new StringBuilder(sbLen);
        sb.append(dir);
        for (int i = 0; i < file.length; i++) {
            if (addSep[i]) {
                sb.append(File.separator);
            }
            sb.append(file[i]);
        }
        return sb.toString();
    }
 
    /**
     * Create full path {@link String} from directory and file names.
     * @param dir  Directory to be prepended.
     * @param suffix File suffix to be appended.
     * @param file File names to be appended.
     * @return Full path {@link String}.
     */
    public static String fullPathwithsuffix(final String dir, final String suffix, final String... file) {
        final boolean[] addSep = new boolean[file.length];
        int suffixLen = suffix != null ? suffix.length(): 0;
        int sbLen = dir.length();
        addSep[0] = !dir.endsWith(File.separator) && !file[0].startsWith(File.separator);
        sbLen += file[0].length();
        if (addSep[0]) {
            sbLen += OS.FSEP_LEN;
        }
        for (int i = 1; i < file.length; i++) {
            addSep[i] = !file[i - 1].endsWith(File.separator) && !file[i].startsWith(File.separator);
            sbLen += file[1].length();
            if (addSep[0]) {
                sbLen += OS.FSEP_LEN;
            }
        }
        final StringBuilder sb = new StringBuilder(sbLen + suffixLen);
        sb.append(dir);
        for (int i = 0; i < file.length; i++) {
            if (addSep[i]) {
                sb.append(File.separator);
            }
            sb.append(file[i]);
        }
        if (suffixLen > 0) {
            sb.append(suffix);
        }
        return sb.toString();
    }

    /**
     * Create directory structure on file system.
     * @param path Path of directory structure to be created.
     * @return Value of {@code true} if and only if the directory was created, along with all necessary parent directories
     *         or {@code false} otherwise.
     */
    public static boolean mkDirs(final File path) {
        if (!path.exists()) {
            return path.mkdirs();
        } else {
            return false;
        }
    }

}
