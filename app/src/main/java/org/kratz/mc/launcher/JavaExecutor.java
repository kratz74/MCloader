/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.launcher;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.kratz.mc.config.Argument;
import org.kratz.mc.config.LoaderConfig;
import org.kratz.mc.config.Property;
import org.kratz.mc.init.LoaderInit;
import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;

/**
 * Executes Java VM.
 */
public class JavaExecutor {
    /** Java option argument prefix. */
    private static final String JAVA_OPTION_PREFIX = "-";

    /** Java option assignment operator. */
    private static final String JAVA_OPTION_ASSIGN = "=";

    /** Java property argument prefix. */
    private static final String JAVA_PROPERTY_PREFIX = "-D";

    /** Java property assignment operator. */
    private static final String JAVA_PROPERTY_ASSIGN = "=";

    /** Java class path option prefix. */
    private static final String JAVA_CP_PREFIX = "-cp";

    /** Main class argument prefix. */
    private static final String CLASS_ARG_PREFIX = "--";

    /**
     * Add Java options into specified execution arguments list.
     * @param arguments   Target execution arguments list.
     * @param javaOptions Java options list.
     */
    private static void addOptions(final LinkedList<String> arguments, final LinkedList<Property> javaOptions) {
        for (Property property: javaOptions) {
            if (property.isAlowed()) {
                final String name = property.getName();
                final String value = Replace.expand(property.getValue());
                final int len = name.length() + JAVA_OPTION_PREFIX.length()
                        + (value != null ? value.length() + JAVA_OPTION_ASSIGN.length() : 0);
                final StringBuilder sb = new StringBuilder(len);
                sb.append(JAVA_OPTION_PREFIX);
                sb.append(name);
                if (value != null) {
                    sb.append(JAVA_OPTION_ASSIGN);
                    sb.append(value);
                }
                String option = sb.toString();
                arguments.add(option);
                Logger.log(LogLevel.FINEST, "Added java option: %s", option);
                if (option.length() != len) {
                    Logger.log(LogLevel.WARNING, "Java option length calculation error for %s", option);
                }
            }
        }
    }

    /** HTTP proxy host Java property name. */
    private static final String JAVA_HTTP_PROXY_HOST_PROP = "http.proxyHost";

    /** HTTP proxy port Java property name. */
    private static final String JAVA_HTTP_PROXY_PORT_PROP = "http.proxyPort";

    /** HTTP proxy host Java property name. */
    private static final String JAVA_HTTPS_PROXY_HOST_PROP = "https.proxyHost";

    /** HTTP proxy port Java property name. */
    private static final String JAVA_HTTPS_PROXY_PORT_PROP = "https.proxyPort";

    /**
     * 
     * @param arguments 
     */
    private static void addProxyProperties(final LinkedList<String> arguments) {
        final String host = LoaderInit.getHttpProxyHost();
        if (host != null && host.length() > 0) {
            StringBuilder sb = new StringBuilder(
                    JAVA_HTTP_PROXY_HOST_PROP.length() + JAVA_PROPERTY_PREFIX.length()
                    + host.length() + JAVA_OPTION_ASSIGN.length());
            sb.append(JAVA_PROPERTY_PREFIX);
            sb.append(JAVA_HTTP_PROXY_HOST_PROP);
            sb.append(JAVA_OPTION_ASSIGN);
            sb.append(host);
            arguments.add(sb.toString());
            final int port = LoaderInit.getHttpProxyPort();
            final String portStr = port >= 0 ? Integer.toString(port) : "80";
            sb = new StringBuilder(
                    JAVA_HTTP_PROXY_PORT_PROP.length() + JAVA_PROPERTY_PREFIX.length()
                    + portStr.length() + JAVA_OPTION_ASSIGN.length());
            sb.append(JAVA_PROPERTY_PREFIX);
            sb.append(JAVA_HTTP_PROXY_PORT_PROP);
            sb.append(JAVA_OPTION_ASSIGN);
            sb.append(portStr);
            arguments.add(sb.toString());
            sb = new StringBuilder(
                    JAVA_HTTPS_PROXY_HOST_PROP.length() + JAVA_PROPERTY_PREFIX.length()
                    + host.length() + JAVA_OPTION_ASSIGN.length());
            sb.append(JAVA_PROPERTY_PREFIX);
            sb.append(JAVA_HTTPS_PROXY_HOST_PROP);
            sb.append(JAVA_OPTION_ASSIGN);
            sb.append(host);
            arguments.add(sb.toString());
            sb = new StringBuilder(
                    JAVA_HTTPS_PROXY_PORT_PROP.length() + JAVA_PROPERTY_PREFIX.length()
                    + portStr.length() + JAVA_OPTION_ASSIGN.length());
            sb.append(JAVA_PROPERTY_PREFIX);
            sb.append(JAVA_HTTPS_PROXY_PORT_PROP);
            sb.append(JAVA_OPTION_ASSIGN);
            sb.append(portStr);
            arguments.add(sb.toString());
        }
    }

    /**
     * Add Java properties into specified execution arguments list.
     * @param arguments  Target execution arguments list.
     * @param properties Java properties list.
     */
    private static void addProperties(final LinkedList<String> arguments, final LinkedList<Property> properties) {
        properties.forEach((property) -> {
            final String name = property.getName();
            final String value = Replace.expand(property.getValue());
            final int len = name.length() + JAVA_PROPERTY_PREFIX.length()
                    + (value != null ? value.length() + JAVA_PROPERTY_ASSIGN.length() : 0);
            final StringBuilder sb = new StringBuilder(len);
            sb.append(JAVA_PROPERTY_PREFIX);
            sb.append(name);
            if (value != null) {
                sb.append(JAVA_PROPERTY_ASSIGN);
                sb.append(value);
            }
            String propArg = sb.toString();
            arguments.add(propArg);
            Logger.log(LogLevel.FINEST, "Added java property: %s", propArg);
            if (propArg.length() != len) {
                Logger.log(LogLevel.WARNING, "Java property length calculation error for %s", propArg);
            }
        });
    }

