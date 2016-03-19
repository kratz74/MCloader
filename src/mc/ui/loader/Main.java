/*
 * (C) 2016 Tomas Kraus
 */
package mc.ui.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import mc.launcher.Arguments;
import mc.config.ConfigReader;
import mc.config.LoaderConfig;
import mc.init.LoaderInit;

/**
 * Main application class.
 */
public class Main {

    private static final Object trigger = new Object();

    /** 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final LoaderInit init = LoaderInit.create("/data/archive/CMloader/src/mc/init");
        final LoaderConfig config = ConfigReader.read("/data/archive/CMloader/src/mc/config/loader.json");
        final UiContext uiCtx = new UiContext(init);
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoaderFrame(uiCtx).setVisible(true);
            }
        });
        uiCtx.waitForUi();
        LoaderInit.persist("/data/archive/CMloader/src/mc/init", init);
        System.out.println("ENDING");

        String path = init.getPath();
        Arguments arguments = new Arguments(
                init.getUserName(),
                "token:3f7c52dd20d0459c9d7187d41f93c4e4:f90d0677ebbf4283b5a82922d93fd9e1",
                "1.6.4-Forge9.11.1.965",
                FileUtils.fullPath(path, "cm164"),
                FileUtils.fullPath(path, "assets/virtual/legacy"),
                "cpw.mods.fml.common.launcher.FMLTweaker",
                "Carovny Minecraft",
                "1440",
                "900",
                FileUtils.fullPath(path, "assets/packs/carovnyminecraft-reloaded/icon.png")
        );
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String cp : config.getClassPath()) {
            if (first) {
                first = false;
            } else {
                sb.append(File.pathSeparator);
            }
            sb.append(FileUtils.fullPath(init.getPath(), cp));
        }
        String javaHome = System.getProperty("java.home");
        String javaBin = FileUtils.fullPath(javaHome, "bin/java");
        String[] javaArgs = new String[] {
                javaBin,
                "-Xdock:icon=" + FileUtils.fullPath(path, "assets/packs/ghostminepack/icon.png"),
                "-Xdock:name='Carovny Minecraft'",
                "-Xmx2000m",
                "-Djava.library.path="+FileUtils.fullPath(path, "lib/native"),
                "-Dfml.core.libraries.mirror=http://mirror.technicpack.net/Technic/lib/fml/%s",
                "-Dminecraft.applet.TargetDirectory="+FileUtils.fullPath(path, "cm164"),
                "-Djava.net.preferIPv4Stack=true",
                "-cp", sb.toString(),
                "net.minecraft.launchwrapper.Launch"
        };
        String[] classArgs = arguments.get();
        String[] execArgs = new String[javaArgs.length + classArgs.length];
        int argPos = 0;
        for (String arg : javaArgs) {
            execArgs[argPos++] = arg;
        }
        for (String arg : classArgs) {
            execArgs[argPos++] = arg;
        }
        
        try {
            //"  net.minecraft.launchwrapper.Launch";
            Process p = Runtime.getRuntime().exec(execArgs);
            InputStreamReader pi = new InputStreamReader(p.getInputStream());
            InputStreamReader pe = new InputStreamReader(p.getErrorStream());
            char[] buff = new char[256];
            boolean waitForThread = true;
            while (waitForThread) {
                boolean wait = true;
                if (pi.ready()) {
                    wait = false;
                    int len = pi.read(buff);
                    System.out.println("OUT " + len);
                    if (len > 0) {
                        for (int i = 0; i < len; i++) {
                            System.out.print(buff[i]);
                        }
                    }
                }
                if (pe.ready()) {
                    wait = false;
                    int len = pe.read(buff);
                    System.out.println("OUT " + len);
                    if (len > 0) {
                        for (int i = 0; i < len; i++) {
                            System.out.print(buff[i]);
                        }
                    }
                }
                if (wait) {
                    Thread.sleep(1000);
                }
                waitForThread = p.isAlive();
            }
            System.out.println("Exit code: " + p.exitValue());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
 }
