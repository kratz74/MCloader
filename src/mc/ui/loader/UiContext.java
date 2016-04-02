/*
 * (C) 2016 Tomas Kraus
 */
package mc.ui.loader;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mc.config.LoaderConfig;
import mc.init.LoaderInit;
import mc.installer.GameCheck;

/**
 * Loader UI context.
 */
public class UiContext {

    /** Loader initialization object. */
    final LoaderInit init;

    /** Loader configuration object. */
    final LoaderConfig config;

    /** Game modules that are not OK. */
    LinkedList<LoaderConfig.Mod> modsToFix;

    /**
     * Creates a new instance of UI context.
     * @param init   Loader initialization data.
     * @param config Loader configuration data.
     */
    public UiContext(final LoaderInit init, final LoaderConfig config) {
        this.init = init;
        this.config = config;
        this.modsToFix = GameCheck.checkModules(init.getPath(), config.getMods());
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
     * UI synchronization: Wait for UI to finish.
     * @param ctx Loader UI context.
     */
    void waitForUi() {
        synchronized(this) {
            try {
                this.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(UiContext.class.getName()).log(Level.SEVERE, "Waiting for UI was interrupted: ", ex);
            }
        }
    }    

    /**
     * Remove provided module from modules that are not OK.
     * @param mod Module to remove.
     */
    void removeModToFix(LoaderConfig.Mod mod) {
        modsToFix.remove(mod);
    }

    /**
     * Run modules check and update list of game modules that are not OK.
     */
    void checkModules() {
        // Help GC.
        if (this.modsToFix != null) {
            this.modsToFix.clear();
        }
        this.modsToFix = GameCheck.checkModules(init.getPath(), config.getMods());
    }

}
