/*
 * (C) 2016 Tomas Kraus
 */
package mc.ui.loader;

import mc.config.LoaderConfig;

/**
 * Download progress event.
 */
public interface DownloadListener {

    /**
     * Mark downloading as started.
     * This will activate related UI elements.
     */
    public void begin();

    /**
     * Mark downloading as ended.
     * This will deactivate related UI elements.
     * @param result Thread execution result: Value of {@code true} if thread execution was finished successfully
     *               or {@code false} otherwise.
     */
    public void end(final boolean result);

    /**
     * Set current download target name.
     * @param name Download target name.
     */
    public void name(final String name);

    /**
     * Set current progress.
     * @param progress Progress value in % (0-100).
     */
    public void progress(final int progress);

    /**
     * Mark module as finished.
     * @param mod Module that was successfully downloaded. 
     */
    public void moduleDone(LoaderConfig.Mod mod);

}
