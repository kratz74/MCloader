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
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import mc.config.LoaderConfig;
import mc.installer.DownloadModules;
import mc.installer.Downloader;

/**
 *
 * @author kratz
 */
public class LoaderFrame extends javax.swing.JFrame {

    /** Dark red color. */
    private static final Color DARK_RED = new java.awt.Color(0x68, 0, 0);

    /**
     * Load image bitmap.
     */
    private static BufferedImage readImage(final String file) {
        try {
            return ImageIO.read(new File(file));
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Could not read image %s", file);
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
     * Validate user password.
     * @return Value of {@code true} when user password is valid or {@code false} otherwise.
     */
    private static boolean checkPassword(final char[] passw) {
        return passw != null && passw.length > 2;
    }

    /** Loader initialization object. */
    final UiContext ctx;
    /** Game installation check. */
    final GameCheck check;
    /** Picture with game logo. */
    final BufferedImage logoPicure;

    /** Download change listener. */
    final DownloadListener downloadListener;

    /** Game installation OK for current path. Cache for checks without path modification. */
    boolean gameCheckCache;
    /** User name is OK for the game. Cache for checks without user modification. */
    boolean userCheckCache;
    /** User password is OK for the game. Cache for checks without user modification. */
    boolean passCheckCache;

    /** Game components download handler. */
    private Downloader installer;

    /**
     * Creates a new instance of form LoaderFrame.
     * @param ctx UI context.
     */
    public LoaderFrame(final UiContext ctx) {
        this.ctx = ctx;
        check = new GameCheck(ctx.config);
        gameCheckCache = ctx.init.getPath() != null && check.check(ctx.init.getPath());
        userCheckCache = checkUserName(ctx.init.getUserName());
        passCheckCache = false;
        logoPicure = readImage("/data/CMloader/src/mc/launcher/thaumcraft.png");
        Logger.log(LogLevel.FINE, "Game start is %senabled", startEnabled() ? "" : "not ");
        Logger.log(LogLevel.FINEST, "  Game path: %s", gameCheckCache ? "OK" : "Not OK ");
        Logger.log(LogLevel.FINEST, "  User name: %s", userCheckCache ? "OK" : "Not OK ");
        Logger.log(LogLevel.FINEST, "  Password:  %s", passCheckCache ? "OK" : "Not OK ");
        initComponents();
        downloadListener = new ModuleDownloadListener(this);
        installer = initDownloader();
        initDownloadComponents();
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
        return installer != null && !installer.isRunning();
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
    private String modulesStatusMesage() {
        if (ctx.modsToFix.isEmpty()) {
            return "Modules are OK";
        } else {
            return "Modules update required. Go to Install tab";
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
     * Update modules status in UI.
     */
    void updateModulesStatus() {
        ctx.checkModules();
        modulesState.setForeground(modulesStatusColor());
        modulesState.setText(modulesStatusMesage());
        buttonStart.setEnabled(startEnabled());
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
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (LoaderConfig.Mod mod : ctx.modsToFix) {
            if (first) {
                first = false;
            } else {
                sb.append('\n');
            }
            sb.append(' ');
            sb.append(mod.getFile());
        }
        return sb.toString();
    }

    /**
     * Initialize {@link Downloader} component depending on current game installation state.
     * @return {@link Downloader} component depending on current game installation state.
     */
    private Downloader initDownloader() {
        if (gameCheckCache && ctx.modsToFix != null && !ctx.modsToFix.isEmpty()) {
            return new DownloadModules(ctx.init.getPath(), ctx.modsToFix, downloadListener);
        } else {
            return null;
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
        if (installEnabled()) {
            buttonInstall.setEnabled(true);
        } else {
            buttonInstall.setEnabled(false);
        }
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
        pathLabel = new javax.swing.JLabel();
        path = new javax.swing.JTextField();
        picture = new javax.swing.JLabel(new ImageIcon(logoPicure));
        modulesState = new javax.swing.JLabel();
        install = new javax.swing.JPanel();
        downloadLabel = new javax.swing.JLabel();
        downloadProgress = new javax.swing.JProgressBar();
        buttonInstall = new javax.swing.JButton();
        modulesList = new javax.swing.JScrollPane();
        downloadModsList = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(800, 600));
        setMinimumSize(new java.awt.Dimension(640, 480));

        userName.setText(ctx.init.getUserName());
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

        password.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkPasswordChange(evt);
            }
        });

        pathLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        pathLabel.setForeground(pathLabelColor());
        pathLabel.setText("Path:");

        path.setText(ctx.init.getPath());
        path.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkPathChange(evt);
            }
        });

