package dev.acton.sample;

import dev.acton.core.annotation.Actor;
import java.util.Map;

@Actor
public class PingActor {
    public Map<String, String> on(Ping ping) {
        return Map.of("Echo", ping.message());
    }
}
