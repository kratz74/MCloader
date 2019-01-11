/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.init;

import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;

import org.kratz.mc.json.JsonReader;
import org.kratz.mc.common.log.LogLevel;
import org.kratz.mc.common.log.Logger;

/**
 * Reads loader initialization file.
 * Implemented as simple LR grammar reader using recursion.
 */
class InitReader extends JsonReader<LoaderInit> {
    
    /**
     * Reads loader initialization file.
     * @param file Loader initialization file to be read.
     * @return Initialization file content as {@link LoaderInit} instance.
     */
    public static LoaderInit read(final String file) {
        InitReader r = null;
        File f = new File(file);
        if (f.canRead()) {
            Logger.log(LogLevel.FINE, "Reading initialization file: %s", file);
            try {
                r = new InitReader(f);
                r.parse();
            } catch (IOException ioe) {
                Logger.log(LogLevel.WARNING, "Error reading initialization file: %s", ioe);
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        } else {
            Logger.log(LogLevel.INFO, "Initialization file %s was not found", file); 
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
        Logger.log(LogLevel.FINEST, 1, "Path: %s", path);
    }

    /**
     * Process user name String.
     * <p>
     * {@code "userName": "<user_name>"
     */
    private void userName() throws IOException {
	next();
	if (token != JsonToken.VALUE_STRING) {
	    throw new IOException("Expected user name String value");
	}
        String userName = parser.getText();
        data.setUserName(userName);
        Logger.log(LogLevel.FINEST, 1, "User: %s", userName);
    }

    /**
     * Process user password String.
     * <p>
     * {@code "userPassword": "<user_password>"
     */
    private void userPassword() throws IOException {
	next();
	if (token != JsonToken.VALUE_STRING) {
	    throw new IOException("Expected user password String value");
	}
        String userPassword = parser.getText();
        data.setUserPassword(userPassword);
        Logger.log(LogLevel.FINEST, 1, "Password: %s", userPassword);
    }

    /**
     * Process profile String.
     * <p>
     * {@code "profile": "<profile_name>"
     */
    private void profile() throws IOException {
        next();
        if (token != JsonToken.VALUE_STRING) {
            throw new IOException("Expected profile name String value");
        }
        String profile = parser.getText();
        data.setProfile(profile);
        Logger.log(LogLevel.FINEST, 1, "Profile: %s", profile);
    }

    /**
     * Process httpProxyHost String.
     * <p>
     * {@code "httpProxyHost": "<http_proxy_host_name>"
     */
    private void httpProxyHost() throws IOException {
        next();
        if (token != JsonToken.VALUE_STRING) {
            throw new IOException("Expected HTTP proxy host name String value");
        }
        String httpProxyHost = parser.getText();
        data.setHttpProxyHost(httpProxyHost);
        Logger.log(LogLevel.FINEST, 1, "HTTP proxy host: %s", httpProxyHost);
    }

    /**
     * Process httpProxyPort Integer.
     * <p>
     * {@code "httpProxyPort": "<http_proxy_port>"
     */
    private void httpProxyPort() throws IOException {
        next();
        if (token != JsonToken.VALUE_NUMBER_INT) {
            throw new IOException("Expected HTTP proxy port integer value");
        }
        int httpProxyPort = parser.getIntValue();
        data.setHttpProxyPort(httpProxyPort);
        Logger.log(LogLevel.FINEST, 1, "HTTP proxy port: %d", httpProxyPort);
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
                    if ("userpassword".equals(name.toLowerCase())) {
                        userPassword();
                    }
                    if ("profile".equals(name.toLowerCase())) {
                        profile();
                    }
                    if ("httpproxyhost".equals(name.toLowerCase())) {
                        httpProxyHost();
                    }
                    if ("httpproxyport".equals(name.toLowerCase())) {
                        httpProxyPort();
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
