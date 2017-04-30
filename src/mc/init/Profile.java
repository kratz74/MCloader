/*
 * (C) 2017 Tomas Kraus
 */
package mc.init;

/**
 * Game profile record.
 */
public class Profile {

    /** Name of the profile. */
    protected final String name;

    /** Profile directory (exclusive on web and local file system). */
    protected final String directory;


    public Profile(final String name, final String directory) {
        this.name = name;
        this.directory = directory;
    }

    /**
     * Get name of the profile.
     * @return Name of the profile.
     */
    public String getName() {
        return name;
    }

    /**
     * Get profile directory.
     * @return Profile directory.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Returns user friendly {@link String} representation of this class.
     * @return user friendly {@link String} representation of this class.
     */
    @Override
    public String toString() {
        return name;
    }

}
