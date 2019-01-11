/*
 * (C) 2019 Tomas Kraus
 */
package org.kratz.mc.launcher;

import org.kratz.mc.init.LoaderInit;

/**
 * Replace specified keywords with {@link LoaderInit} values.
 */
public class Replace {

    /** Internal states of state machine. */
    private static enum State {
        /** Initial state: normal text processing. */
        START,
        /** Keyword characters processing. */
        KEYWORD;
    }

    /** Character classes. */
    private static enum Char {
        /** Keyword opening character {@code '<'}. */
        BEG,
        /** Keyword closing character {@code '>'}. */
        END,
        /** Keyword identifier characters: letters from {@code 'path'}, {@code 'user'} and {@code 'password'}. */
        IDE,
        /** Other characters. */
        RST;
    }    

    private static Char toClass(char c) {
        switch (c) {
            case '<': return Char.BEG;
            case '>': return Char.END;
            case 'a': case 'd': case 'e':  case 'h': case 'o': case 'p':  case 'r': case 's': case 't':  case 'u': case 'w':
            case 'A': case 'D': case 'E':  case 'H': case 'O': case 'P':  case 'R': case 'S': case 'T':  case 'U': case 'W':
                return Char.IDE;
            default:
                return Char.RST;
        }
    }

     /**
     * Process source {@link String} and get new {@link String} with all keywords expanded.
     * @param src  {@link String} to be processed.
     * @return {@link String} with all keywords expanded.
     */
    public static String expand(final String src) {
        if (src == null) {
            return null;
        }
        final StringBuilder dst = new StringBuilder();
        final int len = src.length();
        State state = State.START;
        int lastPos = 0;
        for (int pos = 0; pos < len; pos++) {
            final char c = src.charAt(pos);
            final Char cl = toClass(c);
            switch (state) {
                case START:
                    switch (cl) {
                        // Keyword opening character '<'. Starting keyword processing.
                        case BEG:
                            lastPos = pos;
                            state = State.KEYWORD;
                            break;
                        // Normal text, just copy to output.
                        case END: case IDE: case RST:
                            dst.append(c);
                            break;
                        // This code shall be inaccessible.
                        default:
                            throw new IllegalStateException("Undefined character class");
                    }
                    break;
                case KEYWORD:
                    switch (cl) {
                        // Characters that are not a keyword.
                        case RST:
                            // Copy everything to output.
                            for (int i = lastPos; i <= pos; i++) {
                                dst.append(src.charAt(i));
                            }
                            // Start from beginning.
                            state = State.START;
                            break;
                        // Restart keyword processing.
                        case BEG:
                            for (int i = lastPos; i <= pos; i++) {
                                dst.append(src.charAt(i));
                            }
                            lastPos = pos;
                            break;
                        // Just check up to 8 characters.
                        case IDE:
                            // Too long for keyword.
                            if (pos - lastPos > 8) {
                                // Copy everything to output.
                                for (int i = lastPos; i <= pos; i++) {
                                    dst.append(src.charAt(i));
                                }
                                // Start from beginning.
                                state = State.START;                                
                            }
                            break;
                        // We may have keyword.
                        case END:
                            // I know, this is not very optimized line of code. :)
                            String kw = src.substring(lastPos + 1, pos).toLowerCase();
                            switch (kw) {
                                case "path":
                                    dst.append(LoaderInit.getPath());
                                    break;
                                case "user":
                                    dst.append(LoaderInit.getUserName());
                                    break;
                                case "password":
                                    dst.append(LoaderInit.getUserPassword());
                                    break;
                                // Not a keyword.
                                default:
                                // Copy everything to output.
                                for (int i = lastPos; i <= pos; i++) {
                                    dst.append(src.charAt(i));
                                }                                    
                            }
                            state = State.START;
                            break;
                        // This code shall be inaccessible.
                        default:
                            throw new IllegalStateException("Undefined character class");
                    }
                    break;
                // This code shall be inaccessible.
                default:
                    throw new IllegalStateException("Undefined state of state machine");
            }
        }
        if (state == State.KEYWORD) {
            // Copy rest of the source String to output.
            for (int i = lastPos; i < len; i++) {
                dst.append(src.charAt(i));
            }                                    
        }
        return dst.toString();
    }
    
}
