/*
 * (C) 2016 Tomas Kraus
 */
package mc.init;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * JSON writer.
 */
public class InitWriter implements Closeable {
    
    /** JSON parser factory. */
    private static JsonFactory factory = new JsonFactory();

    public static void write(final String file, final LoaderInit data) {
        System.out.println("Writting " + file);
        InitWriter w = null;
	try {
	    w = new InitWriter(new File(file), data);
	    w.write();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	} finally {
	    if (w != null) {
		w.close();
	    }
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
        generator = factory.createGenerator(file, JsonEncoding.UTF8);
    }

    /**
     * Write 
     */
    public void write() throws IOException {
        generator.writeStartObject();
        generator.useDefaultPrettyPrinter();
        final String path = data.getPath();
        final String userName = data.getUserName();
        if (path != null) {
            generator.writeFieldName("path");
            generator.writeString(path);
        }
        if (userName != null) {
            generator.writeFieldName("userName");
            generator.writeString(userName);
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
	    ioe.printStackTrace();
	}
    }
}
