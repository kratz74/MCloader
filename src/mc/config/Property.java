/*
 * (C) 2016 Tomas Kraus
 */
package mc.config;

/**
 * Java option and/or property configuration element.
 */
public class Property {

    /** Option/property name. */
    private final String name;

    /** Option/property value. */
    private final String value;

    /**
     * Creates an instance of option and/or property configuration element.
     * @param name  Option/property name.
     * @param value Option/property value.
     */
    Property(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get option/property name.
     * @return Option/property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get option/property value.
     * @return Option/property value.
     */
    public String getValue() {
        return value;
    }

}
