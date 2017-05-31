/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.ui.loader;

import org.kratz.mc.config.LoaderConfig;

/**
 * Process module download events.
 */
public class ModuleDownloadListener implements DownloadListener {

    /** UI instance. */
    private final LoaderFrame ui;

    /**
     * Creates an instance of download progress bar events listener.
     * @param ui UI instance.
     */
    ModuleDownloadListener(final LoaderFrame ui) {
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
     * @param result Thread execution result: Value of {@code true} if thread execution was finished successfully
     *               or {@code false} otherwise.
     */
    @Override
    public void end(final boolean result) {
        ui.resetInstaller();
        ui.resetDownloadUI();
        ui.updateGameStatusForModules();
        ui.updateGameComponentsVisibility();
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
        ui.moduleDownloadFinished(mod);
    }
    
}
