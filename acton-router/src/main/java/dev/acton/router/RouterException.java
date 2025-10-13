package dev.acton.router;

/**
 * Runtime exception for router-level failures.
 */
public class RouterException extends RuntimeException {
    public RouterException(String msg) { super(msg); }
    public RouterException(String msg, Throwable cause) { super(msg, cause); }
}
