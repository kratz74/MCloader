/*
 * (C) 2016 Tomas Kraus
 */
package mc.ui.loader;

import mc.config.LoaderConfig;

/**
 * Process game base download events.
 */
public class BaseDownloadListener implements DownloadListener {
    
    /** UI instance. */
    private final LoaderFrame ui;

    /**
     * Creates an instance of download progress bar events listener.
     * @param ui UI instance.
     */
    BaseDownloadListener(final LoaderFrame ui) {
        this.ui = ui;
    }

    /**
     * Mark downloading as started.
     * Show all related UI elements.
     */
    @Override
    public void begin() {
        //label.setVisible(true);
        ui.downloadLabel.setVisible(true);
        ui.downloadProgress.setVisible(true);
        ui.downloadLabel.setEnabled(true);
        ui.downloadProgress.setEnabled(true);
    }

    /**
     * Mark downloading as ended.
     * Hide all related UI elements.
     */
    @Override
    public void end() {
        ui.downloadLabel.setEnabled(false);
        ui.downloadProgress.setEnabled(false);
        //label.setVisible(false);
        ui.downloadLabel.setVisible(false);
        ui.downloadProgress.setVisible(false);
        ui.resetInstaller();
        ui.resetDownloadUI();
        ui.updateGameStatusForBase();
    }

    /**
     * Set current download target name.
     * @param name Download target name.
     */
    @Override
    public void name(final String name) {
        ui.downloadLabel.setText(name);
    }

    /**
     * Set current module download progress.
     * @param progress Progress value in % (0-100).
     */
    @Override
    public void progress(int progress) {
        ui.downloadProgress.setValue(progress);
    }

    /**
     * Mark module as finished.
     * @param mod Module that was successfully downloaded. 
     */
    @Override
    public void moduleDone(LoaderConfig.Mod mod) {
        throw new UnsupportedOperationException("Base game package does not contain modules");
    }
}
