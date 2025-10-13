package dev.acton.sample;

import dev.acton.core.actor.Actor;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PingActor implements Actor {
    public Map<String, String> on(Ping ping) {
        return Map.of("Echo", ping.message());
    }
}
