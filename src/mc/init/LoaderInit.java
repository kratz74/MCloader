/*
 * (C) 2016 Tomas Kraus
 */
package mc.init;

import mc.utils.FileUtils;
import mc.utils.OS;
import java.io.File;

/**
 * Loader initialization.
 */
public class LoaderInit {

    /** File name used to store content of this object. */
    private static final String FILE_NAME = "init";    

    /** Loader initialization instance. initialization data are stored in static context for whole application. */
    private static final LoaderInit INIT = create();

    /**
     * Create loader initialization object.
     * Content is read from initialization file if available.
     * @return Loader initialization object.
     */
    public static LoaderInit create() {
        final String filePath = FileUtils.fullPath(OS.initPath, FILE_NAME);
        LoaderInit init = InitReader.read(filePath);
        if (init == null) {
            init = new LoaderInit(OS.getDefaultGameDir());
        }
        return init;
    }

    /**
     * Persist loader initialization object when needed.
     */
    public static void persist() {
        if (INIT.modified) {
            final String filePath = FileUtils.fullPath(OS.initPath, FILE_NAME);
            final File fileDir = new File(OS.initPath);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            InitWriter.write(filePath, INIT);
        }
    }

    /**
     * Get installation path.
     * @return Installation path.
     */
    public static String getPath() {
        return INIT.path;
    }

    /**
     * Update installation path. Object is marked as modified.
     * @param path Installation path to set.
     */
    public static void updatePath(String path) {
        INIT.path = path;
        INIT.modified = true;
    }

    /**
     * Get stored user name.
     * @return Stored user name.
     */
    public static String getUserName() {
        return INIT.userName;
    }

    /**
     * Update stored user name. Object is marked as modified.
     * @param userName Stored user name to set.
     */
    public static void updateUserName(String userName) {
        INIT.userName = userName;
        INIT.modified = true;
    }

   /**
     * Get stored encrypted user password.
     * @return Stored encrypted user password.
     */
    public static String getUserPassword() {
        return INIT.userPassword;
    }

    /**
     * Update stored encrypted user password. Object is marked as modified.
     * @param userPassword Stored encrypted user password to set.
     */
    public static void updateUserPassword(String userPassword) {
        INIT.userPassword = userPassword;
        INIT.modified = true;
    }

    /** Installation path. */
    private String path;

    /** Stored user name. */
    private String userName;

    /** Stored user password (encrypted). */
    private String userPassword;

    /** Content modification indicator. */
    private boolean modified;

    /**
     * Creates an empty instance of loader initialization.
     */
    LoaderInit() {
	this.path = null;
        this.userName = null;
        this.userPassword = null;
        this.modified = false;
    }

    /**
     * Creates an instance of loader initialization with installation path set.
     * @param path Installation path.
     */
    private LoaderInit(final String path) {
	this.path = path;
        this.userName = null;
        this.userPassword = null;
        this.modified = false;
    }

    /**
     * Set installation path.
     * @param path Installation path to set.
     */
    void setPath(String path) {
        this.path = path;
    }

    /**
     * Set stored user name.
     * @param userName Stored user name to set.
     */
    void setUserName(final String userName) {
        this.userName = userName;
    }


    /**
     * Set stored encrypted user password.
     * @param userName Stored encrypted user password to set.
     */
    void setUserPassword(final String userPassword) {
        this.userPassword = userPassword;
    }

}
