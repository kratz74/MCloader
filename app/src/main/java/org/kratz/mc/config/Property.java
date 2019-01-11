/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.config;

import java.util.LinkedList;

import org.kratz.mc.utils.OS;

/**
 * Java option and/or property configuration element.
 */
public class Property {

    /** Option/property name. */
    private final String name;

    /** Option/property value. */
    private final String value;

    /** Specific OS ({@code null} means allowed for all). */
    private final OS[] os;

    /**
     * Parse list of OS names.
     * Constructor helper method.
     * @param os List of OS names as {@link String}.
     * @return Array of OS instances retrieved from {@code os} argument.
     */
    private static OS[] initOS(final String os) {
        if (os == null) {
            return null;
        }
        final String[] osStrings = os.split(" *, *");
        final LinkedList<OS> osList = new LinkedList<>();
        for (final String osString : osStrings) {
            final OS osValue = OS.toValue(osString);
            if (osValue != null) {
                osList.add(osValue);
            }
        }
        if (osList.isEmpty()) {
            return null;
        }
        final int len = osList.size();
        final OS[] osArray = new OS[len];
        int i = 0;
        for (final OS osValue : osList) {
            osArray[i++] = osValue;
        }
        return osArray;
    }

    /**
     * Creates an instance of option and/or property configuration element.
     * @param name  Option/property name.
     * @param value Option/property value.
     */
    Property(final String name, final String value, final String os) {
        this.name = name;
        this.value = value;
        this.os = initOS(os);
    }

    /**
     * Creates an instance of option and/or property configuration element.
     * @param name  Option/property name.
     * @param value Option/property value.
     */
    Property(final String name, final String value) {
        this.name = name;
        this.value = value;
        this.os = null;
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

    /**
     * Get specific OS array for this property.
     * @return Specific OS array for this property.
     */
    public OS[] getOS() {
        return os;
    }

    /**
     * Check whether this property is allowed for current OS.
     * @return Value of {@code true} if property is allowed for current OS or {@code false} otherwise.
     */
    public boolean isAlowed() {
        if (os == null) {
            return true;
        }
        for (OS osValue : os) {
            if (osValue == OS.os) {
                return true;
            }
        }
        return false;
    }

}
