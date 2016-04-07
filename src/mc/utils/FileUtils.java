/*
 * (C) 2016 Tomas Kraus
 */
package mc.utils;

import java.io.File;

/**
 * Various FS utilities.
 */
public class FileUtils {

    /** Modules subdirectory under game installation root directory. */
    //public static final String MODULES_DIR = "mods";

    /**
     * Create full path {@link String} from directory and file name.
     * @param dir  Directory to be prepended.
     * @param file File name to be appended.
     * @return Full path {@link String}.
     */
    public static String fullPath(final String dir, final String file) {
        final boolean addSep = !dir.endsWith(File.separator) && !file.startsWith(File.separator);
        final StringBuilder sb = new StringBuilder(dir.length() + file.length() + (addSep ? 1 : 0));
        sb.append(dir);
        if (addSep) {
            sb.append(File.separator);
        }
        sb.append(file);
        return sb.toString();
    }

    /**
     * Create full path {@link String} from directory, subdirectory and file name.
     * @param dir    Directory to be prepended.
     * @param subdir Directory to be appended in the middle.
     * @param file   File name to be appended at the end.
     * @return Full path {@link String}.
     */
    public static String fullPath(final String dir, final String subdir, final String file) {
        final boolean addSepDir = !dir.endsWith(File.separator) && !subdir.startsWith(File.separator);
        final boolean addSepFile = !subdir.endsWith(File.separator) && !file.startsWith(File.separator);
        final int len = dir.length() + subdir.length() + file.length() + (addSepDir ? 1 : 0)+ (addSepFile ? 1 : 0);
        final StringBuilder sb = new StringBuilder(len);
        sb.append(dir);
        if (addSepDir) {
            sb.append(File.separator);
        }
        sb.append(subdir);
        if (addSepFile) {
            sb.append(File.separator);
        }
        sb.append(file);
        return sb.toString();
    }
  
}
