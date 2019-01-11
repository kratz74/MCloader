/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.config;

import java.util.LinkedList;

import org.kratz.mc.log.LogLevel;
import org.kratz.mc.log.Logger;
import org.kratz.mc.utils.FileUtils;

/**
 * Loader configuration.
 */
public class LoaderConfig {

    /** Path elements separator used in JSON configuration files. */
    public static final char SEPARATOR = '/';

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
         * @param url Mod download URL.
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
         * @param path     Game installation path.
         * @param modsPath Modules path under game installation root.
         * @return Absolute path of local module file.
         */
        public String buildLocalPath(final String path, final String modsPath){
            return FileUtils.fullPath(path, modsPath, file);
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

    /** Loader configuration instance. Configuration data are stored in static context for whole application. */
    private static LoaderConfig CONFIG = ConfigReader.read();

    /**
     * Update loader configuration depending on current profile.
     */
    public static void update() {
        CONFIG = ConfigReader.read();
    }

    /**
     * Get Java options list.
     * @return Java options list.
     */
    public static LinkedList<Property> getJavaOptions() {
        return CONFIG.javaOptions;
    }

    /**
     * Get Java properties list.
     * @return Java properties list.
     */
    public static LinkedList<Property> getProperties() {
        return CONFIG.properties;
    }

    /**
     * Get ClassPath list.
     * @return ClassPath list.
     */
    public static LinkedList<String> getClassPath() {
        return CONFIG.classpath;
    }

    /**
     * Get game base package URL.
     * @return Game base package URL
     */
    public static String getGameUrl() {
        return CONFIG.gameUrl;
    }

    /**
     * Get modules path under game root directory.
     * @return Modules path under game root directory.
     */
    public static String getModsPath() {
        return CONFIG.modsPath;
    }

    /**
     * Get Main class startup arguments list.
     * @return Main class startup arguments list.
     */
    public static LinkedList<Argument> getArguments() {
        return CONFIG.arguments;
    }

    /**
     * Get modules list.
     * @return Modules list.
     */
    public static LinkedList<Mod> getMods() {
	return CONFIG.mods;
    }

    /**
     * Get startup class name.
     * @return Startup class name.
     */
    public static String getStartupClass() {
        return CONFIG.startupClass;
    }

    /**
     * Check whether configuration exists.
     * @return Value of {@code true} when configuration exists or {@code false} otherwise.
     */
    public static boolean isConfig() {
        return CONFIG != null;
    }

    /** Java options list. */
    private final LinkedList<Property> javaOptions;

    /** Java properties list. */
    private final LinkedList<Property> properties;

    /** ClassPath list. */
    private final LinkedList<String> classpath;

    /** Game base package URL. */
    private String gameUrl;

    /** Modules path under game root directory. */
    private String modsPath;

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
        gameUrl = null;
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
     * Add new element at the end of Java properties list.
     * @param property Java property to be added at the end of the list.
     */
    void addProperty(final Property property) {
	properties.addLast(property);
    }

    /**
     * Add new element at the end of ClassPath list.
     * @param path Path element to be added at the end of the list.
     */
    void addClassPath(final String path) {
	classpath.addLast(path);
    }

    /**
     * Set game base package URL.
     * @param gameUrl Game base package URL.
     */
    void setGameUrl(final String gameUrl) {
        this.gameUrl = gameUrl;
    }

    /**
     * Set modules path under game root directory.
     * @param modsPath Modules path under game root directory.
     */
    void setModsPath(final String modsPath) {
        this.modsPath = modsPath;
    }

    /**
     * Add new element at the end of Main class startup arguments list.
     * @param path Argument to be added at the end of the list.
     */
    void addArgument(final Argument argument) {
	arguments.addLast(argument);
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
     * Set startup class name.
     * @param startupClass Startup class name.
     */
    void setStartupClass(final String startupClass) {
        this.startupClass = startupClass;
    }

}
