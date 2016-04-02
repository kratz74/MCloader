/*
 * (C) 2016 Tomas Kraus
 */
package mc.ui.loader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import mc.config.ConfigReader;
import mc.config.LoaderConfig;
import mc.init.LoaderInit;
import mc.launcher.JavaExecutor;

/**
 * Main application class.
 */
public class Main {

    /** Thread name. */
    private static final String THREAD_NAME = "CM164.Main";

    /** 
     * @param args the command line arguments
     */
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String[] args) {
        Thread.currentThread().setName(THREAD_NAME);
        final LoaderInit init = LoaderInit.create("/data/CMloader/src/mc/init");
        final LoaderConfig config = ConfigReader.read("/data/CMloader/src/mc/config/loader.json");
        final UiContext uiCtx = new UiContext(init, config);
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoaderFrame(uiCtx).setVisible(true);
            }
        });
        uiCtx.waitForUi();
        LoaderInit.persist("/data/CMloader/src/mc/init", init);
        JavaExecutor executor = new JavaExecutor(init, config);
        Process p = executor.exec();

        // TODO: This shall be rewritten later.
        try {
            InputStreamReader pi = new InputStreamReader(p.getInputStream());
            InputStreamReader pe = new InputStreamReader(p.getErrorStream());
            char[] buff = new char[256];
            boolean waitForThread = true;
            while (waitForThread) {
                boolean wait = true;
                while (pi.ready()) {
                    wait = false;
                    int len = pi.read(buff);
                    if (len > 0) {
                        for (int i = 0; i < len; i++) {
                            System.out.print(buff[i]);
                        }
                    }
                }
                while (pe.ready()) {
                    wait = false;
                    int len = pe.read(buff);
                    if (len > 0) {
                        for (int i = 0; i < len; i++) {
                            System.out.print(buff[i]);
                        }
                    }
                }
                if (wait) {
                    Thread.sleep(200);
                }
                waitForThread = p.isAlive();
            }
            System.out.println("Exit code: " + p.exitValue());
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
 }
