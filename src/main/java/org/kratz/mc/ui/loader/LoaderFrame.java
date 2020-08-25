/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.ui.loader;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.kratz.mc.config.LoaderConfig;
import org.kratz.mc.init.LoaderInit;
import org.kratz.mc.init.Profile;
import org.kratz.mc.installer.DownloadBase;
import org.kratz.mc.installer.DownloadModules;
import org.kratz.mc.installer.DownloadProfile;
import org.kratz.mc.installer.DownloadProfiles;
import org.kratz.mc.installer.Downloader;
import org.kratz.mc.installer.GameCheck;
import org.kratz.mc.locale.Messages;
import org.kratz.mc.log.LogLevel;
import org.kratz.mc.log.Logger;
import org.kratz.mc.utils.FileUtils;
import org.kratz.mc.utils.OS;
import org.kratz.mc.utils.OSUtils;
import org.kratz.mc.utils.PasswordUtils;
import org.kratz.mc.utils.Version;

/**
 *
 * @author kratz
 */
public class LoaderFrame extends javax.swing.JFrame {

    static enum GameState {
        /** Game profile was not selected. */
        NO_PROFILE,
        /** Game installation path does not exist. */
        NO_PATH,
        /** Game base installation is missing or is broken. */
        INSTALL,
        /** Game modules need update. */
        MODULES,
        /** Game installation is OK. */
        OK;

        /**
         * Get game state depending on conditions.
         * @param profileExists Game profile exists.
         * @param pathExists    Game installation path exists.
         * @param gameCheck     Game base installation check passed.
         * @param modulesCheck  Game modules check passed.
         * @return Current game state depending on provided indicators.
         */
        private static GameState gameState(final boolean profileExists, final boolean pathExists, final boolean gameCheck, LinkedList<LoaderConfig.Mod> modsToFix) {
            if (profileExists) {
                if (pathExists) {
                    if (gameCheck) {
                        boolean modulesCheck = modsToFix != null ? modsToFix.isEmpty() : false;
                        if (modulesCheck) {
                            Logger.log(LogLevel.FINE, "Game state: %s", GameState.OK);
                            return GameState.OK;
                        } else {
                            Logger.log(LogLevel.FINE, "Game state: %s", GameState.MODULES);
                            return GameState.MODULES;
                        }
                    } else {
                        Logger.log(LogLevel.FINE, "Game state: %s", GameState.INSTALL);
                        return GameState.INSTALL;
                    }
                } else {
                    Logger.log(LogLevel.FINE, "Game state: %s", GameState.NO_PATH);
                    return GameState.NO_PATH;
                }
            } else {
                Logger.log(LogLevel.FINE, "Game state: %s", GameState.NO_PROFILE);
                return GameState.NO_PROFILE;
            }
        }

    };

    /** Logo image file packaged in JAR. */
    private static final String LOGO_FILE = "/ui/panel.png";

    /** Dark red color. */
    private static final Color DARK_RED = new java.awt.Color(0x68, 0, 0);

    /**
     * Retrieve decrypted password as {@link String}.
     * @return Decrypted password from loader initialization object.
     */
    private static String getPassword() {
        final String password = LoaderInit.getUserPassword();
        if (password == null) {
            return null;
        } else { 
            return new String(PasswordUtils.decrypt(password));
        }
    }

    /**
     * Load image bitmap.
     */
    private static BufferedImage readImage() {
        try {
            return ImageIO.read(LoaderFrame.class.getResourceAsStream(LOGO_FILE));
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Could not read image %s", LOGO_FILE);
            return null;        
        }
    }
    
    /**
     * Validate user name.
     * @return Value of {@code true} when user name is valid or {@code false} otherwise.
     */
    private static boolean checkUserName(final String userName) {
        return userName != null && userName.length() > 2;
    }

    /**
     * Validate user password length from encrypted password.
     * @return Value of {@code true} when user password is valid or {@code false} otherwise.
     */
    private static boolean checkUserPassword(final String userPassword) {
        if (userPassword == null) {
            return false;
        }
        final char[] password = PasswordUtils.decrypt(userPassword);
        final boolean valid = password != null && password.length > 2;
        Arrays.fill(password, '\u0000');
        return valid;
    }

    /**
     * Validate user password.
     * @return Value of {@code true} when user password is valid or {@code false} otherwise.
     */
    private static boolean checkPassword(final char[] passw) {
        return passw != null && passw.length > 2;
    }

    /**
     * Create and initialize directory chooser.
     * @returns Initialized directory chooser.
     */
    private static JFileChooser createDirectoryChooser() {
        final JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setDialogTitle(Messages.get("ui.dir.select.title"));
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooser.setAcceptAllFileFilterUsed(false);
        return directoryChooser;
    }

