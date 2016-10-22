/*
 * (C) 2016 Tomas Kraus
 */
package mc.ui.loader;

import mc.installer.GameCheck;
import mc.log.LogLevel;
import mc.log.Logger;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import mc.config.LoaderConfig;
import mc.init.LoaderInit;
import mc.installer.DownloadBase;
import mc.installer.DownloadModules;
import mc.installer.Downloader;
import mc.utils.PasswordUtils;
import mc.utils.Version;

/**
 *
 * @author kratz
 */
public class LoaderFrame extends javax.swing.JFrame {

    private static enum GameState {
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
         * @param pathExists   Game installation path exists.
         * @param gameCheck    Game base installation check passed.
         * @param modulesCheck Game modules check passed.
         * @return Current game state depending on provided indicators.
         */
        private static GameState gameState(final boolean pathExists, final boolean gameCheck, final boolean modulesCheck) {
            if (pathExists) {
                if (gameCheck) {
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
        }
    };

    /** Logo image file packaged in JAR. */
    private static final String LOGO_FILE = "/mc/ui/loader/thaumcraft.png";

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
        directoryChooser.setDialogTitle("Select Game installation directory");
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooser.setAcceptAllFileFilterUsed(false);
        return directoryChooser;
    }

    /** Loader initialization object. */
    final UiContext ctx;
    /** Game installation check. */
    final GameCheck check;
    /** Picture with game logo. */
    final BufferedImage logoPicure;

    final JFileChooser directoryChooser;

    /** Download change listener. */
//    final DownloadListener downloadListener;

    /** Game installation path exists. Cache for checks without path modification. */
    boolean pathExists;
    /** Game installation OK for current path. Cache for checks without path modification. */
    boolean gameCheckCache;
    /** User name is OK for the game. Cache for checks without user modification. */
    boolean userCheckCache;
    /** User password is OK for the game. Cache for checks without user modification. */
    boolean passCheckCache;

    /** Current game installation state depending on indicators. */
    GameState installationState;
    
    /** Game components download handler. */
    private Downloader installer;

    /**
     * Creates a new instance of form LoaderFrame.
     * @param ctx UI context.
     */
    public LoaderFrame(final UiContext ctx) {
        this.ctx = ctx;
        check = new GameCheck();
        pathExists = check.checkInstallDir(LoaderInit.getPath());
        gameCheckCache = pathExists && LoaderInit.getPath() != null && check.check(LoaderInit.getPath());
        userCheckCache = checkUserName(LoaderInit.getUserName());
        passCheckCache = checkUserPassword(LoaderInit.getUserPassword());
        installationState = GameState.gameState(pathExists, gameCheckCache, ctx.modsToFix.isEmpty());
        logoPicure = readImage();
        directoryChooser = createDirectoryChooser();
        Logger.log(LogLevel.FINE, "Game start is %senabled", startEnabled() ? "" : "not ");
        Logger.log(LogLevel.FINEST, "  Game path: %s", gameCheckCache ? "OK" : "Not OK ");
        Logger.log(LogLevel.FINEST, "  User name: %s", userCheckCache ? "OK" : "Not OK ");
        Logger.log(LogLevel.FINEST, "  Password:  %s", passCheckCache ? "OK" : "Not OK ");
        initComponents();
//        downloadListener = new ModuleDownloadListener(this);
        installer = null;
        initDownloadComponents();
    }

    /**
     * Build version label value.
     * @return Version label value.
     */
    private String versionLabel() {
        return "Minecraft Launcher " + Version.MAJOR + "." + Version.MINOR + ", \u00A9 2016 Tomáš Kraus";
    }

    /**
     * Validates whether to enable {@code Start} button.
     * @return Value of {@code true} when {@code Start} button shall be enabled or {@code false} otherwise.
     */
    private boolean startEnabled() {
        return userCheckCache && passCheckCache && gameCheckCache && ctx.modsToFix.isEmpty();
    }

    /**
     * Validates whether to enable {@code Install} button.
     */
    private boolean installEnabled() {
        return installationState != GameState.OK && installer == null;
    }

     /**
     * Game path label color chooser.
     * @return Game path label color depending on user name validation.
     */
    private Color pathLabelColor() {
        return gameCheckCache ? Color.BLACK : DARK_RED;
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
        return ctx.modsToFix.isEmpty() ? Color.BLACK : DARK_RED;
    }

