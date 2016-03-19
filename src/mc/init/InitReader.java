/*
 * (C) 2016 Tomas Kraus
 */
package mc.init;

import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.IOException;
import mc.json.JsonReader;

/**
 * Reads loader initialization file.
 * Implemented as simple LR grammar reader using recursion.
 */
class InitReader extends JsonReader<LoaderInit> {
    
    public static LoaderInit read(final String file) {
        InitReader r = null;
        File f = new File(file);
        if (f.canRead()) {
            System.out.println("Reading " + file);
            try {
                r = new InitReader(f);
                r.parse();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        } else {
            System.out.println("File " + file + " was not found");
        }
        return r != null ? r.getData() : null;
    }

     /**
     * Creates an instance of loader initialization file parser.
     * @param file File to be parsed.
     * @throws java.io.IOException when JSON parser cannot be initialized.
     */
    InitReader(final File file) throws IOException {
        super(file, new LoaderInit());
    }

    /**
     * Process path String.
     * <p>
     * {@code "path": "<path>"
     */
    private void path() throws IOException {
	next();
	if (token != JsonToken.VALUE_STRING) {
	    throw new IOException("Expected path String value");
	}
        String path = parser.getText();
        data.setPath(path);
        System.out.print("Path: ");
        System.out.println(path);
    }

    /**
     * Process user name String.
     * <p>
     * {@code "userName": "<user_name>"
     */
    private void userName() throws IOException {
	next();
	if (token != JsonToken.VALUE_STRING) {
	    throw new IOException("Expected path String value");
	}
        String userName = parser.getText();
        data.setUserName(userName);
        System.out.print("User name: ");
        System.out.println(userName);
    }

    /**
     * Parses initialization file.
     * @throws java.io.IOException
     */
    @Override
    public void parse() throws IOException {
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
                    if ("path".equals(name.toLowerCase())) {
                        path();
                    }
                    if ("username".equals(name.toLowerCase())) {
                        userName();
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
