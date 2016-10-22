/*
 * (C) 2016 Tomas Kraus
 */
package mc.utils;

import java.io.File;
import java.util.HashMap;
import mc.log.LogLevel;
import mc.log.Logger;

/**
 * Operating system type.
 */
public enum OS {
    
    UNIX,
    MAC,
    WIN;

    /** Logging levels enumeration length. */
    public static final int length = OS.values().length;

    /** OS type of this host. */
    public static final OS os = OS.getOS(System.getProperty("os.name"));

    /** Current user home directory. */
    public static final String home = getUserHome();

    /** Game directory name. */
    public static final String gameDirName = "lotr1710";

    /** Initialization folder storage path. */
    public static final String initPath = getAppData();

    /** Application data subdirectory on modern Windows (7, 8, 10). */
    private static final String WIN_APPDATA_LOCAL = "AppData\\Local";

    /** Application installation subdirectory on modern Windows (7, 8, 10). */
    private static final String WIN_APPDATA_ROAMING = "AppData\\Roaming";

    /** Application data subdirectory on older Windows (XP). */
    private static final String WIN_LOCSET_APPDATA = "Local Settings\\Application Data";

    /** {@link HashMap} for {@link String} to {@link OS} case insensitive lookup. */
    private static final HashMap<String, OS> valuesMap = new HashMap<>(2 * length);

    // Initialize String to OS case insensitive lookup Map.
    static {
        for (OS osValue : OS.values()) {
            valuesMap.put(osValue.name().toUpperCase(), osValue);
        }
    }

    /**
     * Returns {@link OS} object corresponding to the value of the specified {@link String}.
     * @param name The {@link String} to be checked.
     * @return {@link OS} object corresponding to the value of the string argument or {@code null} when
     *         there exists no corresponding {@link OS} object to provided {@link String}.
     */
    public static final OS toValue(final String name) {
        return name != null ? valuesMap.get(name.toUpperCase()) : null;
    }

    /**
     * Return {@link OS} for specified OS name.
     * @param osName OS name.
     * @return {@link OS} for specified OS name.
     */
    private static OS getOS(final String osName) {
        final String osLc = osName.toLowerCase();
        // TODO: Find more effective way.
        if (osLc.contains("windows")) {
            Logger.log(LogLevel.FINE, "OS: %s for OS name: %s", WIN, osLc);
            return WIN;
        } else if (osLc.startsWith("mac")) {
            Logger.log(LogLevel.FINE, "OS: %s for OS name: %s", MAC, osLc);
            return MAC;
        } else {
            Logger.log(LogLevel.FINE, "OS: %s for OS name: %s", UNIX, osLc);
            return UNIX;
        }
    }

    /**
     * Returns current user home directory.
     * This should work fine since JDK 8.
     * @return current user home directory.
     */
    private static String getUserHome() {
        return System.getProperty("user.home");
    }


    /**
     * Get application data directory for launcher on UNIX systems.
     * @return Application data directory for launcher on UNIX systems.
     */
    private static String getAppDataUnix() {
        final boolean addSep = !home.endsWith(File.separator);
        final StringBuilder sb = new StringBuilder(home.length() + gameDirName.length() + (addSep ? 2 : 1));
        sb.append(home);
        if (addSep) {
            sb.append(File.separator);
        }
        sb.append('.');
        sb.append(gameDirName);
        return sb.toString();
    }

    /**
     * Get {@code "AppData/Local"} directory full path on Windows.
     * @return {@code "AppData/Local"} folder on Windows.
     */
    private static String getAppDataLocalPath() {
        final boolean addSep = !home.endsWith(File.separator);
        final StringBuilder sb = new StringBuilder(home.length() + WIN_APPDATA_LOCAL.length() + (addSep ? 1 : 0));
        sb.append(home);
        if (addSep) {
            sb.append(File.separator);
        }
        sb.append(WIN_APPDATA_LOCAL);
        return sb.toString();
    }        
    
    /**
     * Get {@code "Local Settings/Application Data"} directory full path on Windows.
     * @return {@code "AppData/Local"} folder on Windows.
     */
    private static String getLocSetAppDataPath() {
        final boolean addSep = !home.endsWith(File.separator);
        final StringBuilder sb = new StringBuilder(home.length() + WIN_LOCSET_APPDATA.length() + (addSep ? 1 : 0));
        sb.append(home);
        if (addSep) {
            sb.append(File.separator);
        }
        sb.append(WIN_LOCSET_APPDATA);
        return sb.toString();
    }        

    /**
     * Get application data directory for launcher on Windows systems.
     * @return Application data directory for launcher on UNIX systems.
     */
    private static String getAppDataWin() {
        // Try modern Windows path first.
        String appDataName = getAppDataLocalPath();
        File appDataPath = new File(appDataName);
        if (appDataPath.isDirectory()) {
            return FileUtils.fullPath(appDataName, gameDirName);
        }
        // Try old Windows path as a second option.
        appDataName = getLocSetAppDataPath();
        appDataPath = new File(appDataName);
        if (appDataPath.isDirectory()) {
            return FileUtils.fullPath(appDataName, gameDirName);
        }
        // Use UNIX style path as a fallback.
        return getAppDataUnix();
    }

    /**
     * Get application data directory for launcher.
     */
    private static String getAppData() {
        switch(OS.os) {
            case UNIX:
            case MAC: return getAppDataUnix();
            case WIN: return getAppDataWin();
            default:
                    throw new IllegalStateException("Unknown OS identifier");
       }
    }

    /**
     * Get application installation directory for game on UNIX systems.
     * This is just normal visible directory in user home directory.
     * @return Application installation directory for game on UNIX systems.
     */
    private static String getGameDataUnix() {
        final boolean addSep = !home.endsWith(File.separator);
        final StringBuilder sb = new StringBuilder(home.length() + gameDirName.length() + (addSep ? 1 : 0));
        sb.append(home);
        if (addSep) {
            sb.append(File.separator);
        }
        sb.append(gameDirName);
        return sb.toString();
    }

    /**
     * Get {@code "AppData/Roaming"} directory full path on Windows.
     * @return {@code "AppData/Roaming"} folder on Windows.
     */
    private static String getAppDataRoamingPath() {
        final boolean addSep = !home.endsWith(File.separator);
        final StringBuilder sb = new StringBuilder(home.length() + WIN_APPDATA_ROAMING.length() + (addSep ? 1 : 0));
        sb.append(home);
        if (addSep) {
            sb.append(File.separator);
        }
        sb.append(WIN_APPDATA_ROAMING);
        return sb.toString();
    }        

    /**
     * Get application installation directory for game on Windows systems.
     * @return Application installation directory for game on Windows systems.
     */
    private static String getGameDataWin() {
        // Try modern Windows path first.
        String appDataName = getAppDataRoamingPath();
        File appDataPath = new File(appDataName);
        if (appDataPath.isDirectory()) {
            return FileUtils.fullPath(appDataName, gameDirName);
        }
        // Use UNIX style path as a fallback.
        return getAppDataUnix();
    }

    /**
     * Get default application application installation directory for game.
     * @return Default application application installation directory for game.
     */
    public static String getDefaultGameDir() {
        switch(OS.os) {
            case UNIX:
            case MAC: return getGameDataUnix();
            case WIN: return getGameDataWin();
            default:
                    throw new IllegalStateException("Unknown OS identifier");
       }
    }

}
