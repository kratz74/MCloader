/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kratz.mc.init.LoaderInit;
import org.kratz.mc.launcher.JavaExecutor;
import org.kratz.mc.ui.loader.LoaderFrame;
import org.kratz.mc.ui.loader.UiContext;

/**
 * Main application class.
 */
public class Main {

    /** Thread name. */
    private static final String THREAD_NAME = "CM.Main";

    /**
     * Check whether the subprocess represented by provided {@code Process} instance is alive.
     * @param p Subprocess {@code Process} instance to be checked.
     * @return {@code true} if the subprocess represented by this {@code Process} object has not yet terminated.
     */
    public static boolean isAlive(Process p) {
        try {
            p.exitValue();
            return false;
        } catch(IllegalThreadStateException e) {
            return true;
        }
    }
    /** 
     * @param args the command line arguments
     */
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String[] args) {
        Thread.currentThread().setName(THREAD_NAME);
        final UiContext uiCtx = new UiContext();
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoaderFrame(uiCtx).setVisible(true);
            }
        });
        uiCtx.waitForUi();
        LoaderInit.persist();
        JavaExecutor executor = new JavaExecutor();
        Process p = executor.exec();

        if (uiCtx.getExitGame())
            return;

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
                waitForThread = isAlive(p);
            }
            System.out.println("Exit code: " + p.exitValue());
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
 }
