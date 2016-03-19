/*
 * (C) 2016 Tomas Kraus
 */
package mc.init;

import mc.ui.loader.FileUtils;

/**
 * Loader initialization.
 */
public class LoaderInit {

    /** File name used to store content of this object. */
    public static final String FILE_NAME = ".cm164";    

    /**
     * Create loader initialization object.
     * Content is read from initialization file if available.
     * @param homeDir User home directory.
     * @return Loader initialization object.
     */
    public static LoaderInit create(final String homeDir) {
        final String filePath = FileUtils.fullPath(homeDir, FILE_NAME);
        LoaderInit init = InitReader.read(filePath);
        if (init == null) {
            init = new LoaderInit();
        }
        return init;
    }

    /**
     * Persist loader initialization object when needed.
     * @param homeDir User home directory.
     * @param init Loader initialization object to be persisted.
     * 
     */
    public static void persist(final String homeDir, final LoaderInit init) {
        if (init.modified) {
            final String filePath = FileUtils.fullPath(homeDir, FILE_NAME);
            InitWriter.write(filePath, init);
        }
    }

    /** Installation path. */
    private String path;

    /** Stored user name. */
    private String userName;

    /** Content modification indicator. */
    private boolean modified;

    /**
     * Creates an empty instance of loader initialization.
     */
    LoaderInit() {
	path = null;
        userName = null;
        modified = false;
    }

    /**
     * Get installation path.
     * @return Installation path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set installation path.
     * @param path Installation path to set.
     */
    void setPath(String path) {
        this.path = path;
    }

    /**
     * Update installation path. Object is marked as modified.
     * @param path Installation path to set.
     */
    public void updatePath(String path) {
        this.path = path;
        modified = true;
    }

    /**
     * Get stored user name.
     * @return Stored user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set stored user name.
     * @param userName Stored user name to set.
     */
    void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Update stored user name. Object is marked as modified.
     * @param userName Stored user name to set.
     */
    public void updateUserName(String userName) {
        this.userName = userName;
        modified = true;
    }

}
