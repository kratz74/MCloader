/*
 * (C) 2016 Tomas Kraus
 */
package mc.launcher;

import java.io.File;
import java.io.FileFilter;
import mc.log.LogLevel;
import mc.log.Logger;

/**
 * Java runtime for executing a new process.
 */
public class JavaRuntime {

    /** Logger. */
    private static final Logger LOG = Logger.getInstance();

    private enum OS {
        UNIX,
        MAC,
        WIN;

        /**
         * Return {@link OS} for specified OS name.
         * @return {@link OS} for specified OS name.
         */
        private static OS get(final String osName) {
            final String osLc = osName.toLowerCase();
            if (osName.startsWith("windows")) {
                return WIN;
            } else if (osName.startsWith("mac")) {
                return MAC;
            } else {
                return UNIX;
            }
        }

    }

    /** {@link FileFilter} for {@code bin} subdirectory. */
    private static class JavaBinDirFilter implements FileFilter {

        /** Host operating system. */
        private final OS os;

        /**
         * Creates an instance of Java runtime bin directory filter.
         */
        private JavaBinDirFilter(final OS os) {
            this.os = os;
        }

        /**
         * Accept Java runtime bin directory under Java home.
         * @param pathname Pathname to be tested.
         * @return Value of {@code true} for Java runtime bin directory or {@code false} otherwise.
         */
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                switch(os) {
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

        /** Host operating system. */
        private final OS os;

        /**
         * Creates an instance of Java executable filter.
         */
        private JavaExecFilter(final OS os) {
            this.os = os;
        }

        /**
         * Accept Java executable.
         * @param pathname Pathname to be tested.
         * @return Value of {@code true} for Java executable or {@code false} otherwise.
         */
        @Override
        public boolean accept(File pathname) {
            if (pathname.isFile() && pathname.canExecute()) {
                switch(os) {
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
     * @param os       Host operating system.
     * @return Java executable under Java home for specified OS or {@code null} if executable was not found.
     */
    private static File findJavaExec(final String javaHome, OS os) {
        File fHome = new File(javaHome);
        if (fHome.isDirectory()) {
            // Currently it checks physical directory structure under Java home for bin/java[.exe]
            final File[] filesList = fHome.listFiles(new JavaBinDirFilter(os));
            if (filesList.length > 0) {
                final File binDir = filesList[0];
                final File[] execList = binDir.listFiles(new JavaExecFilter(os));
                if (execList.length > 0) {
                    LOG.log(LogLevel.FINE, "Java executable: %s", execList[0].getAbsolutePath());
                    return execList[0];
                } else {
                    LOG.log(LogLevel.WARNING, "Java executable was not found under Java home %s/bin", javaHome);
                    return null;
                }
            } else {
                LOG.log(LogLevel.WARNING, "Directory bin was not found under Java home %s", javaHome);
                return null;
            }
        } else {
            LOG.log(LogLevel.WARNING, "Java home %s is not an existing directory", javaHome);
            return null;
        }
        
    }

    /** Java home. */ 
    private final String home;

    /** Host operating system. */
    private final OS os;

    /** Java executable. */
    private final File java;

    /**
     * Creates new Java runtime for executing a new process using current VM Java home.
     */
    public JavaRuntime() {
        os = OS.get(System.getProperty("os.name"));
        home = System.getProperty("java.home");
        java = findJavaExec(home, os);
    }

    /**
     * Get Java executable.
     * @return Java executable or {@code null} if executable does not exist.
     */
    public File getJava() {
        return java;
    }
}
