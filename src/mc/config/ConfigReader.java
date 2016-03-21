/*
 * (C) 2016 Tomas Kraus
 */
package mc.config;

import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.IOException;
import mc.json.JsonReader;
import mc.log.LogLevel;
import mc.log.Logger;

/**
 * Reads loader configuration file.
 * Implemented as simple LR grammar reader using recursion.
 */
public class ConfigReader extends JsonReader<LoaderConfig> {

    /** Logger. */
    private static final Logger LOG = Logger.getInstance();

    /**
     * Reads loader configuration file.
     * @param file Loader configuration file to be read.
     * @return Configuration file content as {@link LoaderConfig} instance.
     */
    public static LoaderConfig read(final String file) { 
	ConfigReader r = null;
	try {
            LOG.log(LogLevel.FINE, "Reading configuration file: %s", file);
	    r = new ConfigReader(new File(file));
	    r.parse();
	} catch (IOException ioe) {
            LOG.log(LogLevel.WARNING, "Error reading configuration file: %s", ioe);
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
     * Process Java option element.
     * <p>
     * {@code <java_option> :: '{' "name" ':' <option_name> ',' "value" ':' <option_value> '}'}<br>
     * {@code <property>    :: '{' "name" ':' <option_name> ',' "value" ':' <option_value> '}'}
     * @throws IOException 
     */
    private Property property() throws IOException {
	next();
	if (token != JsonToken.FIELD_NAME) {
	    throw new IOException("Expected Java option 'name' field name");
	}
	String name = parser.getCurrentName();
	if (!"name".equals(name.toLowerCase())) {
	    throw new IOException("Field name shall be \"name\"");
	}
	next();
	if (token != JsonToken.VALUE_STRING) {
	    throw new IOException("Expected 'name' field String value");
	}
	String optionName = parser.getText();
	next();
	if (token != JsonToken.FIELD_NAME) {
	    throw new IOException("Expected Java option 'value' field name");
	}
	name = parser.getCurrentName();
	if (!"value".equals(name.toLowerCase())) {
	    throw new IOException("Field name shall be \"value\"");
	}
	next();
        String value;
        switch (token) {
            case VALUE_STRING:
                value = parser.getText();
                break;
            case VALUE_NULL:
                value = null;
                break;
            default:
                throw new IOException("Expected 'value' field String or null value");
        }
        LOG.log(LogLevel.FINEST, 2, "Property/option %s = %s", optionName, value != null ? value : "null");
        return new Property(optionName, value);
    }

    /**
     * Process JavaOptions list.
     * "JavaOptions" ':' '[' <java_option>  { ',' <java_option> } ']'
     */
    private void javaoptions() throws IOException {
	// Read object starting symbol.
        LOG.log(LogLevel.FINE, 1, "Processing JavaOptions list");
	next();
	if (token != JsonToken.START_ARRAY) {
	    throw new IOException("Expecting array starting symbol '[' after JavaOptions");
	}
	// Process java options elements stored in array.
	next();
	while (token == JsonToken.START_OBJECT) {
	    final Property option = property();
	    next();
	    if (token != JsonToken.END_OBJECT) {
		throw new IOException("Expecting array ending symbol '}' after JavaOptions");
	    }
            data.addJavaOption(option);
	    next();
	}
	// Verify that last symbol is object end.
	if (token != JsonToken.END_ARRAY) {
	    throw new IOException("Expecting array ending symbol ']' after JavaOptions elements");
	}
    }

    /**
     * Process properties list.
     * "Properties" ':' '[' <property>  { ',' <property> } ']'
     */
    private void properties() throws IOException {
	// Read object starting symbol.
        LOG.log(LogLevel.FINE, 1, "Processing Properties list");
	next();
	if (token != JsonToken.START_ARRAY) {
	    throw new IOException("Expecting array starting symbol '[' after properties");
	}
	// Process properties elements stored in array.
	next();
	while (token == JsonToken.START_OBJECT) {
	    final Property property = property();
	    next();
	    if (token != JsonToken.END_OBJECT) {
		throw new IOException("Expecting array ending symbol '}' after properties");
	    }
            data.addProperty(property);
	    next();
	}
	// Verify that last symbol is object end.
	if (token != JsonToken.END_ARRAY) {
	    throw new IOException("Expecting array ending symbol ']' after properties elements");
	}
    }

    /**
     * Process ClassPath structure.
     * "ClassPath" ':' '[' <path> { ',' <path> } ']'
     */
    private void classpath() throws IOException {
        LOG.log(LogLevel.FINE, 1, "Processing ClassPath list");
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
            LOG.log(LogLevel.FINEST, 2, "ClassPath: %s", value);
	    next();
	}
	// Verify that last symbol is array end.
	if (token != JsonToken.END_ARRAY) {
	    throw new IOException("Expecting array ending symbol ']' after ClassPath elements");
	}
    }

    /**
     * Process Class value.
     * "Class" ':' <startup_class>
     */
    private void startupClass() throws IOException {
	// Read class name.
	next();
	if (token != JsonToken.VALUE_STRING) {
	    throw new IOException("Expected 'Class' field String value");
	}
	String startupClass = parser.getText();
        data.setStartupClass(startupClass);
        LOG.log(LogLevel.FINE, 1, "Startup class: %s", startupClass);
    }
    /**
     * Process Arguments.
     */
    private void arguments() throws IOException {
        LOG.log(LogLevel.FINE, 1, "Processing Arguments list");
	// Read object starting symbol.
	next();
	if (token != JsonToken.START_OBJECT) {
	    throw new IOException("Expecting object starting symbol '{' after Arguments");
	}
	// Process path elements.
	next();
        while (token == JsonToken.FIELD_NAME) {
            final String name = parser.getCurrentName();
            next();
            String value;
            switch (token) {
                case VALUE_STRING:
                    value = parser.getText();
                    break;
                case VALUE_NULL:
                    value = null;
                    break;
                default:
                    throw new IOException("Expected 'value' field String or null value");
            }
            next();
            data.addArgument(new Argument(name, value));
            LOG.log(LogLevel.FINEST, 2, "Argumant: %s = %s", name, value != null ? value : "null");
        }
	// Verify that last symbol is object end.
	if (token != JsonToken.END_OBJECT) {
	    throw new IOException("Expecting object ending symbol '}' after Arguments elements");
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
        LOG.log(LogLevel.FINEST, 2, "Mod file: %s URL: %s", file, url);
    }

    /**
     * Process Mods structure.
     */
    private void mods() throws IOException {
        LOG.log(LogLevel.FINE, 1, "Processing Mods list");
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
		    final String name = parser.getCurrentName().toLowerCase();
                    switch(name) {
                        case "javaoptions":
                            javaoptions();
                            break;
                        case "properties":
                            properties();
                            break;
                        case "classpath":
                            classpath();
                            break;
                        case "class":
                            startupClass();
                            break;
                        case "arguments":
                            arguments();
                            break;
                        case "mods":
                            mods();
                            break;
                        default:
                            throw new IOException("Unknown field name: " + name);
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
