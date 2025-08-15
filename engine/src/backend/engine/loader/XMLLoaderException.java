package backend.engine.loader;

/**
 * Custom exception for XML loading errors
 */
public class XMLLoaderException extends Exception {
    private static final long serialVersionUID = 1L;

    public XMLLoaderException(String message) {
        super(message);
    }

    public XMLLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}

