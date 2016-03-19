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
     * Process Java option element.
     * <p>
     * {@code <java_option> :: '{' "name" ':' <option_name> ',' "value" ':' <option_value> '}'}<br>
     * {@code <property>    :: '{' "name" ':' <option_name> ',' "value" ':' <option_value> '}'}
     * @throws IOException 
     */
    private LoaderConfig.Property property() throws IOException {
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
	//data.addMod(optionName, url);
	System.out.print("Property/option: ");
	System.out.print(optionName);
	System.out.print(" = ");
	System.out.println(value != null ? value : "null");
        return new LoaderConfig.Property(name, value);
    }

    /**
     * Process JavaOptions list.
     * "JavaOptions" ':' '[' <java_option>  { ',' <java_option> } ']'
     */
    private void javaoptions() throws IOException {
	// Read object starting symbol.
	next();
	if (token != JsonToken.START_ARRAY) {
	    throw new IOException("Expecting array starting symbol '[' after JavaOptions");
	}
	// Process java options elements stored in array.
	next();
	while (token == JsonToken.START_OBJECT) {
	    final LoaderConfig.Property option = property();
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
	next();
	if (token != JsonToken.START_ARRAY) {
	    throw new IOException("Expecting array starting symbol '[' after properties");
	}
	// Process properties elements stored in array.
	next();
	while (token == JsonToken.START_OBJECT) {
	    final LoaderConfig.Property property = property();
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
        System.out.print("Startup class: ");
        System.out.println(startupClass);
    }
    /**
     * Process Arguments.
     */
    private void arguments() throws IOException {
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
            data.addArgument(new LoaderConfig.Argument(name, value));
            System.out.print("Argument: ");
            System.out.print(name);
            System.out.print(" = ");
            System.out.println(value != null ? value : "null");
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
