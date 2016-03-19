/*
 * (C) 2016 Tomas Kraus
 */
package mc.launcher;

import java.util.LinkedList;

/**
 * Main class invocation arguments.
 */
public class Arguments {

    /** Key prefix. */
    private static final String KEY_PREFIX = "--";

    /** User name key. */
    private static final String USERNAME_KEY="username";
    /** Session token key. */
    private static final String SESSION_KEY="session";
    /** Forge version key. */
    private static final String VERSION_KEY="version";
    /** Game directory key. */
    private static final String GAME_DIR_KEY="gameDir";
    /** Assets directory key. */
    private static final String ASSET_DIR_KEY="assetsDir";
    /** Tweak class key. */
    private static final String TWEAK_CLASS_KEY="tweakClass";
    /** Window title key. */
    private static final String TITLE_KEY="title";
    /** Window width key. */
    private static final String WIDTH_KEY="width";
    /** Window height key. */
    private static final String HEIGHT_KEY="height";
    /** Dock icon key. */
    private static final String ICON_KEY="icon";

    /** User name. */
    private final String username;
    /** Session token. */
    private final String sessionToken;
    /** Forge version. */
    private final String forgeVersion;
    /** Game directory. */
    private final String gameDir;
    /** Assets directory. */
    private final String assetsDir;
    /** Tweak class. */
    private final String tweakClass;
    /** Window title. */
    private final String title;
    /** Window width. */
    private final String width;
    /** Window height. */
    private final String height;
    /** Dock icon. */
    private final String icon;

    /**
     * Creates an instance of Main class invocation arguments.
     * @param username
     * @param sessionToken
     * @param forgeVersion
     * @param gameDir
     * @param assetsDir
     * @param tweakClass
     * @param title
     * @param width
     * @param height
     * @param icon 
     */
    public Arguments(
            final String username, final String sessionToken, final String forgeVersion,
            final String gameDir, final String assetsDir, final String tweakClass,
            final String title, final String width, final String height, final String icon
    ) {
        this.username = username;
        this.sessionToken = sessionToken;
        this.forgeVersion = forgeVersion;
        this.gameDir = gameDir;
        this.assetsDir = assetsDir;
        this.tweakClass = tweakClass;
        this.title = title;
        this.width = width;
        this.height = height;
        this.icon = icon;
    }

    private static void addArgToList(final LinkedList<String> args, final String key, final String value) {
        StringBuilder sb = new StringBuilder(KEY_PREFIX.length() + key.length());
        sb.append(KEY_PREFIX);
        sb.append(key);
        args.add(sb.toString());
        args.add(value);
        System.out.print(sb.toString());
        System.out.print(' ');
        System.out.println(value);
    }

    public String[] get() {
        LinkedList<String> argList = new LinkedList<>();
        addArgToList(argList, USERNAME_KEY, username);
        addArgToList(argList, SESSION_KEY, sessionToken);
        addArgToList(argList, VERSION_KEY, forgeVersion);
        addArgToList(argList, GAME_DIR_KEY, gameDir);
        addArgToList(argList, ASSET_DIR_KEY, assetsDir);
        addArgToList(argList, TWEAK_CLASS_KEY, tweakClass);
        addArgToList(argList, TITLE_KEY, title);
        addArgToList(argList, WIDTH_KEY, width);
        addArgToList(argList, HEIGHT_KEY, height);
        addArgToList(argList, ICON_KEY, icon);
        String[] args = new String[argList.size()];
        int i = 0;
        for (String arg : argList) {
            args[i++] = arg;
        }
        return args;
    }

    public String getAsString() {
        LinkedList<String> argList = new LinkedList<>();
        addArgToList(argList, USERNAME_KEY, username);
        addArgToList(argList, SESSION_KEY, sessionToken);
        addArgToList(argList, VERSION_KEY, forgeVersion);
        addArgToList(argList, GAME_DIR_KEY, gameDir);
        addArgToList(argList, ASSET_DIR_KEY, assetsDir);
        addArgToList(argList, TWEAK_CLASS_KEY, tweakClass);
        addArgToList(argList, TITLE_KEY, title);
        addArgToList(argList, WIDTH_KEY, width);
        addArgToList(argList, HEIGHT_KEY, height);
        addArgToList(argList, ICON_KEY, icon);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String arg : argList) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(arg);
        }
        return sb.toString();
    }

}
