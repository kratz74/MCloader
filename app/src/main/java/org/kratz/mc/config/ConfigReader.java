/*
 * (C) 2016 Tomas Kraus
 */
package org.kratz.mc.config;

import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;

import org.kratz.mc.init.LoaderInit;
import org.kratz.mc.json.JsonReader;
import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;

/**
 * Reads loader configuration file.
 * Implemented as simple LR grammar reader using recursion.
 */
public class ConfigReader extends JsonReader<LoaderConfig> {

    /** Loader configuration file name. */
    private static final String CONFIG_FILE = "loader.json";

    /**
     * Reads loader configuration file.
     * @return Configuration file content as {@link LoaderConfig} instance.
     */
    public static LoaderConfig read() {
        final String filePath = LoaderInit.getCurrentConfigFile(null);
        ConfigReader r = null;
        if (filePath != null) {
            final File file = new File(filePath);
            if (file.canRead()) {
                Logger.log(LogLevel.FINE, "Reading configuration file: %s", file.getAbsolutePath());
                try {
                    r = new ConfigReader(file);
                    r.parse();
                } catch (IOException ioe) {
                    Logger.log(LogLevel.WARNING, "Error reading configuration file: %s", ioe);
                } finally {
                    if (r != null) {
                        r.close();
                    }
                }
            } else {
                Logger.log(LogLevel.INFO, "Configuration file %s was not found", file.getAbsolutePath());
            }
        } else {
            Logger.log(LogLevel.INFO, "Configuration file does not exist yet");
        }
        return r != null ? r.getData() : null;
    }

    /**
     * Creates an instance of loader configuration parser.
     * @param file Configuration file to read.
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
        String optionName = null;
        String optionValue = null;
        String optionOS = null;
        while (token == JsonToken.FIELD_NAME) {
            final String name = parser.getCurrentName().toLowerCase();
            switch(name) {
                case "name": case "value": case "os": break;
                default: throw new IOException("Expected Java option or property field name: 'name', 'value', 'os'");
            }        
            next();
            switch(name) {
                case "name":
                    if (token != JsonToken.VALUE_STRING) {
                        throw new IOException("Expected 'name' field String value");
                    }
                    optionName = parser.getText();
                    break;
                case "value":
                    switch (token) {
                        case VALUE_STRING:
                            String value = parser.getText();
                            if (value != null && !value.toLowerCase().startsWith("http:")) {
                                value = value.replace(LoaderConfig.SEPARATOR, File.separatorChar);
                            }
                            optionValue = value;
                            break;
                        case VALUE_NULL:
                            optionValue = null;
                            break;
                        default:
                            throw new IOException("Expected 'value' field String or null value");
                    }
                    break;
                case "os":
                    switch (token) {
                        case VALUE_STRING:
                            optionOS = parser.getText();
                            break;
                        case VALUE_NULL:
                            optionOS = null;
                            break;
                        default:
                            throw new IOException("Expected 'os' field String or null value");
                    }
                    break;
            }
            next();
        }
        if (optionName == null) {
            throw new IOException("Name field is missing in Java option or property");
        }
        Logger.log(LogLevel.FINEST, 2, "Property/option %s = %s for %s",
                optionName, optionValue != null ? optionValue : "N/A", optionOS != null ? optionOS : "all");
        return optionOS != null
                ? new Property(optionName, optionValue, optionOS) : new Property(optionName, optionValue);
    }

    /**
     * Process JavaOptions list.
     * "JavaOptions" ':' '[' <java_option>  { ',' <java_option> } ']'
     */
    private void javaoptions() throws IOException {
	// Read object starting symbol.
        Logger.log(LogLevel.FINE, 1, "Processing JavaOptions list");
	next();
	if (token != JsonToken.START_ARRAY) {
	    throw new IOException("Expecting array starting symbol '[' after JavaOptions");
	}
	// Process java options elements stored in array.
	next();
	while (token == JsonToken.START_OBJECT) {
	    final Property option = property();
	    //next();
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
        Logger.log(LogLevel.FINE, 1, "Processing Properties list");
	next();
	if (token != JsonToken.START_ARRAY) {
	    throw new IOException("Expecting array starting symbol '[' after properties");
	}
	// Process properties elements stored in array.
	next();
	while (token == JsonToken.START_OBJECT) {
	    final Property property = property();
	    //next();
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
        Logger.log(LogLevel.FINE, 1, "Processing ClassPath list");
	// Read array starting symbol.
	next();
	if (token != JsonToken.START_ARRAY) {
	    throw new IOException("Expecting array starting symbol '[' after ClassPath");
	}
	// Process path elements.
	next();
	while (token == JsonToken.VALUE_STRING) {
	    String value = parser.getText();
	    data.addClassPath(value != null ? value.replace(LoaderConfig.SEPARATOR, File.separatorChar) : null);
            Logger.log(LogLevel.FINEST, 2, "ClassPath: %s", value);
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
        Logger.log(LogLevel.FINE, 1, "Startup class: %s", startupClass);
    }
    /**
     * Process Arguments.
     */
    private void arguments() throws IOException {
        Logger.log(LogLevel.FINE, 1, "Processing Arguments list");
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
            data.addArgument(new Argument(
                    name, value != null ? value.replace(LoaderConfig.SEPARATOR, File.separatorChar) : null));
            Logger.log(LogLevel.FINEST, 2, "Argumant: %s = %s", name, value != null ? value : "null");
        }
	// Verify that last symbol is object end.
	if (token != JsonToken.END_OBJECT) {
	    throw new IOException("Expecting object ending symbol '}' after Arguments elements");
	}

    }

    /**
     * Process Game.
     */
    private void game() throws IOException {
        Logger.log(LogLevel.FINE, 1, "Processing Game object");
	// Read object starting symbol.
	next();
	if (token != JsonToken.START_OBJECT) {
	    throw new IOException("Expecting object starting symbol '{' after Game");
	}
        next();
        while (token == JsonToken.FIELD_NAME) {
            final String name = parser.getCurrentName();
            next();
            if (token != JsonToken.VALUE_STRING) {
                throw new IOException("Expected field String value");
            }
            String value = parser.getText();
            next();
            switch(name) {
                case "url":
                    data.setGameUrl(value);
                    Logger.log(LogLevel.FINEST, 2, "Game base package URL: %s", value);
                    break;
                case "mods":
                    data.setModsPath(value);
                    Logger.log(LogLevel.FINEST, 2, "Game modules directory: %s", value);
                    break;
            }
        }
        if (token != JsonToken.END_OBJECT) {
	    throw new IOException("Expecting object ending symbol '}' after Arguments elements");
	}       
    }

    /**
     * Process module object.
     * <p>
     * {@code <module> :: '{' "file" ':' <file_name> ',' "chksum" ':' <Adler32_hexa> "url" ':' <download_url> '}' }
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
	if (!"chksum".equals(name.toLowerCase())) {
	    throw new IOException("Field name shall be \"chksum\"");
	}
	next();
	if (token != JsonToken.VALUE_STRING) {
	    throw new IOException("Expected field String value");
	}
	String chkSum = parser.getText();
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
        data.addMod(file, chkSum, url);
        Logger.log(LogLevel.FINEST, 2, "Mod file: %s AD32: %s, URL: %s", file, chkSum, url);
    }

    /**
     * Process Mods structure.
     */
    private void mods() throws IOException {
        Logger.log(LogLevel.FINE, 1, "Processing Mods list");
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
     * @throws IOException when there is a problem with reading the file.
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
                        case "game":
                            game();
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
