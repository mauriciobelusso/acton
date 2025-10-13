package dev.acton.sample;

import dev.acton.core.annotation.Contract;

@Contract("ping.echo")
public record Ping(String message) {}