    /**
     * Returns HTTP proxy port to be set into form field.
     * @return HTTP proxy port to be set into form field.
     */
    private static String getHTTPProxyPort() {
        int port = LoaderInit.getHttpProxyPort();
        if (port >= 0) {
            return Integer.toString(port);
        }
        port = OS.getHTTPProxyPort();
        if (port >= 0) {
            return Integer.toString(port);
        }
        final String host = OS.getHTTPProxyHost();
        return host != null && host.length() > 0 ? "80" : "";
    }

    /**
     * Returns HTTP proxy host to be set into form field.
     * @return HTTP proxy host to be set into form field.
     */
    private static String getHTTPProxyHost() {
        String host = LoaderInit.getHttpProxyHost();
        if (host != null && host.length() > 0) {
            return host;
        }
        host = OS.getHTTPProxyHost();
        return host != null ? host : "";
    }

    /**
     * Returns HTTP proxy host to be used for profiles download during initialization.
     * @return HTTP proxy host to be used for profiles download during initialization.
     */
    private static int getHTTPProxyPortNum() {
        int port = LoaderInit.getHttpProxyPort();
        if (port >= 0) {
            return port;
        }
        port = OS.getHTTPProxyPort();
        return port >= 0 ? port : 80;
    }

    /** Loader initialization object. */
    final UiContext ctx;
    /** Game installation check. */
    final GameCheck check;
    /** Picture with game logo. */
    final BufferedImage logoPicure;

    final JFileChooser directoryChooser;

    /** Content of profiles select box. */
    Profile[] profilesContent;

    /**
     * Check and download profiles list when needed.
     */
    private void updateProfilesList() {
        File profiles = new File(LoaderInit.PROFILES_PATH);
        if (!profiles.exists() || (System.currentTimeMillis() - profiles.lastModified()) > 86400000L) {

            Downloader downloader = new DownloadProfiles(
                    LoaderInit.PROFILES_PATH, LoaderInit.PROFILES_URL,
                    new ProfilesDownloadListener(this), OSUtils.customHTTPProxy(getHTTPProxyHost(), getHTTPProxyPortNum()));
            downloader.start();
        }
    }

    private Profile[] buildProfilesContent() {
        LinkedList<Profile> profiles = LoaderInit.getProfiles();
        return profiles != null ? profiles.toArray(new Profile[profiles.size()]) : new Profile[0];
    }

    /** Game profile exists. Cache for checks without path modification. */
    boolean profileExists;
    /** Game installation path exists. Cache for checks without path modification. */
    boolean pathExists;
    /** Game installation OK for current path. Cache for checks without path modification. */
    boolean gameCheckCache;
    /** User name is OK for the game. Cache for checks without user modification. */
    boolean userCheckCache;
    /** User password is OK for the game. Cache for checks without user modification. */
    boolean passCheckCache;

    /** Mark UI initialization phase. */
    boolean isInit;

    /** Current game installation state depending on indicators. */
    GameState installationState;
    
    /** Profile download handler. */
    Downloader profileDownloader;

    /** Game components download handler. */
    private Downloader installer;

    /**
     * Creates a new instance of form LoaderFrame.
     * @param ctx UI context.
     */
    public LoaderFrame(final UiContext ctx) {
        isInit = true;
        this.ctx = ctx;
        check = new GameCheck();
        profilesContent = buildProfilesContent();
        profileExists = LoaderInit.getProfiles() != null && LoaderInit.getProfiles().size() > 0 && LoaderInit.getProfile() != null;
        pathExists = profileExists && check.checkInstallDir(LoaderInit.getPath());
        gameCheckCache = false;
        userCheckCache = checkUserName(LoaderInit.getUserName());
        passCheckCache = checkUserPassword(LoaderInit.getUserPassword());
        installationState = profileExists ? GameState.NO_PATH : GameState.NO_PROFILE;
        logoPicure = readImage();
        directoryChooser = createDirectoryChooser();
        Logger.log(LogLevel.FINE, "Game start is %senabled", startEnabled() ? "" : "not ");
        Logger.log(LogLevel.FINEST, "  Game path: %s", gameCheckCache ? "OK" : "Not OK ");
        Logger.log(LogLevel.FINEST, "  User name: %s", userCheckCache ? "OK" : "Not OK ");
        Logger.log(LogLevel.FINEST, "  Password:  %s", passCheckCache ? "OK" : "Not OK ");
        initComponents();
        if (installationState != GameState.NO_PROFILE) {
            final Profile profile = LoaderInit.getCurrentProfile();
            profileDownloader = new DownloadProfile(
                    LoaderInit.getCurrentConfigFile(profile), LoaderInit.getCurrentConfigURL(profile),
                    new ProfileDownloadListener(this), OSUtils.customHTTPProxy(getHTTPProxyHost(), getHTTPProxyPortNum()));
            profileDownloader.start();
        } else {
            profileDownloader = null;
        }
        updateProfilesSelectBox(false);
        profileDownloader = null;
        installer = null;
        updateGameComponentsVisibility();
        initDownloadComponents();
        // Switch logger to UI
        logText.setEditable(false);
        Logger.initUi(logText.getDocument());
    }

