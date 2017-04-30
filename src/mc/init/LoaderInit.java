/*
 * (C) 2016 Tomas Kraus
 */
package mc.init;

import java.io.File;
import java.util.LinkedList;
import mc.utils.FileUtils;
import mc.utils.OS;

/**
 * Loader initialization.
 */
public class LoaderInit {

    /** Games base download URL. */
    private static final String BASE_URL = "http://www.carovnak.cz/dist";
    
    /** File name used to store content of this object. */
    private static final String INIT_NAME = "init";    

    /** File name used to store profiles list. */
    private static final String PROFILES_NAME = "profiles.json";    

    /** Full path of file name used to store profiles list. */
    public static final String PROFILES_PATH = FileUtils.fullPath(OS.initPath, PROFILES_NAME);    

    /** Profiles file URL. */
    public static final String PROFILES_URL = BASE_URL + '/' + PROFILES_NAME; 

    /** Loader initialization instance. initialization data are stored in static context for whole application. */
    private static final LoaderInit INIT = create();

    /**
     * Create loader initialization object.
     * Content is read from initialization file if available.
     * @return Loader initialization object.
     */
    public static LoaderInit create() {
        final String initPath = FileUtils.fullPath(OS.initPath, INIT_NAME);
        LoaderInit init = InitReader.read(initPath);
        if (init == null) {
            init = new LoaderInit();
        }
        final File profilesFile = new File(PROFILES_PATH);
        init.setProfiles(ProfileReader.read(profilesFile));
        return init;
    }

    /**
     * Persist loader initialization object when needed.
     */
    public static void persist() {
        if (INIT.modified) {
            final String filePath = FileUtils.fullPath(OS.initPath, INIT_NAME);
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

   /**
     * Get stored profile name.
     * @return Stored profile name.
     */
    public static String getProfile() {
        return INIT.profile;
    }
    
   /**
     * Update stored profile name.
     * @param profile Profile name to be stored.
     */
    public static void updateProfile(String profile) {
        INIT.profile = profile;
        INIT.modified = true;
    }

    /**
     * Get stored profiles list.
     * @return Stored profiles list.
     */
    public static LinkedList<Profile> getProfiles() {
        return INIT.profiles;
    }

    /**
     * Update stored profiles list.
     */
    public static void updateProfiles() {
        final File profilesFile = new File(PROFILES_PATH);
        INIT.setProfiles(ProfileReader.read(profilesFile));
    }

    /**
     * Get currently selected profile.
     * @return Currently selected profile.
     */
    public static Profile getCurrentProfile() {
        if (INIT.profiles == null || INIT.profile == null || INIT.profile.length() == 0) {
            return null;
        }
        Profile current = null;
        for (Profile profile : INIT.profiles) {
            if (INIT.profile.equalsIgnoreCase(profile.getDirectory())) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Get game directory related to currently selected profile.
     * @return Game directory related to currently selected profile.
     */
    public static String getCurrentGameDir() {
        final Profile profile = getCurrentProfile();
        return profile != null ? OS.getGameDir(profile.getDirectory()) : null;
    }

    /**
     * Get game directory related to currently selected profile.
     * @param profileIn Current profile if available. Search for default profile will be done when {@code null}.
     * @return Game directory related to currently selected profile.
     */
    public static String getCurrentConfigFile(final Profile profileIn) {
        final Profile profile = profileIn != null ? profileIn : getCurrentProfile();
        return profile != null ? FileUtils.fullPathwithsuffix(OS.initPath, ".json", profile.getDirectory()) : null;
    }

    /**
     * Get game directory related to currently selected profile.
     * @param profileIn Current profile if available. Search for default profile will be done when {@code null}.
     * @return Game directory related to currently selected profile.
     */
    public static String getCurrentConfigURL(final Profile profileIn) {
        final Profile profile = profileIn != null ? profileIn : getCurrentProfile();
        if (profile != null) {
            return BASE_URL + '/' + profile.getDirectory() + ".json";
        } else {
            return null;
        }
    }

    /** Installation path. */
    private String path;

    /** Stored user name. */
    private String userName;

    /** Stored user password (encrypted). */
    private String userPassword;

    /** Selected game profile. */
    private String profile;

    /** List of existing game profiles. */
    private LinkedList<Profile> profiles;

    /** Content modification indicator. */
    private boolean modified;

    /**
     * Creates an empty instance of loader initialization.
     */
    LoaderInit() {
        this(null);
    }

    /**
     * Creates an instance of loader initialization with installation path set.
     * @param path Installation path.
     */
    private LoaderInit(final String path) {
        this.path = path;
        this.userName = null;
        this.userPassword = null;
        this.profile = null;
        this.profiles = null;
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

    /**
     * Set stored profile.
     * @param profile Profile name to set.
     */
    void setProfile(final String profile) {
        this.profile = profile;
    }

    /**
     * Set list of existing game profiles.
     * @param profiles List of existing game profiles to set.
     */
    void setProfiles(final LinkedList<Profile> profiles) {
        this.profiles = profiles;
    }

}
