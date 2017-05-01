/*
 * (C) 2016 Tomas Kraus
 */
package mc.ui.loader;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mc.config.LoaderConfig;
import mc.init.LoaderInit;
import mc.init.Profile;
import mc.installer.GameCheck;

/**
 * Loader UI context.
 */
public class UiContext {

    /** Game modules that are not OK. */
    LinkedList<LoaderConfig.Mod> modsToFix;

    /** Profiles. */
    LinkedList<Profile> profiles;

    /** Exit launcher check box state. */
    boolean exitLauncher;
    
    /**
     * Creates a new instance of UI context.
     */
    public UiContext() {
//        if (LoaderConfig.isConfig() && LoaderInit.getPath() != null) {
//            this.modsToFix = GameCheck.checkModules(
//                    LoaderInit.getPath(), LoaderConfig.getModsPath(), LoaderConfig.getMods());
//        } else {
            this.modsToFix = null;
//        }
//        
        this.exitLauncher = false;
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
     */
    public void waitForUi() {
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
        this.modsToFix = GameCheck.checkModules(
                LoaderInit.getPath(), LoaderConfig.getModsPath(), LoaderConfig.getMods());
    }

    /**
     * Run modules check and update list of game modules that are not OK.
     * @param path Game installation path.
     */
    void checkModules(final String path) {
        // Help GC.
        if (this.modsToFix != null) {
            this.modsToFix.clear();
        }
        this.modsToFix = GameCheck.checkModules(path, LoaderConfig.getModsPath(), LoaderConfig.getMods());
    }

    /**
     * Get exit launcher check box state.
     * @return Exit launcher check box state.
     */
    public boolean getExitGame() {
        return exitLauncher;
    }

    /**
     * Do some modules for fixing exist?
     */
    boolean isModules() {
        return !(this.modsToFix == null || this.modsToFix.isEmpty());
    }

    /**
     * Do some modules for fixing exist?
     */
    boolean noModules() {
        return this.modsToFix != null && this.modsToFix.isEmpty();
    }

}
