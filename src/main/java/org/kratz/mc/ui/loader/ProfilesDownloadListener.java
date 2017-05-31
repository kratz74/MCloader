/*
 * (C) 2017 Tomas Kraus
 */
package org.kratz.mc.ui.loader;

import org.kratz.mc.config.LoaderConfig;
import org.kratz.mc.init.LoaderInit;

/**
 * Process profiles list download events.
 */
public class ProfilesDownloadListener implements DownloadListener {
    
    /** UI instance. */
    private final LoaderFrame ui;

    /**
     * Creates an instance of download progress bar events listener.
     * @param ui UI instance.
     */
    ProfilesDownloadListener(final LoaderFrame ui) {
        this.ui = ui;
    }

    /**
     * Mark downloading as started.
     * Show all related UI elements.
     */
    @Override
    public void begin() {
        ui.profileProgressLabel.setVisible(true);
        ui.profleProgress.setVisible(true);
    }

    /**
     * Mark downloading as ended.
     * Hide all related UI elements.
     * @param result Thread execution result: Value of {@code true} if thread execution was finished successfully
     *               or {@code false} otherwise.
     */
    @Override
    public void end(final boolean result) {
        if (result) {
            LoaderInit.updateProfiles();
        }
        ui.updateProfilesSelectBox(true);
        ui.profileDownloader = null;
        ui.updateGameStatusForBase();
        ui.updateGameComponentsVisibility();
    }

    /**
     * Set current download target name.
     * @param name Download target name.
     */
    @Override
    public void name(final String name) {
        ui.profileProgressLabel.setText(name);
    }

    /**
     * Set current module download progress.
     * @param progress Progress value in % (0-100).
     */
    @Override
    public void progress(int progress) {
        ui.profleProgress.setValue(progress);
    }

    /**
     * Mark module as finished.
     * @param mod Module that was successfully downloaded. 
     */
    @Override
    public void moduleDone(LoaderConfig.Mod mod) {
        throw new UnsupportedOperationException("Profile list does not contain modules");
    }

}
