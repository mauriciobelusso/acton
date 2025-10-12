package dev.acton.core.actor;

/**
 * Base marker interface for domain actors.
 * An actor executes logic for one or more contracts (commands or queries).
 */
public interface Actor {
    // marker interface — behavior defined by runtime modules (router, grpc, etc.)
}
