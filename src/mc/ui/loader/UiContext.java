/*
 * (C) 2016 Tomas Kraus
 */
package mc.ui.loader;

import java.util.logging.Level;
import java.util.logging.Logger;
import mc.init.LoaderInit;

/**
 * Loader UI context.
 */
public class UiContext {


    /** Loader initialization object. */
    final LoaderInit init;

    /**
     * Creates a new instance of UI context.
     * @param init Loader initialization data.
     */
    public UiContext(final LoaderInit init) {
        this.init = init;
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

}
