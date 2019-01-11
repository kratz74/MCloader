/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.loader;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.html.HTMLDocument;
import org.kratz.mc.loader.gui.LoaderUIContext;

/**
 * Main Loader Class.
 */
public class Main {
    
    /** Thread name. */
    private static final String THREAD_NAME = "Loader.Main";

    /** 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Thread.currentThread().setName(THREAD_NAME);
        HTMLDocument htmlDoc = new HTMLDocument();
        final LoaderUIContext uiCtx = new LoaderUIContext();
        uiCtx.waitForUi();
        for (int i = 10; i > 0; i--) {
            try {
                uiCtx.updateAction(Integer.toString(i));
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        uiCtx.close();
    }

}