    /**
     * Build version label value.
     * @return Version label value.
     */
    private String versionLabel() {
        return "Minecraft Launcher " + Version.MAJOR + "." + Version.MINOR + "." + Version.PATCH + ", \u00A9 2018 Tomáš Kraus";
    }

    /**
     * Validates whether to enable {@code Start} button.
     * @return Value of {@code true} when {@code Start} button shall be enabled or {@code false} otherwise.
     */
    private boolean startEnabled() {
        return userCheckCache && passCheckCache && gameCheckCache && ctx.noModules();
    }

    /**
     * Validates whether to enable {@code Install} button.
     * @return Value of {@code true} when {@code Install} button shall be enabled or {@code false} otherwise.
     */
    private boolean installEnabled() {
        return LoaderConfig.isConfig() && installationState != GameState.OK && installer == null && profileDownloader == null;
    }

    /**
     * Validates whether to enable profiles downloading UI elements.
     * @return Value of {@code true} when profiles downloading UI elements shall be enabled or {@code false} otherwise.
     */
    private boolean profileDownloadEnabled() {
        return profileDownloader != null;
    }

    /**
     * Game path label color chooser.
     * @return Game path label color depending on user name validation.
     */
    private Color pathLabelColor() {
        return gameCheckCache ? Color.BLACK : DARK_RED;
    }

    /**
     * Profile label color chooser.
     * @return Profile label color depending on profile selection.
     */
    private Color profileLabelColor() {
        return profileExists ? Color.BLACK : DARK_RED;
    }

    /**
     * User name label color chooser.
     * @return User name label color depending on user name validation.
     */
    private Color userLabelColor() {
        return userCheckCache ? Color.BLACK : DARK_RED;
    }

    /**
     * User password label color chooser.
     * @return User password label color depending on user name validation.
     */
    private Color passwLabelColor() {
        return passCheckCache ? Color.BLACK : DARK_RED;
    }

    /**
     * Modules status label color chooser.
     * @return User password label color depending on user name validation.
     */
    private Color modulesStatusColor() {
        return ctx.modsToFix == null ? DARK_RED : (ctx.modsToFix.isEmpty() ? Color.BLACK : DARK_RED);
    }

    /**
     * Install button label
     */
    private String installButtonLabel() {
        switch(installationState) {
            case NO_PROFILE:
            case NO_PATH:
            case INSTALL:
                return Messages.get("ui.dir.button.install.base");
            case MODULES:
            case OK:
                return Messages.get("ui.dir.button.install.modules");
            default: throw new IllegalStateException("Unknown game installation state");
        }
    }

    /**
     * Modules status message.
     * @return UI message depending on current modules status.
     */
    private String gameStatusMesage() {
        switch (installationState) {
            case NO_PROFILE: return Messages.get("ui.status.noProfile");
            case NO_PATH: return Messages.get("ui.status.noPath");
            case INSTALL: return Messages.get("ui.status.install");
            case MODULES: return Messages.get("ui.status.modules");
            case OK: return Messages.get("ui.status.ok");
            default: throw new IllegalStateException("Unknown game installation state");
        }
    }

    /**
     * Reset download status and download label values.
     */
    void resetDownloadUI() {
        downloadProgress.setValue(0);
        downloadLabel.setText("");
    }

    /**
     * Update profiles select box.
     * @param updateContent Whether to update select box content.
     */
    final void updateProfilesSelectBox(final boolean updateContent) {
        if (updateContent) {
            profilesContent = buildProfilesContent();
            profilesBox.setEnabled(true);
        } else {
            profilesBox.setEnabled(false);
        }
        profilesBox.removeAllItems();
        isInit = false;
        if (profilesContent != null) {
            String current = LoaderInit.getProfile();
            for (Profile profile : profilesContent) {
                profilesBox.addItem(profile);
                if (current != null && current.equalsIgnoreCase(profile.getDirectory())) {
                    profilesBox.setSelectedItem(profile);
                }
            }
        }
    }

