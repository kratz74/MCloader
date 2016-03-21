/*
 * (C) 2016 Tomas Kraus
 */
package mc.config;

/**
 * Main class startup argument configuration element.
 */
public class Argument {

    /** Startup argument name. */
    private final String name;

    /** Startup argument value. */
    private final String value;
    /**
     * Creates an instance of Main class startup argument configuration element.
     * @param name  Option/property name.
     * @param value Option/property value.
     */
    Argument(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get startup argument name.
     * @return Startup argument name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get startup argument value.
     * @return Startup argument value.
     */
    public String getValue() {
        return value;
    }

}