    /**
     * Modules status message.
     * @return UI message depending on current modules status.
     */
    private String gameStatusMesage() {
        switch (installationState) {
            case NO_PATH: return "Game directory does not exist.";
            case INSTALL: return "Game needs reinstallation.";
            case MODULES: return "Modules update required. Go to Install tab";
            case OK: return "Game is OK";
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
     * Update UI status after module download finished.
     */
    void updateGameStatusForBase() {
        final String gamePath = path.getText();
        pathExists = check.checkInstallDir(gamePath);
        gameCheckCache = pathExists && gamePath != null && check.check(gamePath);
        ctx.checkModules(path.getText());
        installationState = GameState.gameState(pathExists, gameCheckCache, ctx.modsToFix.isEmpty());
        gameState.setForeground(modulesStatusColor());
        gameState.setText(gameStatusMesage());
        buttonStart.setEnabled(startEnabled());
        buttonInstall.setEnabled(installEnabled());
        downloadModsList.setText(modulesToInstallText());
    }

    /**
     * Update UI status after module download finished.
     */
    void updateGameStatusForModules() {
        ctx.checkModules(path.getText());
        installationState = GameState.gameState(pathExists, gameCheckCache, ctx.modsToFix.isEmpty());
        gameState.setForeground(modulesStatusColor());
        gameState.setText(gameStatusMesage());
        buttonStart.setEnabled(startEnabled());
        buttonInstall.setEnabled(installEnabled());
        downloadModsList.setText(modulesToInstallText());
    }
    
    /**
     * Update UI content after module download is finished.
     * @param mod Finished module.
     */
    void moduleDownloadFinished(LoaderConfig.Mod mod) {
        ctx.removeModToFix(mod);
        downloadModsList.setText(modulesToInstallText());
    }

    /**
     * Generates content of "modules to install" list.
     * @return String containing list of modules to be installed.
     */
    private String modulesToInstallText() {
        switch(installationState) {
            case NO_PATH:
                return "";
            case INSTALL:
                return "Base game";
            case MODULES:
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (LoaderConfig.Mod mod : ctx.modsToFix) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append('\n');
                    }
                    sb.append(' ');
                    int from = mod.getFile().lastIndexOf(File.separatorChar);
                    if (from >= 0) {
                        sb.append(mod.getFile().substring(from+1));
                    } else {
                        sb.append(mod.getFile());
                    }
                }
                return sb.toString();
            case OK:
                return "";
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
     * Initialize {@link Downloader} component depending on current game installation state.
     * @return {@link Downloader} component depending on current game installation state.
     */
    private Downloader initDownloader() {
        switch(installationState) {
            case NO_PATH:
            case INSTALL: return new DownloadBase(
                    path.getText(), LoaderConfig.getGameUrl(), new BaseDownloadListener(this));
            case MODULES: return new DownloadModules(
                    path.getText(), LoaderConfig.getModsPath(), ctx.modsToFix, new ModuleDownloadListener(this));
            case OK: return null;
            default: throw new IllegalStateException("Unknown game installation state");
        }
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
        gameState = new javax.swing.JLabel();
        exitCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(800, 600));
        setMinimumSize(new java.awt.Dimension(640, 480));
        setPreferredSize(new java.awt.Dimension(600, 480));

        userName.setText(LoaderInit.getUserName());
        userName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkUserChange(evt);
            }
        });

        userNameLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        userNameLabel.setForeground(userLabelColor());
        userNameLabel.setText("User name:");