    /**
     * Update UI status after module download finished.
     */
    void updateGameStatusForBase() {
        final String gamePath = path.getText();
        profileExists = LoaderInit.getProfile() != null;
        pathExists = profileExists && check.checkInstallDir(gamePath);
        gameCheckCache = pathExists && gamePath != null && check.check(gamePath);
        if (LoaderConfig.isConfig()) {
            ctx.checkModules(path.getText());
        }
        installationState = GameState.gameState(profileExists, pathExists, gameCheckCache, ctx.modsToFix);
    }

    /**
     * Update UI status after module download finished.
     */
    void updateGameStatusForModules() {
        ctx.checkModules(path.getText());
        installationState = GameState.gameState(profileExists, pathExists, gameCheckCache, ctx.modsToFix);
    }

    /**
     * Update UI content after module download is finished.
     * @param mod Finished module.
     */
    void moduleDownloadFinished(LoaderConfig.Mod mod) {
        ctx.removeModToFix(mod);
    }

    /**
     * Generates content of "modules to install" list.
     * @param document UI list document.
     * @return String containing list of modules to be installed.
     */
    private void setModulesToInstall(final Document document) throws BadLocationException {
        Logger.log(LogLevel.INFO, "Building modules list for state: %s.", installationState.name());
        switch(installationState) {
            case NO_PROFILE:
                return;
            case NO_PATH:
                document.insertString(document.getLength(), " Base game", null);
                return;
            case INSTALL:
                document.insertString(document.getLength(), " Base game", null);
                return;
            case MODULES:
                if (ctx.isModules()) {
                    boolean first = true;
                    for (LoaderConfig.Mod mod : ctx.modsToFix) {
                        if (first) {
                            first = false;
                        } else {
                            document.insertString(document.getLength(), "\n", null);
                        }
                        document.insertString(document.getLength(), " ", null);
                        int from = mod.getFile().lastIndexOf(File.separatorChar);
                        if (from >= 0) {
                            document.insertString(document.getLength(), mod.getFile().substring(from + 1), null);
                        } else {
                            document.insertString(document.getLength(), mod.getFile(), null);
                        }
                    }
                } else {
                    return;
                }
            case OK:
                return;
            default: throw new IllegalStateException("Unknown game installation state");
        }
    }

    /**
     * Reset {@link Downloader} component.
     * Sets component to {@code null}.
     */
    void resetInstaller() {
        installer = null;
    }

    /**
     * Builds proxy instance from form fields.
     * @return Proxy instance from form fields or <code>null</code> if no proxy instance was found.
     */
    private Proxy getProxy() {
        final String host = getProxyHost();
        int port = getProxyPort();
        if (host != null && port >= 0 && port <= 65535) {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        } else {
            return null;
        }
    }

    /**
     * Builds proxy host instance from form field.
     * @return Proxy host instance from form field.
     */
    private String getProxyHost() {
        final String host = proxyHost.getText();
        return host != null && host.length() > 0 ? host : null;
    }
    
