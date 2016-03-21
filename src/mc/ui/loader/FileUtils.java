/*
 * (C) 2016 Tomas Kraus
 */
package mc.ui.loader;

import java.io.File;

/**
 * Various FS utilities.
 */
public class FileUtils {

    /**
     * Create full path String from directory and file name.
     * @param dir  Directory to be prepended.
     * @param file File name to be appended.
     * @return 
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

}
