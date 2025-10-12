package dev.acton.core.exception;

/**
 * Base runtime exception for all ActOn modules.
 */
public class ActOnException extends RuntimeException {
    public ActOnException(String message) { super(message); }
    public ActOnException(String message, Throwable cause) { super(message, cause); }
}
