/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.loader.gui;

/**
 * Loader Window UI Context.
 */
public class LoaderUIContext implements AutoCloseable {

    /**
     * UI Components Access.
     */
    public interface Access {
        /**
         * Update displayed action {@code String} to match current UI context value.
         */
        void updateAction();

        /**
         * Update displayed description {@code String} to match current UI context value.
         */
        void updateDescription();

        /**
         * Display progress bar and initialize it.
         * @param minValue Minimum progress bar value.
         * @param maxValue Maximum progress bar value.
         */
        void showProgressBar(final int minValue, final int maxValue);

        /**
         * Hide progress bar and reset it.
         */
        void hideProgressBar();

        /**
         * Update displayed progress bar.
         * @param value Current progress bar value.
         */
        void updateProgressBar(final int value);

        /**
         * Shut down UI component.
         */
        void exit();
    }

    /** No activity {@code String}. */
    private static final String NO_ACTION = "";

    /** No description {@code String}. */
    private static final String NO_DESCRIPTION = "";

    /** Current action {@code String}. */
    private String action;
 
    /** Current description {@code String}. */
    private String description;

    private Access ui;
    /**
     * Creates a new instance of UI context.
     */
    public LoaderUIContext() {
        this.action = NO_ACTION;
        this.description = NO_DESCRIPTION;
        this.ui = null;
        java.awt.EventQueue.invokeLater(() -> {
            final LoaderWindow loaderWindow = new LoaderWindow(LoaderUIContext.this);
            this.ui = loaderWindow;
            loaderWindow.setVisible(true);
            wakeUp();
        });
    }

    /**
     * Get current activity {@code String}.
     * @return Current activity {@code String}.
     */
    public String getAction() {
        return action;
    }

    /**
     * Get current description {@code String}.
     * @return Current activity {@code String}.
     */
    public String getDescription() {
        return action;
    }

    /**
     * Update UI action {@code String}.
     * @param action Action {@code String} to display.
     */
    public void updateAction(final String action) {
        if (ui == null) {
            throw new IllegalStateException("UI access instance was not initialized yet!");
        } else {
            this.action = action;
            ui.updateAction();
        }
    }

    /**
     * Update UI description {@code String}.
     * @param description Description {@code String} to display.
     */
    public void updateDescription(final String description) {
        if (ui == null) {
            throw new IllegalStateException("UI access instance was not initialized yet!");
        } else {
            this.description = description;
            ui.updateDescription();
        }
    }

    /**
     * Cleanup this UI context instance.
     * UI component is destroyed if exists.
     */
    @Override
    public void close() {
        if (this.ui != null) {
            ui.exit();
            ui = null;
        }
    }

    /**
     * UI synchronization: Wait for UI to finish.
     */
    public void waitForUi() {
        synchronized(this) {
            if (ui == null) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }    

    /**
     * UI synchronization: Wake up thread waiting for UI to finish.
     */
    void wakeUp() {
        synchronized(this) {
            this.notifyAll();
        }        
    }

    /**
     * UI synchronization: Wake up thread waiting for UI to finish.
     */
    void finish() {
        synchronized(this) {
            this.notifyAll();
        }        
    }

}
