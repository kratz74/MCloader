/*
 * (C) 2016 Tomas Kraus
 */
package mc.config;

import java.util.LinkedList;

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

	/** Mod download URL. */
	private final String url;

	/**
	 * Creates an instance of mod configuration element.
	 * @param file Mod file name.
	 * @param url  Mod download URL.
	 */
	Mod(final String file, final String url) {
	    this.file = file;
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
     * Add new element at the end of ClassPath list.
     * @param path Path element to be added at the end of the list.
     */
    void addMod(final String file, final String url) {
	mods.addLast(new Mod(file, url));
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