        buttonStart.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        buttonStart.setEnabled(startEnabled());
        buttonStart.setLabel("Start");
        buttonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStartActionPerformed(evt);
            }
        });

        passwordLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        passwordLabel.setForeground(passwLabelColor());
        passwordLabel.setText("Password:");

        password.setText(getPassword());
        password.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkPasswordChange(evt);
            }
        });

        tabs.setPreferredSize(new java.awt.Dimension(600, 600));

        picture.setBackground(new java.awt.Color(0, 0, 0));

        versionLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        versionLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        versionLabel.setText(versionLabel());

        javax.swing.GroupLayout gameLayout = new javax.swing.GroupLayout(game);
        game.setLayout(gameLayout);
        gameLayout.setHorizontalGroup(
            gameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gameLayout.createSequentialGroup()
                .addGroup(gameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(versionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
                    .addComponent(picture, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 6, Short.MAX_VALUE))
        );
        gameLayout.setVerticalGroup(
            gameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gameLayout.createSequentialGroup()
                .addComponent(picture, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionLabel)
                .addContainerGap())
        );

        tabs.addTab("Game", game);

        downloadLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        downloadLabel.setEnabled(false);

        downloadProgress.setEnabled(false);

        buttonInstall.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        buttonInstall.setText("Install");
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
        downloadModsList.setText(modulesToInstallText());
        downloadModsList.setFocusable(false);
        downloadModsList.setMaximumSize(new java.awt.Dimension(350, 600));
        downloadModsList.setMinimumSize(new java.awt.Dimension(350, 300));
        downloadModsList.setPreferredSize(new java.awt.Dimension(350, 314));
        modulesList.setViewportView(downloadModsList);

        pathLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        pathLabel.setForeground(pathLabelColor());
        pathLabel.setText("Path:");

        path.setText(LoaderInit.getPath());
        path.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkPathChange(evt);
            }
        });

        selectPath.setText("Select");
        selectPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectActionPerformed(evt);
            }
        });

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
                            .addComponent(downloadProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, installLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(buttonInstall, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                        .addComponent(buttonInstall)
                        .addGap(0, 247, Short.MAX_VALUE))
                    .addComponent(modulesList, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabs.addTab("Install", install);

        gameState.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        gameState.setForeground(modulesStatusColor());
        gameState.setText(gameStatusMesage());

        exitCheckBox.setSelected(true);
        exitCheckBox.setText("Exit launcher");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(userNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(passwordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(userName, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(password))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(exitCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(buttonStart))
                            .addComponent(gameState, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameState))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonStart)
                    .addComponent(passwordLabel)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exitCheckBox))
                .addContainerGap())
        );

        buttonStart.getAccessibleContext().setAccessibleDescription("");
        tabs.getAccessibleContext().setAccessibleName("Game");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStartActionPerformed
        String newUserName = userName.getText();
        if (!newUserName.equals(LoaderInit.getUserName())) {
            LoaderInit.updateUserName(newUserName);
        }
        String newPassword = PasswordUtils.encrypt(password.getPassword());
        if (!newPassword.equals(LoaderInit.getUserPassword())) {
            LoaderInit.updateUserPassword(newPassword);
        }
        String newPath = path.getText();
        if (!newPath.equals(LoaderInit.getPath())) {
            LoaderInit.updatePath(newPath);
        }
        ctx.exitLauncher = exitCheckBox.isSelected();
        this.setVisible(false);
        this.dispose();
        ctx.wakeUp();
    }//GEN-LAST:event_buttonStartActionPerformed

    private void checkPathChange(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_checkPathChange
        final String gamePath = path.getText();
        pathExists = check.checkInstallDir(gamePath);
        gameCheckCache = pathExists && gamePath != null && check.check(gamePath);
        installationState = GameState.gameState(pathExists, gameCheckCache, ctx.modsToFix.isEmpty());
        if (installationState == GameState.OK || installationState == GameState.MODULES) {
            ctx.checkModules(gamePath);
            installationState = GameState.gameState(pathExists, gameCheckCache, ctx.modsToFix.isEmpty());
        }
        pathLabel.setForeground(pathLabelColor());
        gameState.setForeground(modulesStatusColor());
        gameState.setText(gameStatusMesage());
        buttonStart.setEnabled(startEnabled());
        buttonInstall.setEnabled(installEnabled());
        downloadModsList.setText(modulesToInstallText());
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
                installationState = GameState.gameState(pathExists, gameCheckCache, ctx.modsToFix.isEmpty());
                pathLabel.setForeground(pathLabelColor());
                buttonStart.setEnabled(startEnabled());
                gameState.setForeground(modulesStatusColor());
                gameState.setText(gameStatusMesage());
            }
        }
    }//GEN-LAST:event_buttonSelectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton buttonInstall;
    private javax.swing.JButton buttonStart;
    javax.swing.JLabel downloadLabel;
    private javax.swing.JTextPane downloadModsList;
    javax.swing.JProgressBar downloadProgress;
    private javax.swing.JCheckBox exitCheckBox;
    private javax.swing.JPanel game;
    private javax.swing.JLabel gameState;
    private javax.swing.JPanel install;
    private javax.swing.JScrollPane modulesList;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField path;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel picture;
    private javax.swing.JButton selectPath;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTextField userName;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}
