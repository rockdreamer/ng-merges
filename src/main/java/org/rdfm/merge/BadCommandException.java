package org.rdfm.merge;

/**
 * Created by bantaloukasc on 29/08/15.
 */
public class BadCommandException extends Exception {
    public BadCommandException(String s) {
        super(s);
    }

    public BadCommandException(String s, Throwable cause) {
        super(s, cause);
    }
}