        modulesState.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        modulesState.setForeground(modulesStatusColor());
        modulesState.setText(modulesStatusMesage());

        javax.swing.GroupLayout gameLayout = new javax.swing.GroupLayout(game);
        game.setLayout(gameLayout);
        gameLayout.setHorizontalGroup(
            gameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(picture, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(gameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gameLayout.createSequentialGroup()
                        .addComponent(pathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(path, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE))
                    .addComponent(modulesState, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        gameLayout.setVerticalGroup(
            gameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gameLayout.createSequentialGroup()
                .addComponent(picture, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modulesState)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathLabel)
                    .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        downloadModsList.setMaximumSize(new java.awt.Dimension(300, 2147483647));
        downloadModsList.setMinimumSize(new java.awt.Dimension(300, 16));
        downloadModsList.setPreferredSize(new java.awt.Dimension(300, 16));
        modulesList.setViewportView(downloadModsList);

        javax.swing.GroupLayout installLayout = new javax.swing.GroupLayout(install);
        install.setLayout(installLayout);
        installLayout.setHorizontalGroup(
            installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(installLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(downloadLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(downloadProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, installLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonInstall, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modulesList, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        installLayout.setVerticalGroup(
            installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(installLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(installLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(installLayout.createSequentialGroup()
                        .addComponent(downloadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downloadProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonInstall)
                        .addGap(0, 256, Short.MAX_VALUE))
                    .addComponent(modulesList))
                .addContainerGap())
        );

        tabs.addTab("Install", install);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(userNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userName)
                    .addComponent(password))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonStart)
                .addContainerGap())
            .addComponent(tabs, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonStart)
                    .addComponent(passwordLabel)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        buttonStart.getAccessibleContext().setAccessibleDescription("");
        tabs.getAccessibleContext().setAccessibleName("Game");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStartActionPerformed
        String newUserName = userName.getText();
        if (!newUserName.equals(ctx.init.getUserName())) {
            ctx.init.updateUserName(newUserName);
        }
        String newPath = path.getText();
        if (!newPath.equals(ctx.init.getPath())) {
            ctx.init.updatePath(newPath);
        }
        this.setVisible(false);
        this.dispose();
        ctx.wakeUp();
    }//GEN-LAST:event_buttonStartActionPerformed

    private void checkPathChange(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_checkPathChange
        final String gamePath = path.getText();
        gameCheckCache = gamePath != null && check.check(gamePath);
        pathLabel.setForeground(pathLabelColor());
        buttonStart.setEnabled(startEnabled());
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
        installer.start();
    }//GEN-LAST:event_buttonInstallActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton buttonInstall;
    private javax.swing.JButton buttonStart;
    javax.swing.JLabel downloadLabel;
    private javax.swing.JTextPane downloadModsList;
    javax.swing.JProgressBar downloadProgress;
    private javax.swing.JPanel game;
    private javax.swing.JPanel install;
    private javax.swing.JScrollPane modulesList;
    private javax.swing.JLabel modulesState;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField path;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel picture;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTextField userName;
    private javax.swing.JLabel userNameLabel;
    // End of variables declaration//GEN-END:variables
}
