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

    /** ClassPath list. */
    private final LinkedList<String> classpath;

    /** Mod list. */
    private final LinkedList<Mod> mods;

    /**
     * Creates an empty instance of loader configuration.
     */
    LoaderConfig() {
	classpath = new LinkedList<>();
	mods = new LinkedList<>();
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
     * Add new element at the end of ClassPath list.
     * @param path Path element to be added at the end of the list.
     */
    void addMod(final String file, final String url) {
	mods.addLast(new Mod(file, url));
    }

}
