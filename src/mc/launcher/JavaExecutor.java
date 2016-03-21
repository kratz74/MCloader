/*
 * (C) 2016 Tomas Kraus
 */
package mc.launcher;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import mc.config.Argument;
import mc.config.LoaderConfig;
import mc.config.Property;
import mc.init.LoaderInit;
import mc.log.LogLevel;
import mc.log.Logger;

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

    /** Logger. */
    private static final Logger LOG = Logger.getInstance();

    /**
     * Add Java options into specified execution arguments list.
     * @param arguments   Target execution arguments list.
     * @param javaOptions Java options list.
     * @param init        Loader initialization data.
     */
    private static void addOptions(final LinkedList<String> arguments,
            final LinkedList<Property> javaOptions, final LoaderInit init) {
        for (Property property: javaOptions) {
            final String name = property.getName();
            final String value = Replace.expand(property.getValue(), init);
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
            LOG.log(LogLevel.FINEST, "Added java option: %s", option);
            if (option.length() != len) {
                LOG.log(LogLevel.WARNING, "Java option length calculation error for %s", option);
            }
        }
    }

    /**
     * Add Java properties into specified execution arguments list.
     * @param arguments  Target execution arguments list.
     * @param properties Java properties list.
     * @param init       Loader initialization data.
     */
    private static void addProperties(final LinkedList<String> arguments,
            final LinkedList<Property> properties, final LoaderInit init) {
        for (Property property: properties) {
            final String name = property.getName();
            final String value = Replace.expand(property.getValue(), init);
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
            LOG.log(LogLevel.FINEST, "Added java property: %s", propArg);
            if (propArg.length() != len) {
                LOG.log(LogLevel.WARNING, "Java property length calculation error for %s", propArg);
            }
        }
    }

    /**
     * Add Java class path into specified execution arguments list.
     * @param arguments Target execution arguments list.
     * @param classpath Java class path list.
     * @param init      Loader initialization data.
    */
    private static void addClassPath(final LinkedList<String> arguments,
            final LinkedList<String> classpath, final LoaderInit init) {
        final String path = init.getPath();
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
        LOG.log(LogLevel.FINEST, "Added classpath: %s", cp);
        if (cp.length() != len) {
            LOG.log(LogLevel.WARNING, "Java property length calculation error for class path");
        }
    }

    /**
     * Add main class name into specified execution arguments list.
     * @param arguments Target execution arguments list.
     * @param className Main Java class name.
     */
    private static void addMainClass(final LinkedList<String> arguments, final String className) {
        arguments.add(className);
        LOG.log(LogLevel.FINEST, "Added main class: %s", className);
    }

    /**
     * Add Java properties into specified execution arguments list.
     * @param arguments Target execution arguments list.
     * @param classArgs Java main class arguments list.
     * @param init      Loader initialization data.
    */
    private static void addArguments(final LinkedList<String> arguments,
            final LinkedList<Argument> classArgs, final LoaderInit init) {
        for (Argument arg : classArgs) {
            final String name = arg.getName();
            final String value = Replace.expand(arg.getValue(), init);
            final int len = name.length() + CLASS_ARG_PREFIX.length();
            final StringBuilder sb = new StringBuilder(len);
            sb.append(CLASS_ARG_PREFIX);
            sb.append(name);
            final String argName = sb.toString();
            arguments.add(argName);
            if (value != null) {
                arguments.add(value);
                LOG.log(LogLevel.FINEST, "Added main class argument: %s %s", argName, value);
            } else {
                LOG.log(LogLevel.FINEST, "Added main class argument: %s", argName);
            }
            if (argName.length() != len) {
                LOG.log(LogLevel.WARNING, "Main class argument length calculation error for %s", argName);
            }
            
        }
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
     * @param config      Loader configuration data.
     * @param javaRuntime Java runtime for executing a new process.
     * @param init        Loader initialization data.
     * @return {@link String} array with process execution arguments.
     */
    private static String[] buildArguments(final LoaderConfig config,
            final JavaRuntime javaRuntime, final LoaderInit init) {
        final LinkedList<String> args = new LinkedList<>();
        // Add Java executable.
        final File javaExec = javaRuntime.getJava();
        if (javaExec != null) {
            args.add(javaExec.getAbsolutePath());
            addOptions(args, config.getJavaOptions(), init);
            addProperties(args, config.getProperties(), init);
            addClassPath(args, config.getClassPath(), init);
            addMainClass(args, config.getStartupClass());
            addArguments(args, config.getArguments(), init);
            return listToArray(args);
        } else {
            LOG.log(LogLevel.FATAL, "Java executable is not available");
        }
        return null;
    }

    /** Execution arguments. */
    private final String[] execArgs;

    /**
     * Creates an empty instance of Java VM executor.
     * @param init Loader initialization data.
     * @param config Loader configuration data.
     */
    public JavaExecutor(final LoaderInit init, final LoaderConfig config) {
        this.execArgs = buildArguments(config, new JavaRuntime(), init);
        for (String arg : execArgs) {
            LOG.log(LogLevel.FINEST, 1, "Exec: %s", arg);
        }
    }

    public Process exec() {
        try {
            return Runtime.getRuntime().exec(execArgs);
        } catch (IOException ex) {
            LOG.log(LogLevel.FATAL, "Could not execute process: %s", ex);
            return null;
        }
    }

}
