/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.zip.Adler32;

import org.kratz.mc.log.LogLevel;
import org.kratz.mc.log.Logger;

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

    /** Internal buffer size. */
    private static final int BUFFER_SIZE = 0x7FFF;

    /**
     * Compute Adler32 checksum on given file.
     * @param file Checksum computing source file.
     * @param extBuff Use external buffer for reading the file when not <code>null</code>.
     * @return Adler32 checksum value.
     */
    public static long adler32(final File file, final byte[] extBuff) {
        final byte[] buff = extBuff != null ? extBuff : new byte[BUFFER_SIZE];
        final Adler32 ad32 = new Adler32();
        try (final FileInputStream is = new FileInputStream(file)) {
            int len;
            while ((len = is.read(buff)) >= 0) {
                ad32.update(buff, 0, len);
            }
        } catch (FileNotFoundException ex) {
            Logger.log(LogLevel.WARNING, "Could not open %s for reading: %s", file.getName(), ex.getLocalizedMessage());
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Could not read %s file: %s", file.getName(), ex.getLocalizedMessage());
        }
        return ad32.getValue();
    }

    /**
     * Compute SHA3-256 checksum on given file.
     * @param file Checksum computing source file.
     * @param extBuff Use external buffer for reading the file when not <code>null</code>.
     * @return SHA3-256 checksum value or <code>null</code> if checksum could not be computed.
     */
    public static String sha3_256(final File file, final byte[] extBuff) {
        final byte[] buff = extBuff != null ? extBuff : new byte[BUFFER_SIZE];
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
             Logger.log(LogLevel.WARNING, "Could not initialize checksum generator for SHA3-256 algorithm");
             return null;
        }
        try (final FileInputStream is = new FileInputStream(file)) {
            int len;
            while ((len = is.read(buff)) >= 0) {
                md.update(buff, 0, len);
            }
        } catch (FileNotFoundException ex) {
            Logger.log(LogLevel.WARNING, "Could not open %s for reading: %s", file.getName(), ex.getLocalizedMessage());
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Could not read %s file: %s", file.getName(), ex.getLocalizedMessage());
        }
        byte[] chkSum = md.digest();
        // Log computed SHA3-256 checksum on given file
        if (Logger.shouldLog(LogLevel.FINE)) {
            final StringBuilder sb = new StringBuilder(chkSum.length * 2);
            final Formatter f = new Formatter(sb);
            for (byte b : chkSum) {
                f.format("%02x", b);
            }
            Logger.log(LogLevel.FINE, "Checksum %s: %s", file.getName(), sb.toString());
        }
        return Base64.getEncoder().encodeToString(chkSum);
    }

}
