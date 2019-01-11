/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;

/**
 * Abstract JSON reader.
 * Contains common JSON reader code.
 * @param <T> JSON data structure.
 */
public abstract class JsonReader<T> implements Closeable {

    /** JSON parser factory. */
    protected static JsonFactory factory = new JsonFactory();

    /** Loader configuration instance initialized from loader configuration file. */
    protected final T data;

    /** JSON parser. */ 
    protected final JsonParser parser;

    /** Current JSON token. */
    protected JsonToken token;

    /** Parsing done flag. */
    protected boolean parsingDone;
    
    /**
     * Creates an instance of loader configuration parser.
     * @param file File to be parsed.
     * @param data JSON data structure instance.
     * @throws java.io.IOException when JSON parser cannot be initialized.
     */
    public JsonReader(final File file, final T data) throws IOException {
        parser = factory.createParser(file);
	this.data = data;
	parsingDone = false;
    }

    /**
     * Creates an instance of loader configuration parser.
     * @param is   {@link InputStream} to be parsed.
     * @param data JSON data structure instance.
     * @throws IOException when JSON parser cannot be initialized.
     */
    public JsonReader(InputStream is, final T data) throws IOException {
        parser = factory.createParser(is);
	this.data = data;
	parsingDone = false;
    }

    /**
     * Parses JSON file.
     * @throws java.io.IOException when there is a problem when reading the file.
     */
    public abstract void parse() throws IOException;

    /**
     * Retrieve next token from JSON file.
     * @throws IOException when there is a problem when reading the file.
     */
    protected void next() throws IOException {
	token = parser.nextToken();
	String name = parser.getCurrentName();
    }

    /**
     * Get loader configuration after configuration file parsing is finished.
     * @return Loader configuration instance initialized from configuration file.
     * @throws IllegalStateException If called before configuration file parsing is finished.
     */
    public T getData() {
	if (!parsingDone) {
	    throw new IllegalStateException("File was not parsed yet");
	}
	return data;
    }

    /**
     * Close parser and free all resources.
     */
    @Override
    public void close() {
	try {
            parser.close();
	} catch (IOException ioe) {
	    Logger.log(LogLevel.WARNING, "Error closing file: %s", ioe);
	}
    }

    /**
     * Clean up static fields when this class is no longer needed.
     */
    public static void clean() {
        factory = null;
    }

}