    /**
     * Builds proxy host instance from form field.
     * @return Proxy host instance from form field.
     */
    private int getProxyPort() {
        final String portStr = proxyPort.getText();
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            port = -1;
        }
        return port <= 65535 ? port : -1;
    }

    private void updateModulesList() {
        try {
            final Document modules = downloadModsList.getDocument();
            modules.remove(0, modules.getLength());
            setModulesToInstall(modules);
        } catch (BadLocationException ex) {
            Logger.log(LogLevel.WARNING, "Error updating modules list: %s", ex.getLocalizedMessage());
        }
    }
    /**
     * Initialize {@link Downloader} component depending on current game installation state.
     * @return {@link Downloader} component depending on current game installation state.
     */
    private Downloader initDownloader() {
        switch(installationState) {
            case NO_PATH:
                    if (FileUtils.mkDirs(new File(path.getText()))) {
                        installationState = GameState.gameState(profileExists, pathExists, gameCheckCache, ctx.modsToFix);
                        updateModulesList();
                    }
            case INSTALL: return new DownloadBase(
                    path.getText(), LoaderConfig.getGameUrl(), new BaseDownloadListener(this), getProxy());
            case MODULES: return new DownloadModules(
                    path.getText(), LoaderConfig.getModsPath(), ctx.modsToFix, delUnreg.isSelected(),
                    new ModuleDownloadListener(this), getProxy());
            case NO_PROFILE:
            case OK: return null;
            default: throw new IllegalStateException("Unknown game installation state");
        }
    }

    final void updateGameComponentsVisibility() {
        updateProfilesList();
        updateModulesList();
        gameState.setForeground(modulesStatusColor());
        gameState.setText(gameStatusMesage());
        switch (installationState) {
            case NO_PROFILE:
                LinkedList<Profile> profiles = LoaderInit.getProfiles();
                boolean profileBoxEnabled = profiles != null && profiles.size() > 0;
                userNameLabel.setEnabled(false);
                userNameLabel.setVisible(false);
                userName.setEnabled(false);
                userName.setVisible(false);
                passwordLabel.setEnabled(false);
                passwordLabel.setVisible(false);
                password.setEnabled(false);
                password.setVisible(false);
                profileLabel.setEnabled(profileBoxEnabled);
                profileLabel.setVisible(profileBoxEnabled);
                profilesBox.setEnabled(profileBoxEnabled);
                profilesBox.setVisible(profileBoxEnabled);
                buttonStart.setEnabled(false);
                buttonInstall.setEnabled(false);
                downloadModsList.setEnabled(false);
                break;
            case INSTALL:
                userNameLabel.setEnabled(false);
                userNameLabel.setVisible(true);
                userName.setEnabled(false);
                userName.setVisible(true);
                passwordLabel.setEnabled(false);
                passwordLabel.setVisible(true);
                password.setEnabled(false);
                password.setVisible(true);
                profileLabel.setEnabled(true);
                profileLabel.setVisible(true);
                profilesBox.setEnabled(true);
                profilesBox.setVisible(true);
                buttonStart.setEnabled(startEnabled());
                buttonInstall.setEnabled(installEnabled());
                downloadModsList.setVisible(true);
                downloadModsList.setEnabled(true);
                break;
            case MODULES:
                userNameLabel.setEnabled(false);
                userNameLabel.setVisible(true);
                userName.setEnabled(false);
                userName.setVisible(true);
                passwordLabel.setEnabled(false);
                passwordLabel.setVisible(true);
                password.setEnabled(false);
                password.setVisible(true);
                profileLabel.setEnabled(true);
                profileLabel.setVisible(true);
                profilesBox.setEnabled(true);
                profilesBox.setVisible(true);
                buttonStart.setEnabled(startEnabled());
                buttonInstall.setEnabled(installEnabled());
                downloadModsList.setVisible(true);
                downloadModsList.setEnabled(true);
                break;
            case NO_PATH:
                userNameLabel.setEnabled(false);
                userNameLabel.setVisible(true);
                userName.setEnabled(false);
                userName.setVisible(true);
                passwordLabel.setEnabled(false);
                passwordLabel.setVisible(true);
                password.setEnabled(false);
                password.setVisible(true);
                profileLabel.setEnabled(true);
                profileLabel.setVisible(true);
                profilesBox.setEnabled(true);
                profilesBox.setVisible(true);
                buttonStart.setEnabled(startEnabled());
                buttonInstall.setEnabled(installEnabled());
                downloadModsList.setVisible(true);
                downloadModsList.setEnabled(true);
                break;
            case OK:
                userNameLabel.setEnabled(true);
                userNameLabel.setVisible(true);
                userName.setEnabled(true);
                userName.setVisible(true);
                passwordLabel.setEnabled(true);
                passwordLabel.setVisible(true);
                password.setEnabled(true);
                password.setVisible(true);
                profileLabel.setEnabled(true);
                profileLabel.setVisible(true);
                profilesBox.setEnabled(true);
                profilesBox.setVisible(true);
                buttonStart.setEnabled(startEnabled());
                buttonInstall.setEnabled(installEnabled());
                downloadModsList.setVisible(false);
                downloadModsList.setEnabled(false);
                break;
            default:
                throw new IllegalStateException("Unknown game installation state");
        }
        profileProgressLabel.setVisible(profileDownloadEnabled());
        profileProgressLabel.setEnabled(profileDownloadEnabled());
        profleProgress.setVisible(profileDownloadEnabled());
        profleProgress.setEnabled(profileDownloadEnabled());
        profileLabel.setForeground(profileLabelColor());
        pathLabel.setForeground(pathLabelColor());
        buttonInstall.setText(installButtonLabel());
    }

    /**
     * Hide downloading progress UI elements.
     */
    private void initDownloadComponents() {
        downloadLabel.setEnabled(false);
        downloadProgress.setEnabled(false);
        downloadLabel.setVisible(true);
        downloadProgress.setVisible(false);
        if (ctx.modsToFix == null || ctx.modsToFix.isEmpty()) {
            downloadModsList.setEnabled(false);
            downloadModsList.setVisible(false);
        } else {
            downloadModsList.setEnabled(true);
            downloadModsList.setVisible(true);
        }
        buttonInstall.setEnabled(installEnabled());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings({"unchecked","deprecation"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        userName = new javax.swing.JTextField();
        userNameLabel = new javax.swing.JLabel();
        buttonStart = new javax.swing.JButton();
        passwordLabel = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        tabs = new javax.swing.JTabbedPane();
        game = new javax.swing.JPanel();
        picture = new javax.swing.JLabel(new ImageIcon(logoPicure));
        versionLabel = new javax.swing.JLabel();
        install = new javax.swing.JPanel();
        downloadLabel = new javax.swing.JLabel();
        downloadProgress = new javax.swing.JProgressBar();
        buttonInstall = new javax.swing.JButton();
        modulesList = new javax.swing.JScrollPane();
        downloadModsList = new javax.swing.JTextPane();
        pathLabel = new javax.swing.JLabel();
        path = new javax.swing.JTextField();
        selectPath = new javax.swing.JButton();
        proxyMainLabel = new javax.swing.JLabel();
        proxyHostLabel = new javax.swing.JLabel();
        proxyPortLabel = new javax.swing.JLabel();
        proxyHost = new javax.swing.JTextField();
        proxyPort = new javax.swing.JTextField();
        delUnreg = new javax.swing.JCheckBox();
        log = new javax.swing.JPanel();
        logPane = new javax.swing.JScrollPane();
        logText = new javax.swing.JTextPane();
        gameState = new javax.swing.JLabel();
        exitCheckBox = new javax.swing.JCheckBox();
        profileLabel = new javax.swing.JLabel();
        profilesBox = new javax.swing.JComboBox<>(profilesContent);
        profleProgress = new javax.swing.JProgressBar();
        profileProgressLabel = new javax.swing.JLabel();

        jScrollPane1.setViewportView(jTextPane1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(800, 600));

        userName.setText(LoaderInit.getUserName());
        userName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkUserChange(evt);
            }
        });

        userNameLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        userNameLabel.setForeground(userLabelColor());
        userNameLabel.setText(Messages.get("ui.label.userName"));

        buttonStart.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        buttonStart.setText(Messages.get("ui.button.start"));
        buttonStart.setEnabled(startEnabled());
        buttonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStartActionPerformed(evt);
            }
        });

        passwordLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        passwordLabel.setForeground(passwLabelColor());
        passwordLabel.setText(Messages.get("ui.label.password"));

        password.setText(getPassword());
        password.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkPasswordChange(evt);
            }
        });

        tabs.setPreferredSize(new java.awt.Dimension(800, 600));

        picture.setBackground(new java.awt.Color(0, 0, 0));

        versionLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        versionLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        versionLabel.setText(versionLabel());

        javax.swing.GroupLayout gameLayout = new javax.swing.GroupLayout(game);
        game.setLayout(gameLayout);
        gameLayout.setHorizontalGroup(
            gameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(picture, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(gameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(versionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
                .addContainerGap())
        );
        gameLayout.setVerticalGroup(
            gameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gameLayout.createSequentialGroup()
                .addComponent(picture, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionLabel)
                .addContainerGap())
        );

        tabs.addTab("Game", game);

        downloadLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        downloadLabel.setEnabled(false);

        downloadProgress.setEnabled(false);

        buttonInstall.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        buttonInstall.setText(installButtonLabel());
        buttonInstall.setEnabled(false);
        buttonInstall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonInstallActionPerformed(evt);
            }
        });

        modulesList.setFocusTraversalKeysEnabled(false);
        modulesList.setFocusable(false);

        downloadModsList.setEditable(false);
        downloadModsList.setBackground(new java.awt.Color(238, 238, 238));
        downloadModsList.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        downloadModsList.setFocusable(false);
        downloadModsList.setMaximumSize(new java.awt.Dimension(350, 600));
        downloadModsList.setMinimumSize(new java.awt.Dimension(350, 300));
        downloadModsList.setPreferredSize(new java.awt.Dimension(350, 314));
        modulesList.setViewportView(downloadModsList);

        pathLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        pathLabel.setForeground(pathLabelColor());
        pathLabel.setText(Messages.get("ui.dir.label.path"));

        path.setText(LoaderInit.getPath());
        path.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkPathChange(evt);
            }
        });

        selectPath.setText(Messages.get("ui.dir.button.select"));
        selectPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectActionPerformed(evt);
            }
        });

        proxyMainLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        proxyMainLabel.setText(Messages.get("ui.proxy.label.main"));

        proxyHostLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        proxyHostLabel.setText(Messages.get("ui.proxy.label.host"));

        proxyPortLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        proxyPortLabel.setText(Messages.get("ui.proxy.label.port"));

        proxyHost.setText(getHTTPProxyHost());

        proxyPort.setText(getHTTPProxyPort());

        delUnreg.setSelected(true);
        delUnreg.setText(Messages.get("ui.checkbox.delete.modules"));

        javax.swing.GroupLayout installLayout = new javax.swing.GroupLayout(install);
        install.setLayout(installLayout);
        installLayout.setHorizontalGroup(
            installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(installLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(installLayout.createSequentialGroup()
                        .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(downloadLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(downloadProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, installLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(delUnreg)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonInstall, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(proxyMainLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(installLayout.createSequentialGroup()
                                .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(proxyHostLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(proxyPortLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(proxyHost)
                                    .addComponent(proxyPort))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modulesList, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(installLayout.createSequentialGroup()
                        .addComponent(pathLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(path)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectPath)))
                .addContainerGap())
        );
        installLayout.setVerticalGroup(
            installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(installLayout.createSequentialGroup()
                .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pathLabel)
                    .addComponent(selectPath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(installLayout.createSequentialGroup()
                        .addComponent(downloadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downloadProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(buttonInstall)
                            .addComponent(delUnreg))
                        .addGap(18, 18, 18)
                        .addComponent(proxyMainLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(proxyHostLabel)
                            .addComponent(proxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(proxyPortLabel)
                            .addComponent(proxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(modulesList, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabs.addTab("Install", install);

        logText.setFont(new java.awt.Font("Courier", 0, 12)); // NOI18N
        logPane.setViewportView(logText);

        javax.swing.GroupLayout logLayout = new javax.swing.GroupLayout(log);
        log.setLayout(logLayout);
        logLayout.setHorizontalGroup(
            logLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(logPane, javax.swing.GroupLayout.DEFAULT_SIZE, 773, Short.MAX_VALUE)
        );
        logLayout.setVerticalGroup(
            logLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(logPane, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
        );

        tabs.addTab("Log", log);

        gameState.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        gameState.setForeground(modulesStatusColor());
        gameState.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gameState.setText(gameStatusMesage());

        exitCheckBox.setSelected(true);
        exitCheckBox.setText(Messages.get("ui.label.exit"));

        profileLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        profileLabel.setForeground(profileLabelColor());
        profileLabel.setText(Messages.get("ui.label.profiles"));

        profilesBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileBoxActionPerformed(evt);
            }
        });

        profileProgressLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        profileProgressLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(userNameLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(profileLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(passwordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(password)
                            .addComponent(profilesBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(userName))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(exitCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonStart))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(profileProgressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(profleProgress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(gameState, javax.swing.GroupLayout.PREFERRED_SIZE, 475, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(profileLabel)
                        .addComponent(profilesBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(profileProgressLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(profleProgress, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameState))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(buttonStart)
                        .addComponent(passwordLabel)
                        .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(exitCheckBox))
                .addContainerGap())
        );

        buttonStart.getAccessibleContext().setAccessibleDescription("");
        tabs.getAccessibleContext().setAccessibleName("Game");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStartActionPerformed
        final String newUserName = userName.getText();
        if (!newUserName.equals(LoaderInit.getUserName())) {
            LoaderInit.updateUserName(newUserName);
        }
        final String newPassword = PasswordUtils.encrypt(password.getPassword());
        if (!newPassword.equals(LoaderInit.getUserPassword())) {
            LoaderInit.updateUserPassword(newPassword);
        }
        final String newPath = path.getText();
        if (!newPath.equals(LoaderInit.getPath())) {
            LoaderInit.updatePath(newPath);
        }
        final String localProxyHost = getProxyHost();
        int localProxyPort = getProxyPort();
        if (localProxyHost != null && localProxyPort >= 0 && localProxyPort <= 65535) {
           LoaderInit.updateHttpProxyHost(localProxyHost);
           LoaderInit.updateHttpProxyPort(localProxyPort);
        }
        ctx.exitLauncher = exitCheckBox.isSelected();
        this.setVisible(false);
        this.dispose();
        Logger.closeUi();
        ctx.wakeUp();
    }//GEN-LAST:event_buttonStartActionPerformed

    private void checkPathChange(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_checkPathChange
        final String gamePath = path.getText();
        pathExists = check.checkInstallDir(gamePath);
        gameCheckCache = pathExists && gamePath != null && check.check(gamePath);
        installationState = GameState.gameState(profileExists, pathExists, gameCheckCache, ctx.modsToFix);
        if (installationState == GameState.OK || installationState == GameState.MODULES) {
            ctx.checkModules(gamePath);
            installationState = GameState.gameState(profileExists, pathExists, gameCheckCache, ctx.modsToFix);
        }
        pathLabel.setForeground(pathLabelColor());
        gameState.setForeground(modulesStatusColor());
        gameState.setText(gameStatusMesage());
        buttonStart.setEnabled(startEnabled());
        buttonInstall.setEnabled(installEnabled());
        downloadModsList.setVisible(installEnabled());
        downloadModsList.setEnabled(installEnabled());
        try {
            setModulesToInstall(downloadModsList.getDocument());
        } catch (BadLocationException ex) {
            Logger.log(LogLevel.WARNING, "Error updating modules list: %s", ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_checkPathChange

    private void checkUserChange(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_checkUserChange
        userCheckCache = checkUserName(userName.getText());
        userNameLabel.setForeground(userLabelColor());
        buttonStart.setEnabled(startEnabled());
    }//GEN-LAST:event_checkUserChange

    private void checkPasswordChange(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_checkPasswordChange
        passCheckCache = checkPassword(password.getPassword());
        passwordLabel.setForeground(passwLabelColor());
        buttonStart.setEnabled(startEnabled());
    }//GEN-LAST:event_checkPasswordChange

    private void buttonInstallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonInstallActionPerformed
        installer = initDownloader();
        installer.start();
    }//GEN-LAST:event_buttonInstallActionPerformed

    private void buttonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectActionPerformed
        directoryChooser.setCurrentDirectory(new File(path.getText()));
        final int status = directoryChooser.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            File directory = directoryChooser.getSelectedFile();
            if (directory != null && directory.isDirectory()) {
                String dirStr = directory.getAbsolutePath();
                path.setText(dirStr);
                pathExists = check.checkInstallDir(dirStr);
                gameCheckCache = pathExists && dirStr != null && check.check(dirStr);
                installationState = GameState.gameState(profileExists, pathExists, gameCheckCache, ctx.modsToFix);
                pathLabel.setForeground(pathLabelColor());
                buttonStart.setEnabled(startEnabled());
                gameState.setForeground(modulesStatusColor());
                gameState.setText(gameStatusMesage());
            }
        }
    }//GEN-LAST:event_buttonSelectActionPerformed

    private void profileBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileBoxActionPerformed
        if (!isInit) {
            @SuppressWarnings("unchecked")
            final javax.swing.JComboBox<Profile> cb = (javax.swing.JComboBox<Profile>) evt.getSource();
            final Profile selected = (Profile) cb.getSelectedItem();
            if (selected != null) {
                final String current = LoaderInit.getProfile();
                final String gameDir = selected.getDirectory();
                if (current == null || !current.equalsIgnoreCase(gameDir)) {
                    LoaderInit.updateProfile(gameDir);
                    final String gamePath = OS.getGameDir(gameDir);
                    LoaderInit.updatePath(gamePath);
                    profileExists = LoaderInit.getProfile() != null;
                    path.setText(gamePath);
                    pathExists = check.checkInstallDir(gamePath);
                    gameCheckCache = pathExists && gamePath != null && check.check(gamePath);
                    installationState = GameState.gameState(profileExists, pathExists, gameCheckCache, ctx.modsToFix);
                    pathLabel.setForeground(pathLabelColor());
                    buttonStart.setEnabled(startEnabled());
                    gameState.setForeground(modulesStatusColor());
                    gameState.setText(gameStatusMesage());
                    updateGameComponentsVisibility();
                    final Profile profile = LoaderInit.getCurrentProfile();
                    profileDownloader = new DownloadProfile(
                            LoaderInit.getCurrentConfigFile(profile), LoaderInit.getCurrentConfigURL(profile),
                            new ProfileDownloadListener(this), getProxy());
                    profileDownloader.start();
                }
            }
        }
    }//GEN-LAST:event_profileBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton buttonInstall;
    private javax.swing.JButton buttonStart;
    private javax.swing.JCheckBox delUnreg;
    javax.swing.JLabel downloadLabel;
    protected javax.swing.JTextPane downloadModsList;
    javax.swing.JProgressBar downloadProgress;
    private javax.swing.JCheckBox exitCheckBox;
    private javax.swing.JPanel game;
    private javax.swing.JLabel gameState;
    private javax.swing.JPanel install;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JPanel log;
    private javax.swing.JScrollPane logPane;
    private javax.swing.JTextPane logText;
    private javax.swing.JScrollPane modulesList;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField path;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel picture;
    javax.swing.JLabel profileLabel;
    javax.swing.JLabel profileProgressLabel;
    javax.swing.JComboBox<Profile> profilesBox;
    javax.swing.JProgressBar profleProgress;
    private javax.swing.JTextField proxyHost;
    private javax.swing.JLabel proxyHostLabel;
    private javax.swing.JLabel proxyMainLabel;
    private javax.swing.JTextField proxyPort;
    private javax.swing.JLabel proxyPortLabel;
    private javax.swing.JButton selectPath;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTextField userName;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}
