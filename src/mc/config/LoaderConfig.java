/*
 * (C) 2016 Tomas Kraus
 */
package mc.config;

import java.util.LinkedList;
import mc.log.LogLevel;
import mc.log.Logger;
import mc.ui.loader.FileUtils;

/**
 * Loader configuration.
 */
public class LoaderConfig {

    /**
     * Mod configuration element.
     */
    public static class Mod {

        /** Mod file name. */
	private final String file;

        /** Mod checksum (Adler32). */
        private final long chkSum;

        /** Mod download URL. */
	private final String url;

	/**
	 * Creates an instance of mod configuration element.
	 * @param file Mod file name.
         * @param chkSum Module file checksum (Adler32).
	 * @param url  Mod download URL.
	 */
	Mod(final String file, final String chkSum, final String url) {
	    this.file = file;
            long ad32;
            try {
                ad32 = Long.parseLong(chkSum, 0x10);
            } catch (NumberFormatException ex) {
                Logger.log(LogLevel.WARNING, "Error setting Adler32 checksum %s for %s", chkSum, file);
                ad32 = 0;
            }
            this.chkSum = ad32;
	    this.url = url;
	}

	/**
	 * Get mod file name.
	 * @return Mod file name.
	 */
	public String getFile() {
	    return file;
	}

        /**
         * Build absolute path of local module file.
         * @param path Game installation path.
         * @return Absolute path of local module file.
         */
        public String buildLocalPath(final String path){
            return FileUtils.fullPath(path, FileUtils.MODULES_DIR, file);
        }

        /**
         * Get mod checksum (Adler32).
         * @return Mod checksum (Adler32).
         */
        public long getChkSum() {
            return chkSum;
        }

        /**
	 * Get mod download URL.
	 * @return Mod download URL.
	 */
	public String getUrl() {
	    return url;
	}
    }

    /** Java options list. */
    private final LinkedList<Property> javaOptions;

    /** Java properties list. */
    private final LinkedList<Property> properties;

    /** ClassPath list. */
    private final LinkedList<String> classpath;

    /** Main class startup arguments list. */
    private final LinkedList<Argument> arguments;

    /** Mod list. */
    private final LinkedList<Mod> mods;

    /** Startup class. */
    private String startupClass;

    /**
     * Creates an empty instance of loader configuration.
     */
    LoaderConfig() {
        javaOptions = new LinkedList<>();
        properties = new LinkedList<>();
	classpath = new LinkedList<>();
        arguments = new LinkedList<>();
	mods = new LinkedList<>();
        startupClass = null;
    }

    /**
     * Add new element at the end of Java options list.
     * @param option Java option to be added at the end of the list.
     */
    void addJavaOption(final Property option) {
	javaOptions.addLast(option);
    }

    /**
     * Get Java options list.
     * @return Java options list.
     */
    public LinkedList<Property> getJavaOptions() {
        return javaOptions;
    }

    /**
     * Add new element at the end of Java properties list.
     * @param property Java property to be added at the end of the list.
     */
    void addProperty(final Property property) {
	properties.addLast(property);
    }

    /**
     * Get Java properties list.
     * @return Java properties list.
     */
    public LinkedList<Property> getProperties() {
        return properties;
    }

    /**
     * Add new element at the end of ClassPath list.
     * @param path Path element to be added at the end of the list.
     */
    void addClassPath(final String path) {
	classpath.addLast(path);
    }

    /**
     * Get ClassPath list.
     * @return ClassPath list.
     */
    public LinkedList<String> getClassPath() {
        return classpath;
    }

    /**
     * Add new element at the end of Main class startup arguments list.
     * @param path Argument to be added at the end of the list.
     */
    void addArgument(final Argument argument) {
	arguments.addLast(argument);
    }

    /**
     * Get Main class startup arguments list.
     * @return Main class startup arguments list.
     */
    public LinkedList<Argument> getArguments() {
        return arguments;
    }

    /**
     * Get modules list.
     * @return Modules list.
     * @retrn Modules list.
     */
    public LinkedList<Mod> getMods() {
	return mods;
    }

    /**
     * Add new element at the end of modules list.
     * @param file   Module file name.
     * @param chkSum Module file checksum (Adler32).
     * @param url    Module URL.
     */
    void addMod(final String file, final String chkSum, final String url) {
	mods.addLast(new Mod(file, chkSum, url));
    }

    /**
     * Get startup class name.
     * @return Startup class name.
     */
    public String getStartupClass() {
        return startupClass;
    }

    /**
     * Set startup class name.
     * @param startupClass Startup class name.
     */
    void setStartupClass(final String startupClass) {
        this.startupClass = startupClass;
    }

}
