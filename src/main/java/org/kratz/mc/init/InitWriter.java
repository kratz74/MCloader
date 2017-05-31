/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.init;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.kratz.mc.log.LogLevel;
import org.kratz.mc.log.Logger;

/**
 * JSON writer.
 */
public class InitWriter implements Closeable {
    
    /** JSON parser factory. */
    private static final JsonFactory FACTORY = new JsonFactory();

    /**
     * Writes loader initialization file.
     * @param file Target loader initialization file.
     * @param data Initialization file content.
     */
    public static void write(final String file, final LoaderInit data) {
        Logger.log(LogLevel.FINE, "Writing initialization file: %s", file);
	try (InitWriter w = new InitWriter(new File(file), data)) {
	    w.write();
	} catch (IOException ioe) {
	    Logger.log(LogLevel.WARNING, "Error writing initialization file: %s", ioe);
	}        
    }

    /** JSON parser. */ 
    private final JsonGenerator generator;

    /** Loader initialization data to be written. */
    private final LoaderInit data;

    /**
     * Creates an instance of loader initialization file writer.
     * @param file File to be written.
     * @param data Loader initialization data to be written.
     * @throws java.io.IOException when JSON writer cannot be initialized.
     */
    public InitWriter(final File file, final LoaderInit data) throws IOException {
        this.data = data;
        generator = FACTORY.createGenerator(file, JsonEncoding.UTF8);
    }

    /**
     * Write JSON file content.
     * @throws IOException when there is a problem with writing the file.
     */
    public void write() throws IOException {
        generator.writeStartObject();
        generator.useDefaultPrettyPrinter();
        final String path = LoaderInit.getPath();
        final String userName = LoaderInit.getUserName();
        final String userPassword = LoaderInit.getUserPassword();
        final String profile = LoaderInit.getProfile();
        if (path != null) {
            generator.writeFieldName("path");
            generator.writeString(path);
        }
        if (userName != null) {
            generator.writeFieldName("userName");
            generator.writeString(userName);
        }
        if (userPassword != null) {
            generator.writeFieldName("userPassword");
            generator.writeString(userPassword);
        }
        if (profile != null) {
            generator.writeFieldName("profile");
            generator.writeString(profile);
        }
        generator.writeEndObject();
    }

    /**
     * Close parser and free all resources.
     */
    @Override
    public void close() {
	try {
            generator.close();
	} catch (IOException ioe) {
	    Logger.log(LogLevel.WARNING, "Error closing initialization file: %s", ioe);
	}
    }
}