    /**
     * Add Java class path into specified execution arguments list.
     * @param arguments Target execution arguments list.
     * @param classpath Java class path list.
    */
    private static void addClassPath(final LinkedList<String> arguments, final LinkedList<String> classpath) {
        final String path = LoaderInit.getPath();
        final int pathLen = path.length();
        final int sepLength = File.separator.length();
        final int pSepLen = File.pathSeparator.length();
        final int itemsCount = classpath.size();

        final boolean pathEndsWithSeparator = path.endsWith(File.separator);
        // Calculate classpath String length to avoid StringBuilder resizing
        int len = pathLen * itemsCount + (pSepLen * (itemsCount - 1));
        for (String item : classpath) {
            if (!pathEndsWithSeparator && !item.startsWith(File.separator)) {
                len += sepLength;
            }
            len += item.length();
        }
        // Build class path argument.
        final StringBuilder sb = new StringBuilder(len);
        boolean first = true;
        for (String item : classpath) {
            if (first) {
                first = false;
            } else {
                sb.append(File.pathSeparator);
            }
            sb.append(path);
            if (!pathEndsWithSeparator && !item.startsWith(File.separator)) {
                sb.append(File.separator);
            }
            sb.append(item);
        }
        String cp = sb.toString();

        arguments.add(JAVA_CP_PREFIX);
        arguments.add(cp);
        Logger.log(LogLevel.FINEST, "Added classpath: %s", cp);
        if (cp.length() != len) {
            Logger.log(LogLevel.WARNING, "Java property length calculation error for class path");
        }
    }

    /**
     * Add main class name into specified execution arguments list.
     * @param arguments Target execution arguments list.
     * @param className Main Java class name.
     */
    private static void addMainClass(final LinkedList<String> arguments, final String className) {
        arguments.add(className);
        Logger.log(LogLevel.FINEST, "Added main class: %s", className);
    }

    /**
     * Add Java properties into specified execution arguments list.
     * @param arguments Target execution arguments list.
     * @param classArgs Java main class arguments list.
    */
    private static void addArguments(final LinkedList<String> arguments, final LinkedList<Argument> classArgs) {
        classArgs.forEach((arg) -> {
            final String name = arg.getName();
            final String value = Replace.expand(arg.getValue());
            final int len = name.length() + CLASS_ARG_PREFIX.length();
            final StringBuilder sb = new StringBuilder(len);
            sb.append(CLASS_ARG_PREFIX);
            sb.append(name);
            final String argName = sb.toString();
            arguments.add(argName);
            if (value != null) {
                arguments.add(value);
                Logger.log(LogLevel.FINEST, "Added main class argument: %s %s", argName, value);
            } else {
                Logger.log(LogLevel.FINEST, "Added main class argument: %s", argName);
            }
            if (argName.length() != len) {
                Logger.log(LogLevel.WARNING, "Main class argument length calculation error for %s", argName);
            }
        });
    }

    /**
     * Convert execution arguments list to {@link String} array.
     * @param args Execution arguments list to be converted.
     * @return {@link String} array containing content of provided list.
     */
    private static String[] listToArray(final LinkedList<String> args) {
        final String[] array = new String[args.size()];
        int i = 0;
        for (String arg : args) {
            array[i++] = arg;
        }
        return array;
    }
    
    /**
     * Build process execution arguments.
     * @param javaRuntime Java runtime for executing a new process.
     * @return {@link String} array with process execution arguments.
     */
    private static String[] buildArguments(final JavaRuntime javaRuntime) {
        final LinkedList<String> args = new LinkedList<>();
        // Add Java executable.
        final File javaExec = javaRuntime.getJava();
        if (javaExec != null) {
            args.add(javaExec.getAbsolutePath());
            addOptions(args, LoaderConfig.getJavaOptions());
            addProxyProperties(args);
            addProperties(args, LoaderConfig.getProperties());
            addClassPath(args, LoaderConfig.getClassPath());
            addMainClass(args, LoaderConfig.getStartupClass());
            addArguments(args, LoaderConfig.getArguments());
            return listToArray(args);
        } else {
            Logger.log(LogLevel.FATAL, "Java executable is not available");
        }
        return null;
    }

    /** Execution arguments. */
    private final String[] execArgs;

    /** Game installation root directory. */
    private final File path;
    
    /**
     * Creates an empty instance of Java VM executor.
     */
    public JavaExecutor() {
        this.execArgs = buildArguments(new JavaRuntime());
        this.path = new File(LoaderInit.getPath());
        for (String arg : execArgs) {
            Logger.log(LogLevel.FINEST, 1, "Exec: %s", arg);
        }
    }

    public Process exec() {
        try {
            if (Logger.shouldLog(LogLevel.FINE)) {
                Logger.log(LogLevel.FINE, "Running game in %s", path.getAbsoluteFile());
                Logger.log(LogLevel.FINE, "Executing %s", execArgsToString());
            }
            return Runtime.getRuntime().exec(execArgs, (String[])null, path);
        } catch (IOException ex) {
            Logger.log(LogLevel.FATAL, "Could not execute process: ", ex);
            return null;
        }
    }

    private String execArgsToString() {
        if (execArgs == null) {
            return "<null>";
        } else {
            final StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String arg : execArgs) {
                if (first) {
                    first = false;
                } else {
                    sb.append(' ');
                }
                sb.append(arg);
            }
            return sb.toString();
        }
    }

}
