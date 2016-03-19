/*
 * (C) 2016 Tomas Kraus
 */
package mc.config;

import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.IOException;
import mc.json.JsonReader;

/**
 * Reads loader configuration file.
 * Implemented as simple LR grammar reader using recursion.
 */
public class ConfigReader extends JsonReader<LoaderConfig> {

    public static LoaderConfig read(final String file) {
	ConfigReader r = null;
	try {
	    r = new ConfigReader(new File(file));
	    r.parse();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	} finally {
	    if (r != null) {
		r.close();
	    }
	}
        return r != null ? r.getData() : null;
    }

    /**
     * Creates an instance of loader configuration parser.
     * @param file File to be parsed.
     * @throws java.io.IOException when JSON parser cannot be initialized.
     */
    public ConfigReader(final File file) throws IOException {
        super(file, new LoaderConfig());
    }

    /**
     * Process ClassPath structure.
     * "ClassPath" ':' '[' <path> { ',' <path> } ']'
     */
    private void classpath() throws IOException {
	// Read array starting symbol.
	next();
	if (token != JsonToken.START_ARRAY) {
	    throw new IOException("Expecting array starting symbol '[' after ClassPath");
	}
	// Process path elements.
	next();
	while (token == JsonToken.VALUE_STRING) {
	    String value = parser.getText();
	    data.addClassPath(value);
	    System.out.print("ClasPath Element: ");
	    System.out.println(value);
	    next();
	}
	// Verify that last symbol is array end.
	if (token != JsonToken.END_ARRAY) {
	    throw new IOException("Expecting array ending symbol ']' after ClassPath elements");
	}
    }

    /**
     * Process module object.
     * <p>
     * {@code <module> :: '{' "file" ':' <file_name> ',' "url" ':' <download_url> '}' }
     */
    private void mod() throws IOException {
	next();
	if (token != JsonToken.FIELD_NAME) {
	    throw new IOException("Expected field name");
	}
	String name = parser.getCurrentName();
	if (!"file".equals(name.toLowerCase())) {
	    throw new IOException("Field name shall be \"file\"");
	}
	next();
	if (token != JsonToken.VALUE_STRING) {
	    throw new IOException("Expected field String value");
	}
	String file = parser.getText();
	next();
	if (token != JsonToken.FIELD_NAME) {
	    throw new IOException("Expected field name");
	}
	name = parser.getCurrentName();
	if (!"url".equals(name.toLowerCase())) {
	    throw new IOException("Field name shall be \"url\"");
	}
	next();
	if (token != JsonToken.VALUE_STRING) {
	    throw new IOException("Expected field String value");
	}
	String url = parser.getText();
	data.addMod(file, url);
	System.out.print("Mod: ");
	System.out.print(file);
	System.out.print(" :: ");
	System.out.println(url);
    }

    /**
     * Process Mods structure.
     */
    private void mods() throws IOException {
	// Read array starting symbol.
	next();
	if (token != JsonToken.START_ARRAY) {
	    throw new IOException("Expecting array starting symbol '[' after Mods");
	}
	// Process mod objects stored in array.
	next();
	while (token == JsonToken.START_OBJECT) {
	    mod();
	    next();
	    if (token != JsonToken.END_OBJECT) {
		throw new IOException("Expecting array ending symbol ']' after Mods");
	    }
	    next();
	}
	// Verify that last symbol is array end.
	if (token != JsonToken.END_ARRAY) {
	    throw new IOException("Expecting array ending symbol ']' after Mod elements");
	}

    }

    /**
     * Parses loader configuration.
     * @throws java.io.IOException
     */
    @Override
    public void parse() throws IOException {
	// Expecting START_OBJECT "{"
	next();
	if (token != JsonToken.START_OBJECT) {
	    throw new IOException("Missing starting '{' symbol");
	}
	boolean process = true;
        // Next token shall be top level field name or end.
	while (process) {
            next();
	    switch (token) {
		case FIELD_NAME:
		    final String name = parser.getCurrentName();
		    if ("classpath".equals(name.toLowerCase())) {
			classpath();
		    }
		    if ("mods".equals(name.toLowerCase())) {
			mods();
		    }
		    break;
		case END_OBJECT:
		    process = false;
		    break;
		default:
		    throw new IOException("Expected field name or ending '}' symbol but got " + token.toString());
	    }
	}
	parsingDone = true;
   }

}
