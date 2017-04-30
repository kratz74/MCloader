/*
 * (C) 2017 Tomas Kraus
 */
package mc.init;

import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import mc.json.JsonReader;
import mc.log.LogLevel;
import mc.log.Logger;

/**
 * Reads profiles list.
 */
public class ProfileReader extends JsonReader<LinkedList<Profile>> {

    /**
     * Reads list of profiles from JSON file.
     * @param file Profiles file to be read.
     * @return List of game profiles.
     */
    public static LinkedList<Profile> read(final File file) {
        ProfileReader r = null;
        if (file.canRead()) {
            Logger.log(LogLevel.FINE, "Reading profiles file: %s", file.getAbsolutePath());
            try {
                r = new ProfileReader(file);
                r.parse();
            } catch (IOException ioe) {
                Logger.log(LogLevel.WARNING, "Error reading initialization file: %s", ioe);
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        } else {
            Logger.log(LogLevel.INFO, "Profiles file %s was not found", file.getAbsolutePath());
        }
        return r != null ? r.getData() : null;
    }


    /**
     * Creates an instance of profile file parser.
     * @param file File to be parsed.
     * @throws java.io.IOException when JSON parser cannot be initialized.
     */
    ProfileReader(final File file) throws IOException {
        super(file, new LinkedList<Profile>());
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
                    next();
                    if (token != JsonToken.VALUE_STRING) {
                        throw new IOException("Expected user password String value");
                    }
                    String directory = parser.getText();
                    data.add(new Profile(name, directory));
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
