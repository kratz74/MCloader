/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.launcher;

import java.io.File;
import java.io.FileFilter;

import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;
import org.kratz.mc.utils.OS;

/**
 * Java runtime for executing a new process.
 */
public class JavaRuntime {

    /** {@link FileFilter} for {@code bin} subdirectory. */
    private static class JavaBinDirFilter implements FileFilter {

        /**
         * Creates an instance of Java runtime bin directory filter.
         */
        private JavaBinDirFilter() {
        }

        /**
         * Accept Java runtime bin directory under Java home.
         * @param pathname Pathname to be tested.
         * @return Value of {@code true} for Java runtime bin directory or {@code false} otherwise.
         */
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                switch(OS.os) {
                    case UNIX: return "bin".equals(pathname.getName());
                    case MAC:
                    case WIN:  return "bin".equals(pathname.getName().toLowerCase());
                    default:
                        throw new IllegalStateException("Unknown OS identifier");
                }
            } else {
                return false;
            }
        }
        
    }

    /** {@link FileFilter} for {@code java} executable. */
    private static class JavaExecFilter implements FileFilter {

        /**
         * Creates an instance of Java executable filter.
         */
        private JavaExecFilter() {
        }

        /**
         * Accept Java executable.
         * @param pathname Pathname to be tested.
         * @return Value of {@code true} for Java executable or {@code false} otherwise.
         */
        @Override
        public boolean accept(File pathname) {
            if (pathname.isFile() && pathname.canExecute()) {
                switch(OS.os) {
                    case UNIX: 
                    case MAC:  return "java".equals(pathname.getName());
                    case WIN:  return "java.exe".equals(pathname.getName().toLowerCase());
                    default:
                        throw new IllegalStateException("Unknown OS identifier");
                }
            } else {
                return false;
            }
        }
        
    }

    /**
     * Find Java executable under Java home for specified OS.
     * @param javaHome Java home
     * @return Java executable under Java home for specified OS or {@code null} if executable was not found.
     */
    private static File findJavaExec(final String javaHome) {
        File fHome = new File(javaHome);
        if (fHome.isDirectory()) {
            // Currently it checks physical directory structure under Java home for bin/java[.exe]
            final File[] filesList = fHome.listFiles(new JavaBinDirFilter());
            if (filesList.length > 0) {
                final File binDir = filesList[0];
                final File[] execList = binDir.listFiles(new JavaExecFilter());
                if (execList.length > 0) {
                    Logger.log(LogLevel.FINE, "Java executable: %s", execList[0].getAbsolutePath());
                    return execList[0];
                } else {
                    Logger.log(LogLevel.WARNING, "Java executable was not found under Java home %s/bin", javaHome);
                    return null;
                }
            } else {
                Logger.log(LogLevel.WARNING, "Directory bin was not found under Java home %s", javaHome);
                return null;
            }
        } else {
            Logger.log(LogLevel.WARNING, "Java home %s is not an existing directory", javaHome);
            return null;
        }
        
    }

    /** Java home. */ 
    private final String home;

    /** Java executable. */
    private final File java;

    /**
     * Creates new Java runtime for executing a new process using current VM Java home.
     */
    public JavaRuntime() {
        home = System.getProperty("java.home");
        java = findJavaExec(home);
    }

    /**
     * Get Java executable.
     * @return Java executable or {@code null} if executable does not exist.
     */
    public File getJava() {
        return java;
    }
}
